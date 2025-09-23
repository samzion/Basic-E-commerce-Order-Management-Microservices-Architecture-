package paymentManagement.httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import oop.SimpleBankRestApiApplication;
import oop.models.entities.User;
import oop.models.requests.DepositRequest;
import oop.models.response.AccountOperationResponse;
import oop.services.AccountService;
import oop.services.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class DepositHandler  extends BaseHandler implements HttpHandler {
    public AccountService accountService;

    public DepositHandler(UserService userService, AccountService accountService){
        super(userService);
        this.accountService = accountService;
    }
    public void handle (HttpExchange exchange) throws IOException {
        if(!this.isValidRequestMethod(exchange, "post")) {
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
        String body = "{}";
        try (InputStream input = exchange.getRequestBody()) {
            body =  new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        DepositRequest depositRequest = gson.fromJson(body, DepositRequest.class);
        String validationMessage = DepositRequest.validate(depositRequest);
        if(!validationMessage.equals("Deposit request okay!")){
            SimpleBankRestApiApplication.writeHttpResponse(exchange, 400, validationMessage);
            return;
        }
        try{
            AccountOperationResponse depositResponse =this.accountService.deposit(authenticatedUser, depositRequest.getAccountNumber()
                    , depositRequest.getDepositAmount());
            SimpleBankRestApiApplication.writeHttpResponse(exchange, depositResponse.getStatusCode(), depositResponse.getMessage());

        } catch (Exception e) {
            SimpleBankRestApiApplication.writeHttpResponse(exchange, 500, "Unknown error from server");
        }
    }
}
