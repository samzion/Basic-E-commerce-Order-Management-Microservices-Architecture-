package orderManagement.httpHandlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import orderManagement.RunOrderManagement;
import orderManagement.models.entties.Cart;
import orderManagement.models.entties.CartItem;
import orderManagement.models.entties.Product;
import orderManagement.models.requests.AddItemToCartRequest;
import orderManagement.models.responses.UserMerchantDetails;
import orderManagement.models.responses.UserMerchantPlusMessage;
import orderManagement.services.CartItemService;
import orderManagement.services.CartService;
import orderManagement.services.ProductService;
import orderManagement.services.UserServiceClient;
import userManagement.RunUserManagement;
import userManagement.utilities.LocalDateTimeAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class AddItemToCartHandler extends BaseHandler implements HttpHandler {
    CartService cartService;
    CartItemService cartItemService;
    public AddItemToCartHandler(ProductService productService, CartService cartService, CartItemService cartItemService) {
        super(productService);
        this.cartService = cartService;
        this.cartItemService = cartItemService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // validate query method
        String method = exchange.getRequestMethod();
        if(!"post".equalsIgnoreCase(method)) {
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
            body =  new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        AddItemToCartRequest addItemToCartRequest = gson.fromJson(body, AddItemToCartRequest.class);
        String validationMessage = AddItemToCartRequest.validate(addItemToCartRequest);
        if(!validationMessage.equals("Request okay!")){
            RunUserManagement.writeHttpResponse(exchange, 400, validationMessage);
            return;
        }

        //authenticate user but first fetch data from user mgt. MS
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
        //after this line means user authorization is successful
        UserMerchantDetails userMerchantDetails = userMerchantPlusMessage.getUserMerchantDetails();
        Cart cart = new Cart();
        int userId = userMerchantDetails.getUserId();
        //TODO: check if cart_id specified in request is found in database continue to create cart if null
        int specifiedCardId = addItemToCartRequest.getCartId();
        if(specifiedCardId >0){
            try {
                cart = cartService.getCartDetails(userId, specifiedCardId);
                if(!cart.getStatus().equals("OPEN")){
                    RunOrderManagement.writeHttpResponse(exchange, 400, "Cart no longer active");
                    return;
                }
            } catch (SQLException e) {
                RunOrderManagement.writeHttpResponse(exchange, 500, "Unknown error!");
                return;
            }
        }else {
            try {
                cart = cartService.getActiveCartDetails(userId);
            } catch (SQLException e) {
                RunOrderManagement.writeHttpResponse(exchange, 500, "Unknown error!");
                return;
            }
            if(cart.getId() ==0){
                //an active cart is not available so create one
                cart = cartService.createCart(userId);
                if(cart == null){
                    RunOrderManagement.writeHttpResponse(exchange, 500, "Unknown error!");
                    return;
                }
            }
        }


        //active cart exist now. So validate whether product exist and quantity requested is available
        int productId = addItemToCartRequest.getProductId();
        int quantityRequested = addItemToCartRequest.getQuantity();
        Product product;
        try {
            product =  productService.ExistingProduct(productId);
        } catch (SQLException e) {
            RunOrderManagement.writeHttpResponse(exchange, 500, "Unknown error!");
            return;
        }
        if(product == null){
            RunOrderManagement.writeHttpResponse(exchange, 404, "Product does not exist!");
            return;
        }
        if(product.getStock() < quantityRequested){
            RunOrderManagement.writeHttpResponse(exchange, 409, "Insufficient stock for product");
            return;
        }

        //add cartItem to cart
        //but check whether item exist
        CartItem cartItem = cartItemService.getCartItem(cart.getId(), productId);
        if(cartItem.getId() >0){
            RunOrderManagement.writeHttpResponse(exchange, 409, "Item already exist in cart");
            return;
        }
        cartItem.setCartId(cart.getId());
        cartItem.setQuantity(quantityRequested);
        cartItem.setPriceAtAdd(product.getPrice());
        cartItem.setProductId(productId);
        try {
            if(cartItemService.addCartItem(cartItem)){
                //Yes! successful!!!
                RunOrderManagement.writeHttpResponse(exchange, 200, "Item added to cart successful!");
                return;
            }
            RunOrderManagement.writeHttpResponse(exchange, 500, "Unknown error");
        } catch (SQLException e) {
            RunOrderManagement.writeHttpResponse(exchange, 500, "Unknown error");
        }
    }
}
//TODO: this handler should focus on adding alone. If item exist in cart_items return item already exist.