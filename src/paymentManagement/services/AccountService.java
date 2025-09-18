package paymentManagement.services;

import oop.TransactionType;
import oop.db.DataBaseConnection;
import oop.models.entities.Account;
import oop.models.entities.User;
import oop.models.response.AccountOperationResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AccountService {
    Connection connection ;

    public AccountService() throws SQLException, ClassNotFoundException {
       connection = DataBaseConnection.getConnection();
    }

    public AccountService(Connection connection) {
        this.connection = connection;
    }

    public boolean createAccount(User user, String accountNumber, String bank) throws SQLException {
        boolean flag = false;

        String query = """
                        INSERT INTO accounts
                            (user_id, account_number, account_name, bank)
                            values (?, ?, ?, ?)
                       """;
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, user.getId());
        statement.setString(2, accountNumber);
        statement.setString(3, user.getFirstName() + " "+ user.getLastName());
        statement.setString(4, bank);
        statement.executeUpdate();
        System.out.println("Account Created successfully!");
        return true;
    }

    public boolean updateAccountBalance(Account account) throws SQLException {
        boolean flag = false;

        String query = """
                        UPDATE accounts
                                  SET balance = ?,
                                  updated_on = CURRENT_TIMESTAMP
                                  WHERE account_number = ?;
                       """;
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setDouble(1, account.getBalance());
        statement.setString(2, account.getAccountNumber());
        statement.executeUpdate();
        System.out.println("Account updated successfully!");
        return true;
    }

    public List<Account> listAccount(User user) throws SQLException {
        String query = """
                        SELECT *
                        FROM accounts
                        WHERE user_id = ?
                       """;
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, user.getId());
        ResultSet rs = statement.executeQuery();
        List<Account> accounts = new ArrayList<>();
        while(true){
            if(rs.next()){
                Account account = new Account();
                account.setId(rs.getInt("id"));
                account.setUserID(rs.getInt("user_id"));
                account.setAccountNumber(rs.getString("account_number"));
                account.setAccountName(rs.getString("account_name"));
                account.setBank(rs.getString("bank"));
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

    public Account confirmAccountDetails(String accountNumber) throws SQLException {
        String queryLoginDetails = "SELECT * FROM accounts WHERE account_number = ?";
        PreparedStatement pStatement = connection.prepareStatement(queryLoginDetails);
        pStatement.setString(1, accountNumber);
        ResultSet rs = pStatement.executeQuery();
        Account account = new Account();
        if (rs.next()) {
            System.out.println("The account number exist.");
            account.setId(rs.getInt("id"));
            account.setAccountNumber(rs.getString("account_number"));
            account.setUserID(rs.getInt("user_id"));
            account.setAccountName(rs.getString("account_name"));
            account.setBank(rs.getString("bank"));
            account.setBalance(rs.getDouble("balance"));
            account.setCreatedON(rs.getTimestamp("created_on").toLocalDateTime());
            account.setUpdatedON(rs.getTimestamp("updated_on").toLocalDateTime());
            return account;
        }
        return null;
    }

    public Account confirmAccountDetails(int accountId) throws SQLException {
        String queryLoginDetails = "SELECT * FROM accounts WHERE id = ?";
        PreparedStatement pStatement = connection.prepareStatement(queryLoginDetails);
        pStatement.setInt(1, accountId);
        ResultSet rs = pStatement.executeQuery();
        Account account = new Account();
        if (rs.next()) {
            System.out.println("The account number exist.");
            account.setId(rs.getInt("id"));
            account.setAccountNumber(rs.getString("account_number"));
            account.setUserID(rs.getInt("user_id"));
            account.setAccountName(rs.getString("account_name"));
            account.setBank(rs.getString("bank"));
            account.setBalance(rs.getDouble("balance"));
            account.setCreatedON(rs.getTimestamp("created_on").toLocalDateTime());
            account.setUpdatedON(rs.getTimestamp("updated_on").toLocalDateTime());
            return account;
        }
        return null;
    }

    public AccountOperationResponse deposit(User user, String accountNumber, double depositAmt) throws SQLException, ClassNotFoundException {
        AccountOperationResponse accountOperationResponse = new AccountOperationResponse();

        Account sourceAccount = getAccountByUserAndAccountNumber(user, accountNumber);
        if (sourceAccount == null){
            accountOperationResponse.setStatusCode(404);
            accountOperationResponse.setMessage("User account not found");
            return accountOperationResponse;
        }
      return deposit(sourceAccount,depositAmt);
    }

    public AccountOperationResponse deposit( Account sourceAccount, double depositAmt) throws SQLException, ClassNotFoundException {
        AccountOperationResponse accountOperationResponse = new AccountOperationResponse();
        TransactionService transactionService = new TransactionService();
        double balance =  sourceAccount.getBalance();
        if(depositAmt < 100){
            accountOperationResponse.setStatusCode(400);
            accountOperationResponse.setMessage("Invalid figure");
            return accountOperationResponse;
        }
        balance+=depositAmt;
        sourceAccount.setBalance(balance);
        AccountService accountService = new AccountService();
        if(accountService.updateAccountBalance(sourceAccount)){
            transactionService.createTransaction(sourceAccount, depositAmt, TransactionType.CREDIT);
            System.out.println("Deposit of " + depositAmt + " from " + sourceAccount.getAccountNumber() + "'s account is successful");
            accountOperationResponse.setStatusCode(200);
            accountOperationResponse.setMessage("Deposit request successful!");
            return accountOperationResponse;
        }
        accountOperationResponse.setStatusCode(500);
        accountOperationResponse.setMessage("Unknown error occurred");
        return accountOperationResponse;
    }

    public Account getAccountByUserAndAccountNumber(User user, String accountNumber) throws SQLException, ClassNotFoundException {
        List<Account> accounts = new AccountService().listAccount(user);
        if(accounts == null){
            return null;
        }
        for(Account account: accounts){
            if(account.getAccountNumber().equalsIgnoreCase(accountNumber)){
                return account;
            }
        }
        return null;
    }

    public AccountOperationResponse withdraw( User user, String accountNumber, double withdrawAmt) throws SQLException, ClassNotFoundException {
        AccountOperationResponse accountOperationResponse = new AccountOperationResponse();
        Account sourceAccount = getAccountByUserAndAccountNumber(user, accountNumber);
        if (sourceAccount == null){
            accountOperationResponse.setStatusCode(404);
            accountOperationResponse.setMessage("User account not found");
            return accountOperationResponse;
        }
       return withdraw(sourceAccount,withdrawAmt);
    }

    public AccountOperationResponse withdraw( Account sourceAccount, double withdrawAmt) throws SQLException, ClassNotFoundException {
        AccountOperationResponse accountOperationResponse = new AccountOperationResponse();
        TransactionService transactionService = new TransactionService();
        double balance =  sourceAccount.getBalance();
        if(withdrawAmt > balance){
            accountOperationResponse.setStatusCode(404);
            accountOperationResponse.setMessage("Insufficient funds");
            return accountOperationResponse;
        }
        balance-=withdrawAmt;
        sourceAccount.setBalance(balance);
        AccountService accountService = new AccountService();
        if(accountService.updateAccountBalance(sourceAccount)){
            transactionService.createTransaction(sourceAccount, withdrawAmt, TransactionType.DEBIT);
            System.out.println("Withdrawal of " + withdrawAmt + " from " + sourceAccount.getAccountNumber() + "'s account is successful");
            accountOperationResponse.setStatusCode(200);
            accountOperationResponse.setMessage("Withdrawal request successful!");
            return accountOperationResponse;
        }
        accountOperationResponse.setStatusCode(500);
        accountOperationResponse.setMessage("Unknown error occurred");
        return accountOperationResponse;
    }

}
