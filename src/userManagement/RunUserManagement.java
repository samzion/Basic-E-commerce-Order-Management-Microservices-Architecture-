package userManagement;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import userManagement.db.DataBaseConnection;
import userManagement.db.migrations.MigrationRunner;
import userManagement.httpHandlers.MerchantCreationHandler;
import userManagement.httpHandlers.UserCreationHandler;
import userManagement.httpHandlers.UserLoginHandler;
import userManagement.services.MerchantService;
import userManagement.services.UserService;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class RunUserManagement {
    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        //read from configuration.properties file for all necessary entities to work with

        // Load configuration from file
        Properties props = new Properties();
        props.load(new FileInputStream("userManagementConfiguration.properties"));
//TODO: move configuration inside package
        String url = props.getProperty("dbUrl");
        String user = props.getProperty("dbUser");
        String password = props.getProperty("dbPassword");
        String driver = props.getProperty("dbDriver");


        // Initialize once
        DataBaseConnection.initialize(url, user, password, driver);

        // Now you can call getConnection without arguments
        Connection connection = DataBaseConnection.getConnection();
        System.out.println("Connected successfully: " + connection.getMetaData().getDatabaseProductName() + " - User Management DB");

        MigrationRunner migrationRunner = new MigrationRunner();
        migrationRunner.runMigrations(connection);



        try {
            // Create an HttpServer instance
            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

            UserService userService = new UserService(connection);
            MerchantService merchantService = new MerchantService(connection);


            // Create a context for a specific path and set the handler
            server.createContext("/", new DefaultHandler());
            //TODO: Create a landing page path called homeHandler to return all the APIs that is supported.
            server.createContext("/create-user", new UserCreationHandler(userService));
            server.createContext("/user-login", new UserLoginHandler(userService));
            server.createContext("/create-merchant", new MerchantCreationHandler(userService, merchantService));


            // Start the server
            server.setExecutor(null); // Use the default executor
            server.start();

            System.out.println("Server is running on port 8000");
        } catch (IOException e) {
            System.out.println("Error starting the server: " + e.getMessage());
        }
    }

    // Define a Default HttpHandler
    static class DefaultHandler implements HttpHandler {
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
