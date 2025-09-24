package orderManagement.services;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import orderManagement.RunOrderManagement;
import orderManagement.models.responses.UserMerchantDetails;

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






    public UserMerchantDetails getMerchantAuthorization(HttpExchange exchange) throws Exception {
        var headers = exchange.getRequestHeaders();
        String authorization = headers.getFirst("Authorization");

        if (authorization == null) {
            RunOrderManagement.writeHttpResponse (exchange, 401, "Missing Authorization header");
            return null;
        }

        String[] authHeaderArray = authorization.split("/");
        if (authHeaderArray.length != 2) {
            RunOrderManagement.writeHttpResponse(exchange, 400, "Invalid Authorization header format");
            return null;
        }

        UserServiceClient userClient = new UserServiceClient();
        UserMerchantDetails validation = userClient.validateUser(authorization);

        if (validation == null) {
            RunOrderManagement.writeHttpResponse(exchange, 401, "Unauthorized: invalid credentials");
            return null;
        }

        if (validation.getMerchantId() == 0) {
            RunOrderManagement.writeHttpResponse (exchange, 403, "Forbidden: insufficient role");
            return null;
        }
        System.out.println("User authorised to add product");
        return validation; // authorized
    }

        public UserMerchantDetails validateUser(String auth) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getUserByAuthorisationUrl))
                    .header("Authorization", auth) // just forward it
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return UserMerchantDetails.fromJson(response.body());
    }

}