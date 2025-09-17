package paymentManagement.db.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class LoanMigration implements IMigration {

    @Override
    public void run(Connection connection) throws SQLException {
        System.out.println("Loan migration started");
      String sql = """ 
              CREATE TABLE IF NOT EXISTS loans (
              id SERIAL PRIMARY KEY,
              account_id INT NOT NULL,
              amount_borrowed DECIMAL(15,2) DEFAULT 0.00,
              amount_paid DECIMAL(15,2) DEFAULT 0.00,
              date DATE DEFAULT CURRENT_DATE,
              due_date DATE DEFAULT CURRENT_DATE + INTERVAL '2 weeks',
              created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
              updated_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
              FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE RESTRICT )
              """;
//TODO: add account id of sender/receiver depending on the transaction type.
      try (Statement stmt = connection.createStatement()){
            stmt.executeUpdate(sql);
      }
        System.out.println("Loan migration completed!");
    }
}
