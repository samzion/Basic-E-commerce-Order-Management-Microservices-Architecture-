package paymentManagement.services;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import paymentManagement.models.response.UserMerchantDetails;
import paymentManagement.models.response.UserMerchantPlusMessage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class UserServiceClient {

    private static String getMerchantUrl;
    private static String getUserByAuthorisationUrl;
    private final HttpClient client;
    private final Gson gson;

    public UserServiceClient() {
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    public static void initialize(String merchantUrl, String userUrl){
        getMerchantUrl = merchantUrl;
        getUserByAuthorisationUrl = userUrl;
    }

    // Response DTO






    public UserMerchantPlusMessage getUserMerchantDetails(HttpExchange exchange) throws Exception {
        var headers = exchange.getRequestHeaders();
        String authorization = headers.getFirst("Authorization");
        UserMerchantPlusMessage userMerchantPlusMessage = new UserMerchantPlusMessage();
        if (authorization == null) {
            String errorMessage = "Unauthorized!";
            userMerchantPlusMessage.setErrorMessage(errorMessage);
            userMerchantPlusMessage.setUserMerchantDetails(null);
            return userMerchantPlusMessage;
        }

        String[] authHeaderArray = authorization.split("/");
        if (authHeaderArray.length != 2) {
            String errorMessage = "Invalid Authorization header format!";
            userMerchantPlusMessage.setErrorMessage(errorMessage);
            userMerchantPlusMessage.setUserMerchantDetails(null);
            return userMerchantPlusMessage;
        }

        UserServiceClient userClient = new UserServiceClient();
        UserMerchantDetails validation = userClient.validateUser(authorization);

        if (validation.getUserId() == 0) {
            String errorMessage = "Unauthorized: invalid credentials";
            userMerchantPlusMessage.setErrorMessage(errorMessage);
            userMerchantPlusMessage.setUserMerchantDetails(null);
            return userMerchantPlusMessage;
        }
        userMerchantPlusMessage.setUserMerchantDetails(validation);
        return userMerchantPlusMessage; // authorized
    }

        public UserMerchantDetails validateUser(String auth) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getUserByAuthorisationUrl))
                .header("Authorization", auth)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return UserMerchantDetails.fromJson(response.body());
    }

}