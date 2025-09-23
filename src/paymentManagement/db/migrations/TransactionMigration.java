package paymentManagement.db.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class TransactionMigration implements IMigration {

    @Override
    public void run(Connection connection) throws SQLException {
        System.out.println("Transaction migration started!");
      String sql = """ 
                  CREATE TABLE IF NOT EXISTS transactions (
                  id SERIAL PRIMARY KEY,
                  from_account INT NOT NULL,              -- account sending money
                  to_account INT NOT NULL,                -- account receiving money
                  amount DECIMAL(12,2) NOT NULL,
                  transaction_type VARCHAR(20),           -- DEBIT, CREDIT, TRANSFER
                  status VARCHAR(20) DEFAULT 'PENDING',   -- PENDING, SUCCESS, FAILED
                  created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                  updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                  FOREIGN KEY (payment_id) REFERENCES payments(payment_id) ON DELETE SET NULL)
              """;

      try (Statement stmt = connection.createStatement()){
            stmt.executeUpdate(sql);
      }
        System.out.println("Transaction migration completed!");
    }
}
