package paymentManagement.httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import paymentManagement.RunPaymentManagement;
import paymentManagement.models.entities.Account;
import paymentManagement.models.response.UserMerchantDetails;
import paymentManagement.models.response.UserMerchantPlusMessage;
import paymentManagement.services.AccountService;
import paymentManagement.services.TransactionService;
import paymentManagement.services.UserServiceClient;
import userManagement.RunUserManagement;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;

public class AccountCreationHandler extends BaseHandler implements HttpHandler {
    private AccountService accountService;
    private  TransactionService transactionService;

    public AccountCreationHandler(AccountService accountService, TransactionService transactionService){
        this.accountService = accountService;
        this.transactionService = transactionService;

    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if(!this.isValidRequestMethod(exchange, "get")) {
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
            }
            if (errorMessage.equals("Invalid Authorization header format!")) {
                RunPaymentManagement.writeHttpResponse(exchange, 400, errorMessage);
            }
            if (errorMessage.equals("Unauthorized: invalid credentials")) {
                RunPaymentManagement.writeHttpResponse(exchange, 401, errorMessage);
                return;
            }
        }

        UserMerchantDetails userMerchantDetails = userMerchantPlusMessage.getUserMerchantDetails();
        try{
                Account newCreatedAccount =   this.accountService.getAccountDetails(createAccountFromUserDetails(userMerchantDetails));
                newCreatedAccount.setTransactions(null);
                newCreatedAccount.setLoans(null);
                String jsonResponse = gson.toJson(newCreatedAccount);
                RunPaymentManagement.writeHttpResponse(exchange, 200, jsonResponse);

        } catch (Exception e) {
            RunPaymentManagement.writeHttpResponse(exchange, 500, "Unknown error from server");
        }
    }

    private String createAccountFromUserDetails(UserMerchantDetails userMerchantDetails) throws SQLException {
        String accountNumber;
        Random random = new Random();
        while(true){
            accountNumber = String.valueOf(random.nextLong(1000000000,1100000000));
            String[] banks = {"uba", "gtb", "default"};
            String bank = banks[new Random().nextInt(banks.length)];
            if(this.accountService.createAccount(userMerchantDetails, accountNumber, bank)){
                System.out.println("Your account number is " + accountNumber);
                return accountNumber;
            }
        }
    }
}
