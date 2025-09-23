package paymentManagement.models.entities;


import paymentManagement.enums.TransactionType;

import java.time.LocalDateTime;

public class Transaction {
    TransactionType transactionType;
    LocalDateTime transactionDateTime;
    double amount;
    double balance;
    private LocalDateTime createdON;
    private LocalDateTime updatedON;
    private  int transactionId;

    public LocalDateTime getCreatedON() {
        return createdON;
    }

    public void setCreatedON(LocalDateTime createdON) {
        this.createdON = createdON;
    }

    public LocalDateTime getUpdatedON() {
        return updatedON;
    }

    public void setUpdatedON(LocalDateTime updatedON) {
        this.updatedON = updatedON;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public Transaction() {
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }
    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public LocalDateTime getTransactionDateTime() {
        return transactionDateTime;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }


    public void setTransactionDateTime(LocalDateTime transactionDateTime) {
        this.transactionDateTime = transactionDateTime;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}