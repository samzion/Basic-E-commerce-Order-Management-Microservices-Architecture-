package orderManagement.db.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class OrderItemMigration implements IMigration {

    @Override
    public void run(Connection connection) throws SQLException {
        System.out.println("Order item migration started");
        String sql = """ 
                  CREATE TABLE IF NOT EXISTS order_items (
                    order_item_id BIGSERIAL PRIMARY KEY,
                    order_id BIGINT NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
                    product_id BIGINT NOT NULL REFERENCES products(product_id),
                    quantity INT NOT NULL CHECK (quantity > 0),
                    price DECIMAL(12,2) NOT NULL,                 -- price at time of purchase
                    total DECIMAL(12,2) GENERATED ALWAYS AS (quantity * price) STORED
                  );
                  """;

        try (Statement stmt = connection.createStatement()){
            stmt.executeUpdate(sql);
        }
        System.out.println("Order item migration completed!");
    }
}