package userManagement.httpHandlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import userManagement.RunUserManagement;
import userManagement.models.User;
import userManagement.models.request.UserLoginRequest;
import userManagement.services.UserService;
import userManagement.utilities.LocalDateTimeAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

public class UserLoginHandler implements HttpHandler {
    private UserService userService;

    public UserLoginHandler( UserService userService){
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
        UserLoginRequest userLoginRequest = gson.fromJson(body, UserLoginRequest.class);
        String validationMessage = UserLoginRequest.validate(userLoginRequest);
        if(!validationMessage.equals("User login request okay!")){
            RunUserManagement.writeHttpResponse(exchange, 400, validationMessage);
            return;
        }
        User existingUser;
        try {
            existingUser = this.userService.confirmUserLoginDetails(userLoginRequest.getEmail(), userLoginRequest.getPassword());

            if(existingUser == null){
                RunUserManagement.writeHttpResponse(exchange, 401, "Incorrect login details");
            }else {
                String uuid = UUID.randomUUID().toString().replace("-", "");
                existingUser.setUserToken(uuid);
                this.userService.updateToken(existingUser);
                existingUser.setPasswordHash(null);
                String jsonResponse = gson.toJson(existingUser);
                RunUserManagement.writeHttpResponse(exchange, 200, jsonResponse);
            }
        } catch (Exception e) {
            RunUserManagement.writeHttpResponse(exchange, 500, "Unknown error from server");
        }
    }
}