package paymentManagement.db.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class PaymentMigration implements IMigration {

    @Override
    public void run(Connection connection) throws SQLException {
        System.out.println("Payment migration started");
      String sql = """ 
              CREATE TABLE IF NOT EXISTS payments (
                    payment_id SERIAL PRIMARY KEY,           -- unique identifier for each payment
                    order_id INT NOT NULL,                   -- link to orders table (from Order Management DB)
                    user_id INT NOT NULL,                    -- buyer who made the payment
                    merchant_id INT NOT NULL,                -- seller who will receive payment
                    amount DECIMAL(10,2) NOT NULL,           -- payment amount
                    currency VARCHAR(10) DEFAULT 'NGN',      -- currency used
                    status VARCHAR(20) DEFAULT 'PENDING',    -- PENDING, COMPLETED, FAILED, REFUNDED
                    payment_method VARCHAR(50) DEFAULT 'BANK_TRANSFER',              -- e.g., CARD, BANK_TRANSFER, WALLET
                    transaction_ref VARCHAR(100) UNIQUE,     -- external transaction reference from payment gateway
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
              """;

      try (Statement stmt = connection.createStatement()){
            stmt.executeUpdate(sql);
      }
        System.out.println("Payment migration completed!");
    }
}
