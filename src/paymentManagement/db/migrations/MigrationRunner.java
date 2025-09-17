package paymentManagement.db.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MigrationRunner {
    private final List<IMigration> migrations;

    public MigrationRunner() {
        migrations = new ArrayList<>();
        migrations.add(new AccountMigration());
        migrations.add(new PaymentMigration());
        migrations.add(new TransactionMigration());
    }

    public void runMigrations(Connection conn) throws SQLException {
        for (IMigration migration : migrations) {
            migration.run(conn);
        }
    }
}
