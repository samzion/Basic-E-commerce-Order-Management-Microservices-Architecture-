package paymentManagement.httpHandlers;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import paymentManagement.RunPaymentManagement;
import paymentManagement.models.bank.TransferProcessor;
import paymentManagement.models.entities.Account;
import paymentManagement.models.entities.MerchantPayment;
import paymentManagement.models.requests.PayRequest;
import paymentManagement.models.response.AccountOperationResponse;
import paymentManagement.models.response.PaymentResponse;
import paymentManagement.models.response.UserMerchantDetails;
import paymentManagement.models.response.UserMerchantPlusMessage;
import paymentManagement.services.AccountService;
import paymentManagement.services.LoanService;
import paymentManagement.services.UserServiceClient;
import userManagement.RunUserManagement;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

public class PayHandler extends BaseHandler implements HttpHandler {
    private TransferProcessor transferProcessor;
    private AccountService accountService;
    private LoanService loanService;
    private static String paymentMSAdminAcccountNumber;
    private static String ecommerceAdminAccountNumber;

    public static void initialize(String adminAccountNumber, String ecommerceAccount) {
        paymentMSAdminAcccountNumber = adminAccountNumber;
        ecommerceAdminAccountNumber = ecommerceAccount;
    }

    public PayHandler(TransferProcessor transferProcessor, AccountService accountService, LoanService loanService){
        this.accountService = accountService;
        this.transferProcessor = transferProcessor;
        this.loanService = loanService;
    }
    public void handle (HttpExchange exchange) throws IOException {
        if(!this.isValidRequestMethod(exchange, "post")) {
            // Handle the request
            String response = "Method not allowed";
            RunPaymentManagement.writeHttpResponse(exchange, 405, response);
            return;
        }

        //fetch User +  merchant details if available using HttpClient call to User mgt. MS
        UserServiceClient userServiceClient = new UserServiceClient();
        UserMerchantPlusMessage userMerchantPlusMessage;
        try {
            userMerchantPlusMessage = userServiceClient.getUserMerchantDetails(exchange);
        } catch (Exception e) {
            RunUserManagement.writeHttpResponse(exchange, 500, "unknown error");
            return;
        }

        if(userMerchantPlusMessage.getErrorMessage() != null) {
            String errorMessage = userMerchantPlusMessage.getErrorMessage();
            if (errorMessage.equals("Unauthorized!")) {
                RunPaymentManagement.writeHttpResponse(exchange, 401, errorMessage);
                return;
            }
            if (errorMessage.equals("Invalid Authorization header format!")) {
                RunPaymentManagement.writeHttpResponse(exchange, 400, errorMessage);
                return;
            }
            if (errorMessage.equals("Unauthorized: invalid credentials")) {
                RunPaymentManagement.writeHttpResponse(exchange, 401, errorMessage);
                return;
            }
        }
        UserMerchantDetails userMerchantDetails = userMerchantPlusMessage.getUserMerchantDetails();

        String body = "{}";
        try (InputStream input = exchange.getRequestBody()) {
            body =  new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        PayRequest payRequest = gson.fromJson(body, PayRequest.class);
        String validationMessage = PayRequest.validate(payRequest);
        if(!validationMessage.equals("Pay Now request okay!")){
            RunPaymentManagement.writeHttpResponse(exchange, 400, validationMessage);
            return;
        }
        // get customer account number
        List<Account> customerAccounts = null;
        PaymentResponse paymentResponse =  new PaymentResponse();
        try {
            customerAccounts = accountService.listAccount(userMerchantDetails);
        } catch (SQLException e) {
            paymentResponse.setStatus(500);
            paymentResponse.setMessage("Unknown error from server");
            String jsonResponse = gson.toJson(paymentResponse);
            RunPaymentManagement.writeHttpResponse(exchange, 500, jsonResponse);
            return;
        }
        assert customerAccounts != null;
        if(customerAccounts.isEmpty()){
            paymentResponse.setStatus(404);
            paymentResponse.setMessage("No accounts found for customer");
            String jsonResponse = gson.toJson(paymentResponse);
            RunPaymentManagement.writeHttpResponse(exchange, 404, jsonResponse);
            return;
        }
        AccountOperationResponse transferFromCustomerResponse = null;
        for(Account account : customerAccounts){
            if(account.getBalance()< payRequest.getTotalAmount()){
                continue;
            }
            try {
               transferFromCustomerResponse = transferProcessor.transfer(userMerchantDetails, account.getAccountNumber(),
                        paymentMSAdminAcccountNumber, payRequest.getTotalAmount());
            } catch (SQLException | ClassNotFoundException e) {
                paymentResponse.setStatus(500);
                paymentResponse.setMessage("Unknown error from server");
                String jsonResponse = gson.toJson(paymentResponse);
                RunPaymentManagement.writeHttpResponse(exchange, 500, jsonResponse);
                return;
            }
            break;
        }
//TODO: Make more readable by using if-else statement
        if(transferFromCustomerResponse == null){
            if(payRequest.isPayLater()){
                //call loan service, pay merchant and ecommerce admin
                if(payMerchantAndEcommerceAdmin(exchange, payRequest, paymentResponse)){
                    try {
                        this.loanService.createLoan(customerAccounts.getFirst(), payRequest.getTotalAmount()*1.10);
                    } catch (SQLException e) {
                        paymentResponse.setStatus(500);
                        paymentResponse.setMessage("Unknown error from server");
                        String jsonResponse = gson.toJson(paymentResponse);
                        RunPaymentManagement.writeHttpResponse(exchange, 500, jsonResponse);
                        return;
                    }
                    paymentResponse.setStatus(200);
                    paymentResponse.setMessage("Payment successful! Settle your loan on time");
                    String jsonResponse = gson.toJson(paymentResponse);
                    RunPaymentManagement.writeHttpResponse(exchange, 200, jsonResponse);
                    return;
                }
            }
            paymentResponse.setStatus(402);
            paymentResponse.setMessage("Insufficient balance in customer account");
            String jsonResponse = gson.toJson(paymentResponse);
            RunPaymentManagement.writeHttpResponse(exchange, 402, jsonResponse);
            return;
        }
        if(transferFromCustomerResponse.getStatusCode() != 200){
            String jsonResponse = gson.toJson(transferFromCustomerResponse);
            RunPaymentManagement.writeHttpResponse(exchange, transferFromCustomerResponse.getStatusCode(), jsonResponse);
            return;
        }

        //logic for sending 97% of totalAmount to merchant and 2.5% of totalAmount to ecommerceAccount and 0.5% to paymentAccount
        if (payMerchantAndEcommerceAdmin(exchange, payRequest, paymentResponse)){
            paymentResponse.setStatus(200);
            paymentResponse.setMessage("Payment successful!");
            String jsonResponse = gson.toJson(paymentResponse);
            RunPaymentManagement.writeHttpResponse(exchange, 200, jsonResponse);
            return;
        }
        paymentResponse.setStatus(500);
        paymentResponse.setMessage("Unknown error from server");
        String jsonResponse = gson.toJson(paymentResponse);
        RunPaymentManagement.writeHttpResponse(exchange, 500, jsonResponse);
    }

    private boolean payMerchantAndEcommerceAdmin(HttpExchange exchange, PayRequest payRequest, PaymentResponse paymentResponse) throws IOException {
        try{
            for(MerchantPayment merchantPayment: payRequest.getMerchantPayments()){
                String merchantAccountNumber = accountService.getAccount(merchantPayment.getMerchantId()).getAccountNumber();
                AccountOperationResponse transferResponse = transferProcessor.transfer(paymentMSAdminAcccountNumber
                        , merchantAccountNumber, merchantPayment.getAmount()*0.97);
                AccountOperationResponse transferResponse2 = transferProcessor.transfer(paymentMSAdminAcccountNumber
                        , ecommerceAdminAccountNumber, merchantPayment.getAmount()*0.025);
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
