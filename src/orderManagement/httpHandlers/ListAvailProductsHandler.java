package orderManagement.httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import orderManagement.models.entties.Product;
import orderManagement.services.ProductService;
import userManagement.RunUserManagement;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ListAvailProductsHandler extends BaseHandler implements HttpHandler {


    public ListAvailProductsHandler(ProductService productService) {
        super(productService);
    }
    // validate request is a  get method
    // no authorization needed fetch available products
    //send response
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if(!"get".equalsIgnoreCase(method)) {
            // Handle the request
            String response = "Method not allowed";
            RunUserManagement.writeHttpResponse(exchange, 405, response);
            return;
        }
        try {
            List<Product> products = productService.listAllAvailProducts();
            for(Product product : products){
                product.setCreatedOn(null);
                product.setUpdatedOn(null);
            }
            String jsonResponse = gson.toJson(products);
            RunUserManagement.writeHttpResponse(exchange, 200, jsonResponse);
        } catch (SQLException e) {
            RunUserManagement.writeHttpResponse(exchange, 500, "Unknown error");
            return;
        }



    }
}
