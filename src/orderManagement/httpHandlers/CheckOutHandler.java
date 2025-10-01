package orderManagement.httpHandlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import orderManagement.RunOrderManagement;
import orderManagement.models.entties.Cart;
import orderManagement.models.entties.MerchantPayment;
import orderManagement.models.entties.Order;
import orderManagement.models.requests.CheckOutRequest;
import orderManagement.models.requests.PaymentRequest;
import orderManagement.models.responses.PaymentResponse;
import orderManagement.models.responses.UserMerchantDetails;
import orderManagement.models.responses.UserMerchantPlusMessage;
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

public class CheckOutHandler extends BaseHandler implements HttpHandler {
    private OrderService orderService;
    private OrderItemService orderItemService;
    private CartService cartService;
    private CartItemService cartItemService;

    public CheckOutHandler(ProductService productService, OrderService orderService
            , OrderItemService orderItemService, CartService cartService, CartItemService cartItemService) {
        super(productService);
        this.orderService = orderService;
        this.orderItemService = orderItemService;
        this.cartService = cartService;
        this.cartItemService = cartItemService;
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
        CheckOutRequest checkOutRequest = gson.fromJson(body, CheckOutRequest.class);
        String validationMessage = CheckOutRequest.validate(checkOutRequest);
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

        //Check if cartId exist and active and tallies with userId
        Cart cart = null;
        try {
            cart = cartService.getCartDetails(userId, checkOutRequest.getCartId());
        } catch (SQLException e) {
            RunOrderManagement.writeHttpResponse(exchange, 500, "Unknown error!");
            return;
        }
        if(cart == null){
            RunOrderManagement.writeHttpResponse(exchange, 500, "Unknown error!");
            return;
        }
        if(cart.getId() == 0){
            RunOrderManagement.writeHttpResponse(exchange, 404, "Cart does not exit");
            return;
        }
        // After here it means we have an active cart id that matches userId
        //Now checkout of cart and create orderItems and new order

        if(!cartService.checkOutCart(cart.getId())){
            RunOrderManagement.writeHttpResponse(exchange, 500, "Unknown error!");
            return;
        }

        Order order = orderService.createOrder(userId);
        if(order == null){
            RunOrderManagement.writeHttpResponse(exchange, 500, "Unknown error!");
            return;
        }

        if(!orderItemService.copyCartItemsToOrderItems(order.getId(), cart.getId())){
            RunUserManagement.writeHttpResponse(exchange, 500, "Unknown error!");
            return;
        }
        List<MerchantPayment> merchantPayments =  new ArrayList<>();
        try {
           merchantPayments = cartItemService.getMerchantPayments(cart.getId());
        } catch (SQLException e) {
            RunUserManagement.writeHttpResponse(exchange, 500, "Unknown error!");
            return;
        }
        if(merchantPayments.isEmpty()){
            RunUserManagement.writeHttpResponse(exchange, 500, "Unknown error!");
            return;
        }
        double totalAmount= 0;
        for(MerchantPayment merchantPayment: merchantPayments){
            totalAmount+=merchantPayment.getAmount();
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
            orderItemService.confirmOrderItems(order.getId());
            orderService.updateOrder(order);
            RunOrderManagement.writeHttpResponse(exchange, paymentResponse.getStatus(), paymentResponse.getMessage());
        } catch (SQLException ex) {
            RunOrderManagement.writeHttpResponse(exchange, 500, "Unknown error! Request for refund if debited");
        }
    }
}