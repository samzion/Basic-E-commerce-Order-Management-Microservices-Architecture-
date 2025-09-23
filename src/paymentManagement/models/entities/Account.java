package paymentManagement.models.entities;


import paymentManagement.enums.TransactionType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Account {
    private int id;
    private int userID;
    private String accountNumber;
    private String accountName;
    private double balance;
    private LocalDateTime createdON;
    private LocalDateTime updatedON;
    private String bank;
    List<Transaction> transactions = new ArrayList<>();
    List<Loan> loans = new ArrayList<>();

    public Account() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public LocalDateTime getCreatedON() {
        return createdON;
    }

    public LocalDateTime getUpdatedON() {
        return updatedON;
    }

    public void setCreatedON(LocalDateTime createdON) {
        this.createdON = createdON;
    }

    public void setUpdatedON(LocalDateTime updatedON) {
        this.updatedON = updatedON;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public List<Loan> getLoans() {
        return loans;
    }

    public void setLoans(List<Loan> loans) {
        this.loans = loans;
    }

//withdrawal method for transactions
    public boolean withdraw(double withdrawAmt){
        if(withdrawAmt <=0){
            System.out.println( "Withdrawal amount cannot be zero or negative");
            return false;
        }
        if (this.balance >= withdrawAmt){
            Transaction transaction = new Transaction();
            this.balance-=withdrawAmt;
            transaction.setTransactionDateTime(LocalDateTime.now());
            transaction.setTransactionType(TransactionType.DEBIT);
            transaction.setAmount(withdrawAmt);
            transaction.setBalance(this.balance);
            this.transactions.add(transaction);
            System.out.println("Withdrawal of " + withdrawAmt + " from " + this.accountNumber + "'s account is successful");
            return true;
        } else {
            System.out.println("Insufficient balance");
            return false;
        }
    }

    //deposit method for transactions
    public boolean deposit( double depositAmt){

        if(depositAmt <=0){
            System.out.println( "Invalid figure");
            return false;
        }
        Transaction transaction = new Transaction();
        this.balance+=depositAmt;
        transaction.setTransactionDateTime(LocalDateTime.now());
        transaction.setTransactionType(TransactionType.CREDIT);
        transaction.setAmount(depositAmt);
        transaction.setBalance(this.balance);
        this.transactions.add(transaction);
        System.out.println("Deposit of " + depositAmt + " into " + this.accountNumber + "'s account is successful");
        return true;
    }




    public String toString(){
        double sumDebit=0;
        double sumCredit=0;

        for(Transaction transaction: transactions){
            if(transaction.transactionType == TransactionType.DEBIT){
                sumDebit += transaction.amount;
            } else {
                sumCredit+=transaction.amount;
            }
        }
        return accountNumber + "        " + bank + "         " +  sumCredit + "             " + sumDebit + "                    " + getBalance();
    }
}
