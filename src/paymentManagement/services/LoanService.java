package paymentManagement.services;

import paymentManagement.db.DataBaseConnection;
import paymentManagement.models.entities.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LoanService {
    Connection connection ;

    public LoanService() throws SQLException, ClassNotFoundException {
        connection = DataBaseConnection.getConnection();
    }

    public LoanService(Connection connection) throws SQLException, ClassNotFoundException {
        this.connection = connection;
    }

    public boolean createLoan(Account receiverAccount, double amountBorrowed) throws SQLException {
        boolean flag = false;

        String query = """
                        INSERT INTO loans
                            (account_id, amount_borrowed)
                            values (?, ?)
                       """;
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, receiverAccount.getId());
        statement.setDouble(2, amountBorrowed);

        statement.executeUpdate();
        System.out.println("Loan creation finally successful!");
        return true;
    }
}
