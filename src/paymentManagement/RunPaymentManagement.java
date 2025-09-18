package paymentManagement;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import paymentManagement.db.migrations.MigrationRunner;
import paymentManagement.httpHandlers.AccountCreationHandler;
import paymentManagement.httpHandlers.DepositHandler;
import paymentManagement.httpHandlers.ListAccountHandler;
import paymentManagement.httpHandlers.TransferHandler;
import paymentManagement.models.bankTransfers.*;
import paymentManagement.services.AccountService;
import paymentManagement.services.LoanService;
import userManagement.db.DataBaseConnection;

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


        // Initialize once
        DataBaseConnection.initialize(url, user, password, driver);

        // Now you can call getConnection without arguments
        Connection connection = DataBaseConnection.getConnection();
        System.out.println("Connected successfully: " + connection.getMetaData().getDatabaseProductName() + " - Payment Management DB");


        MigrationRunner migrationRunner = new MigrationRunner();
        migrationRunner.runMigrations(connection);
        try {
            // Create an HttpServer instance
            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

            AccountService accountService = new AccountService(connection);
            LoanService loanService = new LoanService(connection);

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
            //TODO: Create a landing page path called homeHandler to return all the APIs that is supported.

            server.createContext("/create-account", new AccountCreationHandler(userService, accountService));
            server.createContext("/list-accounts", new ListAccountHandler(userService, accountService));
            server.createContext("/deposit", new DepositHandler(userService, accountService));
            server.createContext("/withdraw", new WithdrawHandler(userService, accountService));
            server.createContext("/transfer", new TransferHandler(userService,transferProcessor));
            server.createContext("/collect-loan", new CollectLoanHandler(userService, accountService, loanService, transferProcessor, bankCentralAccountNumber));
            server.createContext("/pay-loan", new PayLoanHandler(userService, accountService, loanService, transferProcessor, bankCentralAccountNumber));

            // Start the server
            server.setExecutor(null); // Use the default executor
            server.start();

            System.out.println("Server is running on port 8000");
        } catch (IOException e) {
            System.out.println("Error starting the server: " + e.getMessage());
        }
    }

    // Define a custom HttpHandler
    static class MyHandler implements HttpHandler {
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

            String response = "Hello, this is a simple HTTP server response!";
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