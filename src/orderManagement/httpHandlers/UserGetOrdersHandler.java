package orderManagement.httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import orderManagement.RunOrderManagement;
import orderManagement.models.entties.Order;
import orderManagement.models.responses.UserMerchantDetails;
import orderManagement.models.responses.UserMerchantPlusMessage;
import orderManagement.services.OrderService;
import orderManagement.services.ProductService;
import orderManagement.services.UserServiceClient;
import userManagement.RunUserManagement;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserGetOrdersHandler extends  BaseHandler implements HttpHandler {
    private OrderService orderService;
    public UserGetOrdersHandler(ProductService productService, OrderService orderService) {
        super(productService);
        this.orderService= orderService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            RunOrderManagement.writeHttpResponse(exchange, 405, "Method not allowed");
            return;
        }

        // Extract query params
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = parseQueryParams(query);

        // Convert params to typed values
        String statusFromRequest = params.get("status");

        Integer orderId = null;
        if (params.get("orderId") != null && !params.get("orderId").isEmpty()) {
            orderId = Integer.parseInt(params.get("orderId"));
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
        //after this line means user authorization is successful
        UserMerchantDetails userMerchantDetails = userMerchantPlusMessage.getUserMerchantDetails();
        int userId = userMerchantDetails.getUserId();
        List<Order> orders = null;
        if (orderId != null) {
            orders = orderService.getOrders(orderId, statusFromRequest, userId);
            if(orders.isEmpty()){
                System.out.println("Cannot find order");
                RunOrderManagement.writeHttpResponse(exchange, 404, "Cannot find order.");
                return;
            }else {
                String jsonResponse = gson.toJson(orders);
                RunOrderManagement.writeHttpResponse(exchange, 200, jsonResponse);
                return;
            }

        }
        //if status is specified check if it is either PENDING or CONFIRMED
        if(statusFromRequest != null){
            if(!(statusFromRequest.equalsIgnoreCase("PENDING") || statusFromRequest.equalsIgnoreCase("CONFIRMED") || statusFromRequest.equalsIgnoreCase("CANCELLED"))){
                // invalid status
                System.out.println("Invalid order status: " + statusFromRequest);
                RunOrderManagement.writeHttpResponse(exchange, 400, "Invalid order status: " + statusFromRequest);
                return;
            }
        }
        orders = orderService.getOrders(orderId, statusFromRequest, userId);
        if(orders == null){
            RunOrderManagement.writeHttpResponse(exchange, 500, "Internal server error");
            return;
        }
        if (orders.isEmpty()) {
            RunOrderManagement.writeHttpResponse(exchange, 200, "[]"); // empty list
        } else {
            String jsonResponse = gson.toJson(orders);
            RunOrderManagement.writeHttpResponse(exchange, 200, jsonResponse);
        }



    }
    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) return params;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                params.put(
                        URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8)
                );
            }
        }
        return params;
    }
}
