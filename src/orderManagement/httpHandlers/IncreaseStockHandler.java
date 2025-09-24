package orderManagement.httpHandlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import orderManagement.RunOrderManagement;
import orderManagement.models.entties.Product;
import orderManagement.models.responses.UserMerchantDetails;
import orderManagement.models.responses.UserMerchantPlusMessage;
import orderManagement.requests.IncreaseStockRequest;
import orderManagement.services.ProductService;
import orderManagement.services.UserServiceClient;
import userManagement.RunUserManagement;
import userManagement.models.Role;
import userManagement.utilities.LocalDateTimeAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class IncreaseStockHandler extends BaseHandler implements HttpHandler {
    public IncreaseStockHandler(ProductService productService) {
        super(productService);
    }
    // 1. validate method is POST else return 405
    // 2. validate if request has authorization in header else return 401
    // 3. validate request is good else return 400
    // 4. Call UserServiceClient to fetch all user details
        //if fetched user details is null return 401 else move to 5
    // 5. Is Role is CUSTOMER then return 401 else move to 6
    // 6. Confirm if product exists
        // if it does not return 404 else do 7
    // 7. update stock.
        //if successful return 200 else return 500
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        //1.
        String method = exchange.getRequestMethod();
        if(!"post".equalsIgnoreCase(method)) {
            // Handle the request
            String response = "Method not allowed";
            RunUserManagement.writeHttpResponse(exchange, 405, response);
            return;
        }

        //2.
        var headers = exchange.getRequestHeaders();
        // Extract a specific header (case-insensitive)
        String authorization = headers.getFirst("Authorization");
        if (authorization == null) {
            String response = "Unauthorized!";
            RunUserManagement.writeHttpResponse(exchange, 401, response);
            return;
        }

        //3.

        String body = "{}";
        try (InputStream input = exchange.getRequestBody()) {
            body =  new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        IncreaseStockRequest increaseStockRequest = gson.fromJson(body, IncreaseStockRequest.class);
        String validationMessage = IncreaseStockRequest.validate(increaseStockRequest);
        if(!validationMessage.equals("Request okay!")){
            RunUserManagement.writeHttpResponse(exchange, 400, validationMessage);
            return;
        }

        // 4
        UserServiceClient userServiceClient = new UserServiceClient();
        UserMerchantPlusMessage userMerchantPlusMessage;
        try {
            userMerchantPlusMessage = userServiceClient.getUserMerchantDetails( exchange);
        } catch (Exception e) {
            RunUserManagement.writeHttpResponse(exchange, 500, "unknown error");
            return;
        }

        if(userMerchantPlusMessage.getErrorMessage() != null) {
            String errorMessage = userMerchantPlusMessage.getErrorMessage();
            if (errorMessage.equals("Unauthorized!")) {
                RunOrderManagement.writeHttpResponse(exchange, 401, errorMessage);
            }
            if (errorMessage.equals("Invalid Authorization header format!")) {
                RunOrderManagement.writeHttpResponse(exchange, 400, errorMessage);
            }
            if (errorMessage.equals("Unauthorized: invalid credentials")) {
                RunOrderManagement.writeHttpResponse(exchange, 401, errorMessage);
                return;
            }
        }
        UserMerchantDetails userMerchantDetails = userMerchantPlusMessage.getUserMerchantDetails();
        if (userMerchantDetails.getMerchantId() == 0 || userMerchantDetails.getRole() == Role.CUSTOMER) {
            RunOrderManagement.writeHttpResponse (exchange, 403, "Forbidden: insufficient role");
            return;
        }
        // extract merchant id
        int merchant_id = userMerchantDetails.getMerchantId();

        // Confirm whether product exist and
        // confirm whether merchantID matches merchant_id in products table
        Product existinProduct;
        try {
            existinProduct = productService.ExistingProduct(increaseStockRequest.getProductId());
        } catch (SQLException e) {
            RunOrderManagement.writeHttpResponse (exchange, 500, "Unknown error");
            return;
        }
        if(existinProduct == null){
            RunOrderManagement.writeHttpResponse (exchange, 404, "Product not found!");
            return;
        }
        if(merchant_id != existinProduct.getMerchantId()){
            RunOrderManagement.writeHttpResponse (exchange, 403, "Forbidden: You are not allowed to update this product");
            return;
        }
        // update database
        if(productService.increaseStock(increaseStockRequest.getProductId(), increaseStockRequest.getQuantity())){
            RunUserManagement.writeHttpResponse(exchange, 200, "Stock updated successfully!");
            return;
        }
        String response = "Unknown error";
        RunUserManagement.writeHttpResponse(exchange, 500, response);
    }
}
