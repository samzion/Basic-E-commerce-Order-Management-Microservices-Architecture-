package paymentManagement.httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import oop.SimpleBankRestApiApplication;
import oop.models.entities.Account;
import oop.models.entities.User;
import oop.services.AccountService;
import oop.services.UserService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;

public class AccountCreationHandler extends BaseHandler implements HttpHandler {
    private AccountService accountService;

    public AccountCreationHandler(UserService userService, AccountService accountService){
        super(userService);
        this.accountService = accountService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if(!this.isValidRequestMethod(exchange, "get")) {
            // Handle the request
            String response = "Method not allowed";
            SimpleBankRestApiApplication.writeHttpResponse(exchange, 405, response);
            return;
        }
        User authenticatedUser = this.getAuthenticatedUser(exchange);
        if(authenticatedUser == null) {
            SimpleBankRestApiApplication.writeHttpResponse(exchange, 401, "Unauthorized!");
            return;
        }
        try{
                Account newCreatedAccount =   this.accountService.confirmAccountDetails(createAccountFromUserDetails(authenticatedUser));
                newCreatedAccount.setTransactions(null);
                newCreatedAccount.setLoans(null);
                String jsonResponse = gson.toJson(newCreatedAccount);
                SimpleBankRestApiApplication.writeHttpResponse(exchange, 200, jsonResponse);

        } catch (Exception e) {
            SimpleBankRestApiApplication.writeHttpResponse(exchange, 500, "Unknown error from server");
        }
    }

    private String createAccountFromUserDetails(User existingUser) throws SQLException {
        String accountNumber;
        Random random = new Random();
        while(true){
            accountNumber = String.valueOf(random.nextLong(1000000000,1100000000));
            String[] banks = {"uba", "gtb", "default"};
            String bank = banks[new Random().nextInt(banks.length)];
            if(this.accountService.createAccount(existingUser, accountNumber, bank)){
                System.out.println("Your account number is " + accountNumber);
                return accountNumber;
            }
        }
    }
}
