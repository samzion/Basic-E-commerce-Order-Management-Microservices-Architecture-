package paymentManagement.services;

import oop.db.DataBaseConnection;
import oop.models.entities.Account;
import oop.models.entities.Loan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

    public List<Loan> listLoans(Account account1) throws SQLException, ClassNotFoundException {
        String query = """
                        SELECT *
                        FROM loans
                        WHERE account_id = ?
                       """;
        PreparedStatement statement = connection.prepareStatement(query);

        statement.setInt(1, account1.getId());

        ResultSet rs = statement.executeQuery();

        List<Loan> loans = new ArrayList<>();

        while(true){
            if(rs.next()){
                Loan loan = new Loan();
                loan.setId(rs.getInt("id"));
                AccountService accountService = new AccountService();
                loan.setAccount(accountService.confirmAccountDetails(rs.getInt("account_id")));
                loan.setAmountBorrowed(rs.getDouble("amount_borrowed"));
                loan.setCreatedON(rs.getTimestamp("created_on").toLocalDateTime());
                loan.setUpdatedON(rs.getTimestamp("updated_on").toLocalDateTime());
                loans.add(loan);
            } else {
                break;
            }
        }
        return  loans;

    }

    public boolean updateLoan(Loan loan) throws SQLException {
        boolean flag = false;

        String query = """
                        UPDATE loans
                                  SET amount_paid = ?,
                                  updated_on = CURRENT_TIMESTAMP
                                  WHERE id = ?;
                       """;
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setDouble(1, loan.getAmountPaid());
        statement.setInt(2, loan.getId());
        statement.executeUpdate();
        System.out.println("Loan updated successfully!");
        return true;
    }
}
