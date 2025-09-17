package orderManagement.db.migrations;

import java.sql.Connection;
import java.sql.SQLException;

public interface IMigration {
    void run(Connection connection) throws SQLException;
}
