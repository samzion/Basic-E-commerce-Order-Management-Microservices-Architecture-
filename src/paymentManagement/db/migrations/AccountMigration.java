package paymentManagement.db.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class AccountMigration implements IMigration {

    @Override
    public void run(Connection connection) throws SQLException {
        System.out.println("Account migration started!");
      String sql = """ 
              CREATE TABLE IF NOT EXISTS accounts (
              id SERIAL PRIMARY KEY,
              user_id INT NOT NULL,
              merchant_id INT,
              account_number VARCHAR(20) UNIQUE NOT NULL,
              account_name VARCHAR(100) NOT NULL,
              bank VARCHAR(100) DEFAULT 'default',
              balance DECIMAL(15,2) DEFAULT 0.00,
              created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
              updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP
               )
              """;

      try (Statement stmt = connection.createStatement()){
            stmt.executeUpdate(sql);
      }
        System.out.println("Account migration completed!");
    }
}
