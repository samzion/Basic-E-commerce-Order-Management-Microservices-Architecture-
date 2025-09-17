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
              account_id INT  NOT NULL,
              amount DECIMAL(15,2) NOT NULL,
              transaction_type VARCHAR(8) NOT NULL,
              balance DECIMAL(15,2) NOT NULL,
              created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
              updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
              FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE )
              """;

      //TODO: add account id of sender/receiver depending on the transaction type.
      try (Statement stmt = connection.createStatement()){
            stmt.executeUpdate(sql);
      }
        System.out.println("Transaction migration completed!");
    }
}
