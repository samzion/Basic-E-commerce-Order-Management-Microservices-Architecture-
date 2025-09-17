package orderManagement.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBaseConnection {
    private static String dbUrl;
    private static String user;
    private static String password;
    private static String className;


    public static void initialize(String url, String u, String p, String driver) {
        dbUrl = url;
        user = u;
        password = p;
        className = driver;
    }
    //connect to postgres
    public static Connection getConnection(  ) throws SQLException, ClassNotFoundException {

        Class.forName(className);
        return DriverManager.getConnection(dbUrl, user, password);
    }

}
