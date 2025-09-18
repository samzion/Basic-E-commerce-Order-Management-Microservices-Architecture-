package orderManagement.db.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class OrderMigration implements IMigration {

    @Override
    public void run(Connection connection) throws SQLException {
        System.out.println("Order migration started");
      String sql = """ 
              CREATE TABLE IF NOT EXISTS orders (
                         id BIGSERIAL PRIMARY KEY,
                         user_id BIGINT NOT NULL,                      -- buyer (from UserManagement DB)
                         status VARCHAR(20) DEFAULT 'PENDING',         -- PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                     );
              """;

      try (Statement stmt = connection.createStatement()){
            stmt.executeUpdate(sql);
      }
        System.out.println("Order migration completed!");
    }
}
