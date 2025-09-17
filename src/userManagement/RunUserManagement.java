package userManagement;

import userManagement.db.DataBaseConnection;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class RunUserManagement {
    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        //read from configuration.properties file for all necessary entities to work with

        // Load configuration from file
        Properties props = new Properties();
        props.load(new FileInputStream("userManagementConfiguration.properties"));

        String url = props.getProperty("dbUrl");
        String user = props.getProperty("dbUser");
        String password = props.getProperty("dbPassword");
        String driver = props.getProperty("dbDriver");


        // Initialize once
        DataBaseConnection.initialize(url, user, password, driver);

        // Now you can call getConnection without arguments
        Connection connection = DataBaseConnection.getConnection();
        System.out.println("Connected successfully: " + connection.getMetaData().getDatabaseProductName() + " - User Management DB");
    }
}
