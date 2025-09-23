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
                   id SERIAL PRIMARY KEY,
                   user_id INT NOT NULL,
                   business_name VARCHAR(100) NOT NULL,
                   business_address TEXT,
                   phone_number VARCHAR(20),
                   verified BOOLEAN DEFAULT FALSE,
                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                   updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
               )
              """;

      try (Statement stmt = connection.createStatement()){
            stmt.executeUpdate(sql);
      }
        System.out.println("Merchant migration completed!");
    }
}
