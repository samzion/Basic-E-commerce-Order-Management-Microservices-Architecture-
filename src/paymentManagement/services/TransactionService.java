package paymentManagement.services;


import paymentManagement.db.DataBaseConnection;
import paymentManagement.enums.TransactionType;
import paymentManagement.models.entities.Account;
import paymentManagement.models.entities.Transaction;

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

    public boolean createTransaction(Account sourceAccount, double amount, TransactionType transactionType) throws SQLException {
        boolean flag = false;

        String query = """
                        INSERT INTO transactions
                            (account_number, amount, transaction_type,  balance_on_source)
                            values (?, ?, ?, ?)
                       """;
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, sourceAccount.getAccountNumber());
        statement.setDouble(2, amount);
        statement.setString(3, transactionType.toString());
        statement.setDouble(4, sourceAccount.getBalance());
        statement.executeUpdate();
        System.out.println("Transaction successful!");
        return true;
    }

//    public boolean createTransactionForTransfer(Account souceaccount, Account destinationAccount, double amount, TransactionType transactionType) throws SQLException {
//        boolean flag = false;
//
//        String query = """
//                        INSERT INTO transactions
//                            (from_account, to_account, amount, transaction_type,  balance)
//                            values (?, ?, ?, ?, ?)
//                       """;
//        PreparedStatement statement = connection.prepareStatement(query);
//        statement.setInt(1, souceaccount.getId());
//        statement.setInt(2, destinationAccount.getId());
//        statement.setDouble(3, amount);
//        statement.setString(4, transactionType.toString());
//        statement.setDouble(5, destinationAccount.getBalance());
//        statement.executeUpdate();
//        System.out.println("Transaction successful!");
//        return true;
//    }

    public List<Transaction> listTransactions(Account account1) throws SQLException {
        String query = """
                        SELECT *
                        FROM transactions
                        WHERE account_id = ?
                       """;
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, account1.getId());
        ResultSet rs = statement.executeQuery();
        List<Transaction> transactions = new ArrayList<>();
        Transaction transaction = new Transaction();
        while(rs.next()){
                transaction.setTransactionId(rs.getInt("id"));
                transaction.setSourceAccount(rs.getString("from_account"));
                transaction.setDestinationAccount(rs.getString("to_account"));
                transaction.setBalance(rs.getInt("balance"));
                transaction.setTransactionType(TransactionType.valueOf(rs.getString("transaction_type")));
                transaction.setCreatedON(rs.getTimestamp("created_on").toLocalDateTime());
                transaction.setUpdatedON(rs.getTimestamp("updated_on").toLocalDateTime());
                transactions.add(transaction);
        }
        return  transactions;

    }
}
