package orderManagement.httpHandlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import orderManagement.RunOrderManagement;
import orderManagement.models.entties.MerchantPayment;
import orderManagement.models.entties.Order;
import orderManagement.models.entties.OrderItem;
import orderManagement.models.entties.Product;
import orderManagement.models.requests.PaymentRequest;
import orderManagement.models.responses.PaymentResponse;
import orderManagement.models.responses.UserMerchantDetails;
import orderManagement.models.responses.UserMerchantPlusMessage;
import orderManagement.requests.PayForItemRequest;
import orderManagement.services.*;
import userManagement.RunUserManagement;
import userManagement.utilities.LocalDateTimeAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PayForItemtHandler extends BaseHandler implements HttpHandler {
    private OrderService orderService;
    private OrderItemService orderItemService;
    public PayForItemtHandler(ProductService productService, OrderService orderService, OrderItemService orderItemService) {
        super(productService);
        this.orderService = orderService;
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
        PayForItemRequest payForItemRequest = gson.fromJson(body, PayForItemRequest.class);
        String validationMessage = PayForItemRequest.validate(payForItemRequest);
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
        //after this line means user authorization is successful
        UserMerchantDetails userMerchantDetails = userMerchantPlusMessage.getUserMerchantDetails();
        int userId = userMerchantDetails.getUserId();

        //check if product exists and stock is greater or equal to quantity to be ordered:
        int productId = payForItemRequest.getProductId();
        int quantityRequested = payForItemRequest.getQuantity();
        Product product;
        try {
            product = productService.ExistingProduct(productId);
        } catch (SQLException e) {
            RunOrderManagement.writeHttpResponse(exchange, 500, "Unknown error!");
            return;
        }
        if (product == null) {
            RunOrderManagement.writeHttpResponse(exchange, 404, "Product does not exist!");
            return;
        }
        if (product.getStock() < quantityRequested) {
            RunOrderManagement.writeHttpResponse(exchange, 409, "Insufficient stock for product");
            return;
        }

        //create order, insert into table
        Order order = orderService.createOrder(userId);
        if (order == null) {
            RunOrderManagement.writeHttpResponse(exchange, 500, "Unknown error!");
            return;
        }
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(order.getId());
        orderItem.setProductId(productId);
        orderItem.setQuantity(quantityRequested);
        orderItem.setPrice(product.getPrice());

        try {
            if (orderItemService.addOrderItem(orderItem).getOrderId() == 0) {
                RunOrderManagement.writeHttpResponse(exchange, 500, "Unknown error!");
                return;
            }
        } catch (SQLException e) {
            RunOrderManagement.writeHttpResponse(exchange, 500, "Unknown error!");
            return;
        }
        //prepare pay load for payment
        List<MerchantPayment> merchantPayments = new ArrayList<>();
        MerchantPayment merchantPayment = new MerchantPayment();
        merchantPayment.setMerchantId(product.getMerchantId());
        merchantPayment.setAmount(product.getPrice() * quantityRequested);
        merchantPayments.add(merchantPayment);
        double totalAmount = 0;
        for (MerchantPayment merchantPayment1 : merchantPayments) {
            totalAmount += merchantPayment1.getAmount();
        }
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setTotalAmount(totalAmount);
        paymentRequest.setMerchantPayments(merchantPayments);

        //call payment service client
        PaymentServiceClient paymentServiceClient = new PaymentServiceClient();
        PaymentResponse paymentResponse = null;
        try {
            paymentResponse = paymentServiceClient.makePayment(authorization, paymentRequest);
        } catch (Exception e) {
            RunOrderManagement.writeHttpResponse(exchange, 500, "Unknown error!");
            return;
        }
        if (paymentResponse.getStatus() != 200) {
            RunOrderManagement.writeHttpResponse(exchange, paymentResponse.getStatus(), paymentResponse.getMessage());
            return;
        }

        //update orderItem status to confirmed
        try {
            orderItemService.updateOrderItem(orderItem);
            orderService.updateOrder(order);
            RunOrderManagement.writeHttpResponse(exchange, paymentResponse.getStatus(), paymentResponse.getMessage());
        } catch (SQLException ex) {
            RunOrderManagement.writeHttpResponse(exchange, 500, "Unknown error! Request for refund if debited");
        }
    }
}
