package orderManagement.httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import orderManagement.RunOrderManagement;
import orderManagement.models.entties.Product;
import orderManagement.services.ProductService;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetProductsByFilterHandler extends BaseHandler implements HttpHandler {


    public GetProductsByFilterHandler(ProductService productService) {
        super(productService);
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
        String category = params.get("category");
        Integer merchantId = params.get("merchantId") != null ? Integer.parseInt(params.get("merchantId")) : null;
        String name = params.get("name");
        Double minPrice = params.get("minPrice") != null ? Double.parseDouble(params.get("minPrice")) : null;
        Double maxPrice = params.get("maxPrice") != null ? Double.parseDouble(params.get("maxPrice")) : null;
        Boolean inStock = params.get("inStock") != null ? Boolean.parseBoolean(params.get("inStock")) : null;

        try {
            List<Product> products = productService.getProductsByFilters(category, merchantId, name, minPrice, maxPrice, inStock);
            if(products == null){
                RunOrderManagement.writeHttpResponse(exchange, 500, "Internal server error");
                return;
            }
            if (products.isEmpty()) {
                RunOrderManagement.writeHttpResponse(exchange, 200, "[]"); // empty list
            } else {
                String jsonResponse = gson.toJson(products);
                RunOrderManagement.writeHttpResponse(exchange, 200, jsonResponse);
            }
        } catch (Exception e) {
            e.printStackTrace();
            RunOrderManagement.writeHttpResponse(exchange, 500, "Internal server error");
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
