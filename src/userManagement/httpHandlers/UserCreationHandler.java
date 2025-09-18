package userManagement.httpHandlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


import userManagement.RunUserManagement;
import userManagement.models.User;
import userManagement.models.request.UserCreationRequest;
import userManagement.services.UserService;
import userManagement.utilities.LocalDateTimeAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class UserCreationHandler implements HttpHandler {

    private UserService userService;

    public UserCreationHandler(UserService userService){
        this.userService = userService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if(!"post".equalsIgnoreCase(method)) {
            // Handle the request
            String response = "Method not allowed";
            RunUserManagement.writeHttpResponse(exchange, 405, response);
            return;
        }
        String body = "{}";
        try (InputStream input = exchange.getRequestBody()) {
            body =  new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        UserCreationRequest userCreationRequest = gson.fromJson(body, UserCreationRequest.class);
        String validationMessage = UserCreationRequest.validate(userCreationRequest);
        if(!validationMessage.equals("User creation request okay!")){
            RunUserManagement.writeHttpResponse(exchange, 400, validationMessage);
            return;
        }
        User newCreatedUser= null;
        try {

            newCreatedUser = this.userService.createUser(userCreationRequest);
            if(newCreatedUser == null){
                RunUserManagement.writeHttpResponse(exchange, 409, "User already exists");
            } else {
                newCreatedUser.setPasswordHash(null);
                newCreatedUser.setPasswordHash(null);
                String jsonResponse = gson.toJson(newCreatedUser);
                RunUserManagement.writeHttpResponse(exchange, 200, jsonResponse);
            }
        } catch (Exception e) {
            RunUserManagement.writeHttpResponse(exchange, 500, "Unknown error from server");
        }
    }
}