package orderManagement.db.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CartMigration implements IMigration {

    @Override
    public void run(Connection connection) throws SQLException {
        System.out.println("Cart migration started");
        String sql = """ 
                CREATE TABLE IF NOT EXISTS carts (
                    id      SERIAL PRIMARY KEY,
                    user_id      INT NOT NULL,
                    status       VARCHAR(20) DEFAULT 'active', -- active, checked_out, abandoned
                    created_on   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_on   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                          )
        """;

        try (Statement stmt = connection.createStatement()){
            stmt.executeUpdate(sql);
        }
        System.out.println("Order item migration completed!");
    }
}