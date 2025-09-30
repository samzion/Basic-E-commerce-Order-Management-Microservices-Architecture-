package orderManagement;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import orderManagement.db.migrations.MigrationRunner;
import orderManagement.httpHandlers.*;
import orderManagement.services.CartItemService;
import orderManagement.services.CartService;
import orderManagement.services.ProductService;
import orderManagement.services.UserServiceClient;
import userManagement.db.DataBaseConnection;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class RunOrderManagement {
    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        //read from configuration.properties file for all necessary entities to work with

        // Load configuration from file
        Properties props = new Properties();
        props.load(new FileInputStream("orderManagementConfiguration.properties"));

        String url = props.getProperty("dbUrl");
        String user = props.getProperty("dbUser");
        String password = props.getProperty("dbPassword");
        String driver = props.getProperty("dbDriver");
        String getMerchantUrl = props.getProperty("getMerchantUrl");
        String getUserByAuthorisationUrl = props.getProperty("getUserByAuthorisationUrl");


        // Initialize once
        DataBaseConnection.initialize(url, user, password, driver);
        UserServiceClient.initialize(getMerchantUrl, getUserByAuthorisationUrl);

        // Now you can call getConnection without arguments
        Connection connection = DataBaseConnection.getConnection();
        System.out.println("Connected successfully: " + connection.getMetaData().getDatabaseProductName() + " - Order Management DB");

        MigrationRunner migrationRunner = new MigrationRunner();
        migrationRunner.runMigrations(connection);
        try {
            // Create an HttpServer instance
            HttpServer server = HttpServer.create(new InetSocketAddress(8002), 0);

           ProductService productService =  new ProductService(connection);
            CartService cartService = new CartService(connection);
            CartItemService cartItemService = new CartItemService(connection);


            // Create a context for a specific path and set the handler
            server.createContext("/", new DefaultHandler());
            //TODO: Create a landing page path called homeHandler to return all the APIs that is supported.
            server.createContext("/insert-product", new InsertProductHandler(productService));
            server.createContext("/reduce-stock", new ReduceStockHandler(productService));
            server.createContext("/increase-stock", new IncreaseStockHandler(productService));
            server.createContext("/all-products", new ListAvailProductsHandler(productService));
            server.createContext("/products", new GetProductsByFilterHandler(productService));
            server.createContext("/add-to-cart", new AddItemToCartHandler(productService, cartService, cartItemService));
            server.createContext("/pay-for-this-item", new PayForItemtHandler(productService, cartService, cartItemService));



            // Start the server
            server.setExecutor(null); // Use the default executor
            server.start();

            System.out.println("Server is running on port 8002");
        } catch (IOException e) {
            System.out.println("Error starting the server: " + e.getMessage());
        }

    }

    // Define a Default HttpHandler
    public static class DefaultHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            String method = exchange.getRequestMethod();

            if(!"get".equalsIgnoreCase(method)) {
                // Handle the request
                String response = "Method not allowed";
                exchange.sendResponseHeaders(405, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }
            // Handle the request

            String response = """
                    Hello there!
                    Welcome to Blake Ecommerce site
                    """;
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
    public static void writeHttpResponse(HttpExchange exchange, int statusCode, String responseMessage) throws IOException {
        // Handle the request
        exchange.sendResponseHeaders(statusCode, responseMessage.length());
        OutputStream os = exchange.getResponseBody();
        os.write(responseMessage.getBytes());
        os.close();
    }
}
