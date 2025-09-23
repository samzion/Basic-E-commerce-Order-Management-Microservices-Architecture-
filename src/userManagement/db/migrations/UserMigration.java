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
                password_hash VARCHAR(255) NOT NULL, -- store hashed password
                role VARCHAR(20) DEFAULT 'CUSTOMER', -- CUSTOMER, MERCHANT, ADMIN
                created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                user_token VARCHAR(100)
              )
              """;
// TODO: Move All enums to enums subpackage in models package. All requests, entities, responses, go into model

      try (Statement stmt = connection.createStatement()){
            stmt.executeUpdate(sql);
      }
        System.out.println("User migration completed!");
    }
}
