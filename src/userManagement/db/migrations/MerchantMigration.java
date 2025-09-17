package userManagement.db.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class MerchantMigration implements IMigration {

    @Override
    public void run(Connection connection) throws SQLException {
        System.out.println("Merchant migration started");
      String sql = """
              CREATE TABLE IF NOT EXISTS merchants (
                   merchant_id SERIAL PRIMARY KEY,
                   user_id INT NOT NULL,
                   business_name VARCHAR(100) NOT NULL,
                   business_address TEXT,
                   phone_number VARCHAR(20),
                   verified BOOLEAN DEFAULT FALSE,
                   merchant_token VARCHAR(100),
                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                   updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                   FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
               )
              """;

      try (Statement stmt = connection.createStatement()){
            stmt.executeUpdate(sql);
      }
        System.out.println("Merchant migration completed!");
    }
}
