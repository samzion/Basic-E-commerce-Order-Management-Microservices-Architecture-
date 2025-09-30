package paymentManagement.httpHandlers;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import paymentManagement.RunPaymentManagement;
import paymentManagement.models.bank.TransferProcessor;
import paymentManagement.models.entities.Account;
import paymentManagement.models.entities.MerchantPayment;
import paymentManagement.models.requests.PayNowRequest;
import paymentManagement.models.response.AccountOperationResponse;
import paymentManagement.models.response.UserMerchantDetails;
import paymentManagement.models.response.UserMerchantPlusMessage;
import paymentManagement.services.AccountService;
import paymentManagement.services.UserServiceClient;
import userManagement.RunUserManagement;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

public class PayNowHandler extends BaseHandler implements HttpHandler {
    private TransferProcessor transferProcessor;
    private AccountService accountService;
    private static String paymentMSAdminAcccountNumber;
    private static String ecommerceAdminAccountNumber;

    public static void initialize(String adminAccountNumber, String ecommerceAccount) {
        paymentMSAdminAcccountNumber = adminAccountNumber;
        ecommerceAdminAccountNumber = ecommerceAccount;
    }

    public PayNowHandler(TransferProcessor transferProcessor, AccountService accountService){
        this.accountService = accountService;
        this.transferProcessor = transferProcessor;
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
        PayNowRequest payNowRequest = gson.fromJson(body, PayNowRequest.class);
        String validationMessage = PayNowRequest.validate(payNowRequest);
        if(!validationMessage.equals("Pay Now request okay!")){
            RunPaymentManagement.writeHttpResponse(exchange, 400, validationMessage);
            return;
        }
        // get customer account number
        List<Account> customerAccounts = null;
        try {
            customerAccounts = accountService.listAccount(userMerchantDetails);
        } catch (SQLException e) {
            RunPaymentManagement.writeHttpResponse(exchange, 500, "Unknown error from server");
            return;
        }
        assert customerAccounts != null;
        if(customerAccounts.isEmpty()){
            RunPaymentManagement.writeHttpResponse(exchange, 404, "No accounts found for customer");
            return;
        }
        AccountOperationResponse transferFromCustomerResponse = null;
        for(Account account : customerAccounts){
            if(account.getBalance()< payNowRequest.getTotalAmount()){
                continue;
            }
            try {
               transferFromCustomerResponse = transferProcessor.transfer(userMerchantDetails, account.getAccountNumber(),
                        paymentMSAdminAcccountNumber, payNowRequest.getTotalAmount());
            } catch (SQLException | ClassNotFoundException e) {
                RunPaymentManagement.writeHttpResponse(exchange, 500, "Unknown error from server");
                return;
            }
            break;
        }

        if(transferFromCustomerResponse == null){
            RunPaymentManagement.writeHttpResponse(exchange, 402, "Insufficient balance in customer account");
            return;
        }
        if(transferFromCustomerResponse.getStatusCode() != 200){
            RunPaymentManagement.writeHttpResponse(exchange, transferFromCustomerResponse.getStatusCode(), transferFromCustomerResponse.getMessage());
            return;
        }

        //logic for sending 97% of totalAmount to merchant and 2.5% of totalAmount to ecommerceAccount and 0.5% to paymentAccount
        try{
            for(MerchantPayment merchantPayment: payNowRequest.getMerchantPayments()){
                String merchantAccountNumber = accountService.getAccount(merchantPayment.getMerchantId()).getAccountNumber();
                AccountOperationResponse transferResponse = transferProcessor.transfer(paymentMSAdminAcccountNumber
                        , merchantAccountNumber, merchantPayment.getAmount()*0.97);
                AccountOperationResponse transferResponse2 = transferProcessor.transfer(paymentMSAdminAcccountNumber
                        , ecommerceAdminAccountNumber, merchantPayment.getAmount()*0.025);
            }
        } catch (Exception e) {
            RunPaymentManagement.writeHttpResponse(exchange, 500, "Unknown error from server");
            return;
        }
        RunPaymentManagement.writeHttpResponse(exchange, 200, "Payment successful!");
    }
}
