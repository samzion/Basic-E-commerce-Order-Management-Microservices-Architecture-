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
                    account INT NOT NULL,
                    amount DECIMAL(12,2) NOT NULL,
                    transaction_type VARCHAR(20),           -- DEBIT, CREDIT, TRANSFER
                    balance_on_source  DECIMAL(12,2) NOT NULL,
                    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP)
              """;
      try (Statement stmt = connection.createStatement()){
            stmt.executeUpdate(sql);
      }
        System.out.println("Transaction migration completed!");
    }
}
