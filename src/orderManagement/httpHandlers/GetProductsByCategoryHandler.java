package orderManagement.httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import orderManagement.RunOrderManagement;
import orderManagement.models.entties.Product;
import orderManagement.services.ProductService;

import java.io.IOException;
import java.util.List;

public class GetProductsByCategoryHandler extends BaseHandler implements HttpHandler {


    public GetProductsByCategoryHandler(ProductService productService) {
        super(productService);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String method = exchange.getRequestMethod();

        if (!"GET".equalsIgnoreCase(method)) {
            RunOrderManagement.writeHttpResponse(exchange, 405, "Method not allowed");
            return;
        }

        // Extract category from query params
        String query = exchange.getRequestURI().getQuery();
        String category = null;
        if (query != null && query.startsWith("category=")) {
            category = query.split("=")[1];
        }

        if (category == null || category.isBlank()) {
            RunOrderManagement.writeHttpResponse(exchange, 400, "Category is required");
            return;
        }

        try {
            List<Product> products = productService.getProductsByCategory(category);

            if (products.isEmpty()) {
                RunOrderManagement.writeHttpResponse(exchange, 200, "[]");
                return;
            }
            for(Product product : products) {
                product.setCreatedOn(null);
                product.setUpdatedOn(null);
            }
            String jsonResponse = gson.toJson(products);
            RunOrderManagement.writeHttpResponse(exchange, 200, jsonResponse);

        } catch (Exception e) {
            e.printStackTrace();
           RunOrderManagement.writeHttpResponse(exchange, 500, "Internal Server Error");
        }
    }
}
