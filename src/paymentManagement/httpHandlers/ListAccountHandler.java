package paymentManagement.httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import oop.SimpleBankRestApiApplication;
import oop.models.entities.Account;
import oop.models.entities.User;
import oop.models.response.AccountListResponse;
import oop.services.AccountService;
import oop.services.UserService;

import java.io.IOException;
import java.util.List;

public class ListAccountHandler extends BaseHandler implements HttpHandler {
    private AccountService accountService;

    public ListAccountHandler(UserService userService, AccountService accountService){
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
                List<Account> accounts =   this.accountService.listAccount(authenticatedUser);
                AccountListResponse accountListResponse = new AccountListResponse();
                accountListResponse.setAccounts(accounts);
                String jsonResponse = gson.toJson(accountListResponse);
                SimpleBankRestApiApplication.writeHttpResponse(exchange, 200, jsonResponse);

        } catch (Exception e) {
            SimpleBankRestApiApplication.writeHttpResponse(exchange, 500, "Unknown error from server");
        }
    }

}
