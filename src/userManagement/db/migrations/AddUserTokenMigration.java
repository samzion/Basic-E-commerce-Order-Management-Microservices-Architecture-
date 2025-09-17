package userManagement.db.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class AddUserTokenMigration implements IMigration {

    @Override
    public void run(Connection connection) throws SQLException {
        System.out.println("User Token field addition migration started");
      String sql = """
              ALTER TABLE users
               ADD COLUMN IF NOT EXISTS user_token VARCHAR(100);
              """;


      try (Statement stmt = connection.createStatement()){
            stmt.executeUpdate(sql);
      }
        System.out.println("User token field addition  completed!");
    }
}
