package paymentManagement.httpHandlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import oop.models.entities.User;
import oop.others.LocalDateAdapter;
import oop.others.LocalDateTimeAdapter;
import oop.services.UserService;

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
}

