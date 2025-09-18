package paymentManagement.models.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Loan {
    int id;
    double amountBorrowed;
    LocalDate date;
    LocalDate dueDate;
    Account account;
    double amountPaid =0;
    LocalDateTime createdON;
    LocalDateTime updatedON;

    public Loan() {
    }

    public Loan(double amount, LocalDate date, LocalDate dueDate) {
        this.amountBorrowed = amount;
        this.date = date;
        this.dueDate = dueDate;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmountBorrowed() {
        return amountBorrowed;
    }

    public void setAmountBorrowed(double amountBorrowed) {
        this.amountBorrowed = amountBorrowed;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

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
}
