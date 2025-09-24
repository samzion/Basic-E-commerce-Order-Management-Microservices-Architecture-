package orderManagement.httpHandlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import orderManagement.services.ProductService;
import userManagement.utilities.LocalDateAdapter;
import userManagement.utilities.LocalDateTimeAdapter;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BaseHandler {

    public ProductService productService;

    public BaseHandler(ProductService productService){
        this.productService = productService;
    }

    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();


    public boolean isValidRequestMethod(HttpExchange exchange, String allowedMethod){
        String method = exchange.getRequestMethod();
        return allowedMethod.equalsIgnoreCase(method);
    }
}
