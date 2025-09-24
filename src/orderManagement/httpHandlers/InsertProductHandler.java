package orderManagement.httpHandlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import orderManagement.RunOrderManagement;
import orderManagement.models.entties.Product;
import orderManagement.models.responses.UserMerchantDetails;
import orderManagement.requests.InsertProductRequest;
import orderManagement.services.ProductService;
import orderManagement.services.UserServiceClient;
import userManagement.RunUserManagement;
import userManagement.utilities.LocalDateTimeAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class InsertProductHandler extends BaseHandler implements HttpHandler {

    public InsertProductHandler(ProductService productService) {
        super(productService);
    }

    // 1. validate method is POST
    // 2. validate if request has authorization in header
    // 3. fetch User details join on merchant details by calling user mgt. MS
          // if User id is null return 404 else go to 4.
    // 4. check for merchant details
       // if merchant id is null return 404
    //5 Get Product object from request
    //6 confirm product name and merchant id does not exist in DB
        //if it exists return 409 + response "conflict"
    //7. save to database and return 200 to user

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
        //validate if request has authorization in header
        var headers = exchange.getRequestHeaders();
        // Extract a specific header (case-insensitive)
        String authorization = headers.getFirst("Authorization");
        if (authorization == null) {
            String response = "Unauthorized!";
            RunUserManagement.writeHttpResponse(exchange, 401, response);
            return;
        }

        // validate insert product request
        String body = "{}";
        try (InputStream input = exchange.getRequestBody()) {
            body =  new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        InsertProductRequest insertProductRequest = gson.fromJson(body, InsertProductRequest.class);
        String validationMessage = InsertProductRequest.validate(insertProductRequest);
        if(!validationMessage.equals("Insert product request okay!")){
            RunUserManagement.writeHttpResponse(exchange, 400, validationMessage);
            return;
        }

        //fetch User + assumed merchant details
        UserServiceClient userServiceClient = new UserServiceClient();
        UserMerchantDetails userMerchantDetails = null;
        try {
           userMerchantDetails = userServiceClient.getMerchantAuthorization( exchange);
        } catch (Exception e) {
            RunUserManagement.writeHttpResponse(exchange, 500, "unknown error");
            return;
        }

        if(userMerchantDetails == null){
            String response = "Unauthorized to perform this task";
            RunUserManagement.writeHttpResponse(exchange, 401, response);
            return;
        }
        // extract merchant id
        int merchant_id = userMerchantDetails.getMerchantId();


        // create object of product
        Product product = new Product(insertProductRequest, merchant_id);

        //check if product exist in database by name and merchantId
        try {
            if(productService.getProductByNameAndMerchant(product.getName(), product.getMerchantId()) != null){
                String response = "Duplicate entry";
                RunOrderManagement.writeHttpResponse(exchange,409, response );
                return;
            }
        } catch (SQLException e) {
            String response = "Unknown error";
            RunUserManagement.writeHttpResponse(exchange, 500, response);
            return;
        }
        //insert in database
        try {
            Product updatedProduct = productService.insertProduct(product);
            String jsonResponse = gson.toJson(updatedProduct);
            RunUserManagement.writeHttpResponse(exchange, 200, jsonResponse);
        } catch (SQLException e) {
            String response = "Unknown error";
            RunUserManagement.writeHttpResponse(exchange, 500, response);
        }


    }
}
