package orderManagement.httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import orderManagement.RunOrderManagement;
import orderManagement.models.entties.OrderItem;
import orderManagement.models.entties.Product;
import orderManagement.models.enums.OrderItemStatus;
import orderManagement.models.responses.UserMerchantDetails;
import orderManagement.models.responses.UserMerchantPlusMessage;
import orderManagement.services.OrderItemService;
import orderManagement.services.ProductService;
import orderManagement.services.UserServiceClient;
import userManagement.RunUserManagement;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MerchantGetOrderItemHandler extends BaseHandler implements HttpHandler {
    private OrderItemService orderItemService;
    public MerchantGetOrderItemHandler(ProductService productService, OrderItemService orderItemService) {
        super(productService);
        this.orderItemService=orderItemService;
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

        Integer orderItemId = null;
        if (params.get("orderItemId") != null && !params.get("orderItemId").isEmpty()) {
            orderItemId = Integer.parseInt(params.get("orderItemId"));
        }

        Integer productId = null;
        if (params.get("productId") != null && !params.get("productId").isEmpty()) {
            productId = Integer.parseInt(params.get("productId"));
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
        //validate user is merchant
        UserMerchantDetails userMerchantDetails = userMerchantPlusMessage.getUserMerchantDetails();
        int merchantId = userMerchantDetails.getMerchantId();
        if (merchantId == 0) {
            RunOrderManagement.writeHttpResponse(exchange, 403, "User is not a merchant and cannot perform this operation");
            return;
        }


        // fetch all products using the merchantIid
        List<Product> products = new ArrayList<>();
        Product product = new Product();
        try {
            products = productService.getProductByMerchantId(merchantId);
        } catch (SQLException e) {
            RunOrderManagement.writeHttpResponse(exchange, 500, "Internal server error");
            return;
        }
        if(products.isEmpty()){
            RunOrderManagement.writeHttpResponse(exchange, 404, "you have no products registered");
            return;
        }
        if(orderItemId!=null){
            OrderItem orderItem = null;
            try {
                orderItem = orderItemService.existingOrderItemByMerchantId(orderItemId, merchantId);
            } catch (SQLException e) {
                System.out.println("Unknown error");
                RunOrderManagement.writeHttpResponse(exchange, 500, "Unknown error");
                return;
            }
            if(orderItem != null){
                String jsonResponse = gson.toJson(orderItem);
                RunOrderManagement.writeHttpResponse(exchange, 200, jsonResponse);
            }else {
                System.out.println("Cannot find item");
                RunOrderManagement.writeHttpResponse(exchange, 404, "Unauthorised! Cannot find item");
            }
        } else {
            //if status is specified check if it is one of PENDING, CONFIRMED, PROCESSING, SHIPPED, COMPLETED OR CANCELLED
            if(statusFromRequest != null){
                try {
                    OrderItemStatus status = OrderItemStatus.valueOf(statusFromRequest.toUpperCase());
                } catch (IllegalArgumentException e) {
                    // invalid status
                    System.out.println("Invalid order item status: " + statusFromRequest);
                    RunOrderManagement.writeHttpResponse(exchange, 400, "Invalid order item status: " + statusFromRequest);
                    return;
                }
            }

            boolean flag = false;
            List<OrderItem> orderItems = new ArrayList<>();

            //if productId is not specified, go ahead and fetch order items
            if(productId == null){
                orderItems= orderItemService.getOrderItemsByFilters(statusFromRequest, null, merchantId);
                if(orderItems == null){
                    RunOrderManagement.writeHttpResponse(exchange, 500, "Internal server error");
                    return;
                }
                if (orderItems.isEmpty()) {
                    RunOrderManagement.writeHttpResponse(exchange, 200, "[]"); // empty list
                } else {
                    String jsonResponse = gson.toJson(orderItems);
                    RunOrderManagement.writeHttpResponse(exchange, 200, jsonResponse);
                }
            } else {
                //check whether product id  specified in request is owned by merchant
                for (Product product1 : products) {
                    if (productId == product1.getId()){
                        flag = true;
                        break;
                    }
                }

                if(!flag){ // product id specified in request is not owned by merchant
                    System.out.println("Forbidden. You are not allowed to query order item of this product " + statusFromRequest);
                    RunOrderManagement.writeHttpResponse(exchange, 403, "Forbidden. You are not allowed to query order item of this product " );
                } else { // product id is owned by merchant go ahead and fetch order items
                    orderItems = orderItemService.getOrderItemsByFilters(statusFromRequest, productId, merchantId);
                    if (orderItems == null) {
                        RunOrderManagement.writeHttpResponse(exchange, 500, "Internal server error");
                        return;
                    }
                    if (orderItems.isEmpty()) {
                        RunOrderManagement.writeHttpResponse(exchange, 200, "[]"); // empty list
                    } else {
                        String jsonResponse = gson.toJson(products);
                        RunOrderManagement.writeHttpResponse(exchange, 200, jsonResponse);
                    }
                }
            }
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

//Get merchant details
//check products that has this merchantId
//list products
//fetch all orderItems where product_id is in (list of product ids)
