package userManagement.httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import userManagement.models.response.UserMerchantDetails;
import userManagement.RunUserManagement;
import userManagement.services.MerchantService;
import userManagement.services.UserService;

import java.io.IOException;

public class UserDetailsByAuth extends BaseHandler implements HttpHandler {


    public UserDetailsByAuth(UserService userService, MerchantService merchantService) {
        super(userService);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if(!"get".equalsIgnoreCase(method)) {
            // Handle the request
            String response = "Method not allowed";
            RunUserManagement.writeHttpResponse(exchange, 405, response);
            return;
        }

        var headers = exchange.getRequestHeaders();
        String authorization = headers.getFirst("Authorization");

        if (authorization == null) {
            RunUserManagement.writeHttpResponse(exchange, 400, "Missing Authorization header");
            return;
        }

        String[] authHeaderArray = authorization.split("/");
        if (authHeaderArray.length != 2) {
            RunUserManagement.writeHttpResponse(exchange, 400, "Invalid Authorization header format");
            return;
        }

        UserMerchantDetails userMerchantDetails = getAllAuthenticatedUserDetails(exchange);
        if(userMerchantDetails.getUserId()==0){
            String jsonResponse = gson.toJson(userMerchantDetails);
            RunUserManagement.writeHttpResponse(exchange, 404, jsonResponse);
            return;
        }
        String jsonResponse = gson.toJson(userMerchantDetails);
        RunUserManagement.writeHttpResponse(exchange, 200, jsonResponse);
    }

}
