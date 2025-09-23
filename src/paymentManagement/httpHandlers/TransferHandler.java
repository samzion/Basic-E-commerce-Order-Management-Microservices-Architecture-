package paymentManagement.httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import oop.SimpleBankRestApiApplication;
import oop.TransferProcessor;
import oop.models.entities.User;
import oop.models.requests.TransferRequest;
import oop.models.response.AccountOperationResponse;
import oop.services.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TransferHandler extends BaseHandler implements HttpHandler {
    private UserService userService;
    private TransferProcessor transferProcessor;

    public TransferHandler(UserService userService, TransferProcessor transferProcessor){
        super(userService);
        this.transferProcessor = transferProcessor;
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
        TransferRequest transferRequest = gson.fromJson(body, TransferRequest.class);
        String validationMessage = TransferRequest.validate(transferRequest);
        if(!validationMessage.equals("Transfer request okay!")){
            SimpleBankRestApiApplication.writeHttpResponse(exchange, 400, validationMessage);
            return;
        }
        try{
            AccountOperationResponse transferResponse = transferProcessor.transfer(authenticatedUser, transferRequest.getSourceAccountNumber(), transferRequest.getDestinationAccountNumber(),transferRequest.getAmount());
            SimpleBankRestApiApplication.writeHttpResponse(exchange, transferResponse.getStatusCode(), transferResponse.getMessage());

        } catch (Exception e) {
            SimpleBankRestApiApplication.writeHttpResponse(exchange, 500, "Unknown error from server");
        }
    }
}
