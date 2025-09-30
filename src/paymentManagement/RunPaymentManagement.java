package paymentManagement;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import paymentManagement.db.DataBaseConnection;
import paymentManagement.db.migrations.MigrationRunner;
import paymentManagement.httpHandlers.AccountCreationHandler;
import paymentManagement.httpHandlers.PayNowHandler;
import paymentManagement.models.bank.*;
import paymentManagement.services.AccountService;
import paymentManagement.services.TransactionService;
import paymentManagement.services.UserServiceClient;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class RunPaymentManagement {
    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        //read from configuration.properties file for all necessary entities to work with

        // Load configuration from file
        Properties props = new Properties();
        props.load(new FileInputStream("paymentManagementConfiguration.properties"));

        String url = props.getProperty("dbUrl");
        String user = props.getProperty("dbUser");
        String password = props.getProperty("dbPassword");
        String driver = props.getProperty("dbDriver");
        String getMerchantUrl = props.getProperty("getMerchantUrl");
        String getUserByAuthorisationUrl = props.getProperty("getUserByAuthorisationUrl");
        String ecommerceAdminAccount = props.getProperty("ecommerceAdminAccount");
        String paymentServiceAdminAccount = props.getProperty("paymentServiceAdminAccount");



        // Initialize once
        DataBaseConnection.initialize(url, user, password, driver);
        UserServiceClient.initialize(getMerchantUrl,getUserByAuthorisationUrl);
        AccountService.initialize(ecommerceAdminAccount, paymentServiceAdminAccount);
        PayNowHandler.initialize(paymentServiceAdminAccount, ecommerceAdminAccount);


        // Now you can call getConnection without arguments
        Connection connection = DataBaseConnection.getConnection();
        System.out.println("Connected successfully: " + connection.getMetaData().getDatabaseProductName() + " - Payment Management DB");


        MigrationRunner migrationRunner = new MigrationRunner();
        migrationRunner.runMigrations(connection);
        try {
            // Create an HttpServer instance
            HttpServer server = HttpServer.create(new InetSocketAddress(8001), 0);

            AccountService accountService = new AccountService();
            TransactionService transactionService =  new TransactionService();


            DefaultTransfer genericTransfer = new DefaultTransfer(accountService);
            GTBTransfer gtbTransfer = new GTBTransfer(accountService);
            UBATransfer ubaTransfer = new UBATransfer(accountService);
            List<ITransfer> genericTransfers = new ArrayList<>();
            genericTransfers.add(gtbTransfer);
            genericTransfers.add(ubaTransfer);
            genericTransfers.add(genericTransfer);
            TransferProcessor transferProcessor = new TransferProcessor(accountService, genericTransfers);

            // Create a context for a specific path and set the handler
            server.createContext("/", new MyHandler());
            server.createContext("/create-account", new AccountCreationHandler(accountService, transactionService));
            server.createContext("/pay-now", new PayNowHandler(transferProcessor, accountService));

            // Start the server
            server.setExecutor(null); // Use the default executor
            server.start();

            System.out.println("Server is running on port 8001");
        } catch (IOException e) {
            System.out.println("Error starting the server: " + e.getMessage());
        }
    }

    // Define a custom HttpHandler
    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();

            if (!"get".equalsIgnoreCase(method)) {
                // Handle the request
                String response = "Method not allowed";
                exchange.sendResponseHeaders(405, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }
            // Handle the request

            String response = "Hello, Payment Mgt. Server response!";
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