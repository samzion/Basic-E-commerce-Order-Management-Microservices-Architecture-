package userManagement.db.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class UserMigration implements IMigration {

    @Override
    public void run(Connection connection) throws SQLException {
        System.out.println("User migration started");
      String sql = """
              CREATE TABLE IF NOT EXISTS users (
                id SERIAL PRIMARY KEY,
                firstname VARCHAR(25) NOT NULL,
                lastname VARCHAR(25) NOT NULL,
                gender VARCHAR(1) NOT NULL,
                email VARCHAR(100) UNIQUE NOT NULL,
                address VARCHAR(100) NOT NULL,
                password VARCHAR(100) NOT NULL,
                created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                CONSTRAINT chk_password_length CHECK (LENGTH(password) >= 8)
              )
              """;


      try (Statement stmt = connection.createStatement()){
            stmt.executeUpdate(sql);
      }
        System.out.println("User migration completed!");
    }
}
