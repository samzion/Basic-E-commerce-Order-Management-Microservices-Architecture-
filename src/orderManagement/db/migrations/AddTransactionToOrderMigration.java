package orderManagement.db.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class AddTransactionToOrderMigration implements IMigration {

    @Override
    public void run(Connection connection) throws SQLException {
        System.out.println("Order migration started");
      String sql = """ 
              ALTER TABLE orders
                ADD COLUMN IF NOT EXISTS transaction_id BIGINT NULL;
              """;

      try (Statement stmt = connection.createStatement()){
            stmt.executeUpdate(sql);
      }
        System.out.println("Add transaction column to Order migration completed!");
    }
}
