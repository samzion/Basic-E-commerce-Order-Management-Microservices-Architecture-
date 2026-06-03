package orderManagement.httpHandlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import orderManagement.RunOrderManagement;
import orderManagement.models.entties.OrderItem;
import orderManagement.models.entties.Product;
import orderManagement.models.requests.ProcessOrderItemRequest;
import orderManagement.models.responses.UserMerchantDetails;
import orderManagement.models.responses.UserMerchantPlusMessage;
import orderManagement.services.OrderItemService;
import orderManagement.services.ProductService;
import orderManagement.services.UserServiceClient;
import userManagement.RunUserManagement;
import userManagement.utilities.LocalDateTimeAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class ProcessOrderItemHandler extends BaseHandler implements HttpHandler {
    private OrderItemService orderItemService;

    public ProcessOrderItemHandler(ProductService productService, OrderItemService orderItemService) {
        super(productService);
        this.orderItemService = orderItemService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // validate query method
        String method = exchange.getRequestMethod();
        if (!"post".equalsIgnoreCase(method)) {
            // Handle the request
            String response = "Method not allowed";
            RunUserManagement.writeHttpResponse(exchange, 405, response);
            return;
        }
        //validate authorization exist in header
        var headers = exchange.getRequestHeaders();
        // Extract a specific header (case-insensitive)
        String authorization = headers.getFirst("Authorization");
        if (authorization == null) {
            String response = "Unauthorized!";
            RunUserManagement.writeHttpResponse(exchange, 401, response);
            return;
        }
        //validate request is looking good
        String body = "{}";
        try (InputStream input = exchange.getRequestBody()) {
            body = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        ProcessOrderItemRequest processOrderItemRequest = gson.fromJson(body, ProcessOrderItemRequest.class);
        String validationMessage = ProcessOrderItemRequest.validate(processOrderItemRequest);
        if (!validationMessage.equals("Request okay!")) {
            RunUserManagement.writeHttpResponse(exchange, 400, validationMessage);
            return;
        }

        //authenticate user but first fetch data from user mgt. MS
        UserServiceClient userServiceClient = new UserServiceClient();
        UserMerchantPlusMessage userMerchantPlusMessage;
        try {
            userMerchantPlusMessage = userServiceClient.getUserMerchantDetails(exchange);
        } catch (Exception e) {
            RunUserManagement.writeHttpResponse(exchange, 500, "unknown error");
            return;
        }

        if (userMerchantPlusMessage.getErrorMessage() != null) {
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
        int merchantId = userMerchantDetails.getMerchantId();
        if (merchantId == 0) {
            RunOrderManagement.writeHttpResponse(exchange, 403, "User is not a merchant and cannot perform this operation");
            return;
        }
        //fetch product details
        OrderItem orderItem = null;
        try {
            orderItem = orderItemService.existingOrderItem(processOrderItemRequest.getId());
        } catch (SQLException e) {
            RunOrderManagement.writeHttpResponse(exchange, 500, "Unknown error!");
            return;
        }
        if (orderItem == null) {
            RunOrderManagement.writeHttpResponse(exchange, 404, "Order item does not exist!");
            return;
        }
        Product product = new Product();
        try {
            product = productService.existingProduct(orderItem.getProductId());
        } catch (SQLException e) {
            RunOrderManagement.writeHttpResponse(exchange, 500, "Unknown error!");
            return;
        }
        if (!(merchantId == product.getMerchantId())) {
            RunOrderManagement.writeHttpResponse(exchange, 409, "Merchant ID does not match the product's merchant ID");
            return;
        }
        try {
            orderItemService.updateOrderItem(orderItem, processOrderItemRequest.getStatus());
        } catch (SQLException e) {
            RunOrderManagement.writeHttpResponse(exchange, 500, "Unknown error!");
            return;
        }
        RunOrderManagement.writeHttpResponse(exchange, 200, "Order Item status updated successfully!");
    }
}
