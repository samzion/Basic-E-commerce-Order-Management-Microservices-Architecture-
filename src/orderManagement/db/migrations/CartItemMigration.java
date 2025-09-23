package orderManagement.db.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CartItemMigration implements IMigration {

    @Override
    public void run(Connection connection) throws SQLException {
        System.out.println("Cart Item migration started");
        String sql = """ 
                CREATE TABLE IF NOT EXISTS cart_items (
                id SERIAL PRIMARY KEY,
                cart_id      INT NOT NULL REFERENCES carts(cart_id) ON DELETE CASCADE,
                product_id   INT NOT NULL REFERENCES products(product_id),
                quantity     INT NOT NULL CHECK (quantity > 0),
                status       VARCHAR(20) DEFAULT 'active', -- active, checked_out, abandoned,
                added_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
        """;

        try (Statement stmt = connection.createStatement()){
            stmt.executeUpdate(sql);
        }
        System.out.println("Cart item migration completed!");
    }
}