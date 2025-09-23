package orderManagement.db.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class ShipmentMigration implements IMigration {

    @Override
    public void run(Connection connection) throws SQLException {
        System.out.println("Shipment migration started");
      String sql = """ 
              CREATE TABLE IF NOT EXISTS shipments (
                    shipment_id BIGSERIAL PRIMARY KEY,
                    order_Item_id BIGINT NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
                    address VARCHAR(255) NOT NULL,
                    tracking_number VARCHAR(100),
                    status VARCHAR(20) DEFAULT 'PROCESSING',      -- PROCESSING, IN_TRANSIT, DELIVERED
                    shipped_at TIMESTAMP,
                    delivered_at TIMESTAMP
                );
              """;

      try (Statement stmt = connection.createStatement()){
            stmt.executeUpdate(sql);
      }
        System.out.println("Shipment migration completed!");
    }
}
