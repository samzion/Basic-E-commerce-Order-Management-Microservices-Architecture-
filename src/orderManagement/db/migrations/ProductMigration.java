package orderManagement.db.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class ProductMigration implements IMigration {

    @Override
    public void run(Connection connection) throws SQLException {
        System.out.println("Product migration started");
      String sql = """ 
              CREATE TABLE IF NOT EXISTS products (
                      product_id BIGSERIAL PRIMARY KEY,
                      merchant_id BIGINT NOT NULL,                  -- merchant ID from UserManagement DB
                      name VARCHAR(255) NOT NULL,
                      description TEXT,
                      price DECIMAL(12,2) NOT NULL,
                      stock INT NOT NULL CHECK (stock >= 0),        -- available units
                      category VARCHAR(100),
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                  );
              """;

      try (Statement stmt = connection.createStatement()){
            stmt.executeUpdate(sql);
      }
        System.out.println("Product migration completed!");
    }
}
