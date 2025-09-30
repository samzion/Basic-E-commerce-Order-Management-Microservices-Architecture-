package userManagement.httpHandlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import userManagement.RunUserManagement;
import userManagement.models.Merchant;
import userManagement.models.Role;
import userManagement.models.request.MerchantCreationRequest;
import userManagement.models.response.UserMerchantDetails;
import userManagement.services.MerchantService;
import userManagement.services.UserService;
import userManagement.utilities.LocalDateTimeAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class MerchantCreationHandler extends BaseHandler implements HttpHandler {

    public MerchantService merchantService;

    public MerchantCreationHandler(UserService userService, MerchantService merchantService) {
        super(userService);
        this.merchantService = merchantService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if(!"post".equalsIgnoreCase(method)) {
            // Handle the request
            String response = "Method not allowed";
            RunUserManagement.writeHttpResponse(exchange, 405, response);
            return;
        }
        String body = "{}";
        try (InputStream input = exchange.getRequestBody()) {
            body =  new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        MerchantCreationRequest merchantCreationRequest = gson.fromJson(body, MerchantCreationRequest.class);
        String validationMessage = MerchantCreationRequest.validate(merchantCreationRequest);
        if(!validationMessage.equals("Merchant creation request okay!")){
            RunUserManagement.writeHttpResponse(exchange, 400, validationMessage);
            return;
        }

        //validate if the request sender is a registered user
        UserMerchantDetails userMerchantDetails = this.getAllAuthenticatedUserDetails(exchange);
        if(userMerchantDetails.getUserId() == 0) {
            RunUserManagement.writeHttpResponse(exchange, 401, "Unauthorized!");
            return;
        }
        if(userMerchantDetails.getRole() == Role.MERCHANT
                && userMerchantDetails.getBusinessName().equalsIgnoreCase(merchantCreationRequest.getBusinessName())) {
            RunUserManagement.writeHttpResponse(exchange, 409, "You are already an existing merchant with this business name");
            return;
        }
        try{
            int user_id = userMerchantDetails.getUserId();
            Merchant newCreatedMerchant =  new Merchant(merchantCreationRequest, user_id);
            merchantService.createMerchant(newCreatedMerchant);
            userService.updateRole(userMerchantDetails, Role.MERCHANT);
            String jsonResponse = gson.toJson(newCreatedMerchant);
            RunUserManagement.writeHttpResponse(exchange, 200, jsonResponse);

        } catch (Exception e) {
            RunUserManagement.writeHttpResponse(exchange, 500, "Unknown error from server");
        }
    }

}
