package userManagement.httpHandlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import orderManagement.models.responses.UserMerchantDetails;
import userManagement.RunUserManagement;
import userManagement.models.User;
import userManagement.services.UserService;
import userManagement.utilities.LocalDateAdapter;
import userManagement.utilities.LocalDateTimeAdapter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BaseHandler {

    public UserService userService;

    public BaseHandler(UserService userService){
        this.userService = userService;
    }

    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();


    public boolean isValidRequestMethod(HttpExchange exchange, String allowedMethod){
        String method = exchange.getRequestMethod();
        return allowedMethod.equalsIgnoreCase(method);
    }


    public User getAuthenticatedUser(HttpExchange exchange) {
        var headers = exchange.getRequestHeaders();
        // Extract a specific header (case-insensitive)
        String authHeader = headers.getFirst("Authorization");
        if (authHeader == null) {
            return null;
        }
        String[] authHeaderArray = authHeader.split("/");
        if (authHeaderArray.length != 2) {
            return null;
        }
        User existingUser = null;
        try {
            String userToken = authHeaderArray[1];
            existingUser = this.userService.getUserDetailsByUserToken(userToken);

            if (existingUser == null || !existingUser.getEmail().equalsIgnoreCase(authHeaderArray[0])) {
                return null;
            }
            return existingUser;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public UserMerchantDetails getAllAuthenticatedUserDetails(HttpExchange exchange) throws IOException {
        var headers = exchange.getRequestHeaders();
        String authorization = headers.getFirst("Authorization");

        if (authorization == null) {
            RunUserManagement.writeHttpResponse(exchange, 400, "Missing Authorization header");
            return null;
        }

        String[] authHeaderArray = authorization.split("/");
        if (authHeaderArray.length != 2) {
            RunUserManagement.writeHttpResponse(exchange, 400, "Invalid Authorization header format");
            return null;
        }

        try {
            String userToken = authHeaderArray[1];
            UserMerchantDetails userMerchantDetails =
                    this.userService.getUserMerchantDetailsByUserToken(userToken);

            if (userMerchantDetails == null ||
                    !userMerchantDetails.getEmail().equalsIgnoreCase(authHeaderArray[0])) {
                RunUserManagement.writeHttpResponse(exchange, 404, "User details not found");
                return null;
            }

            return userMerchantDetails;

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            RunUserManagement.writeHttpResponse(exchange, 500, "Internal error fetching details");
            return null;
        }
    }
}

