package paymentManagement.services;

import oop.TransactionType;
import oop.db.DataBaseConnection;
import oop.models.entities.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransactionService {
    Connection connection ;

    public TransactionService() throws SQLException, ClassNotFoundException {
       connection = DataBaseConnection.getConnection();
    }

    public TransactionService(Connection connection) throws SQLException, ClassNotFoundException {
        this.connection = connection;
    }

    public boolean createTransaction(Account account, double amount, TransactionType transactionType) throws SQLException {
        boolean flag = false;

        String query = """
                        INSERT INTO transactions
                            (account_id, amount, transaction_type,  balance)
                            values (?, ?, ?, ?)
                       """;
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, account.getId());
        statement.setDouble(2, amount);
        statement.setString(3, transactionType.toString());
        statement.setDouble(4, account.getBalance());
        statement.executeUpdate();
        System.out.println("Transaction successful!");
        return true;
    }

    public List<Account> listTransactions(Account account1) throws SQLException {
        String query = """
                        SELECT *
                        FROM transactions
                        WHERE account_id = ?
                       """;
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, account1.getId());
        ResultSet rs = statement.executeQuery();
        List<Account> accounts = new ArrayList<>();
        while(true){
            if(rs.next()){
                Account account = new Account();
                account.setId(rs.getInt("id"));
                account.setUserID(rs.getInt("user_id"));
                account.setAccountNumber(rs.getString("account_number"));
                account.setBalance(rs.getInt("balance"));
                account.setCreatedON(rs.getTimestamp("created_on").toLocalDateTime());
                account.setUpdatedON(rs.getTimestamp("updated_on").toLocalDateTime());
                accounts.add(account);
            } else {
                break;
            }
        }
        return  accounts;

    }
}
