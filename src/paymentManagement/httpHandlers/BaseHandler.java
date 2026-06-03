package paymentManagement.httpHandlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import paymentManagement.utilities.LocalDateAdapter;
import paymentManagement.utilities.LocalDateTimeAdapter;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BaseHandler {

    public BaseHandler(){
    }

    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            . registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();


    public boolean isValidRequestMethod(HttpExchange exchange, String allowedMethod){
        String method = exchange.getRequestMethod();
        return allowedMethod.equalsIgnoreCase(method);
    }
}

