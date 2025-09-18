package paymentManagement.models.requests;

public class PayLoanRequest {
    private String sourceAccountNumber;
    private double amount;

    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public void setSourceAccountNumber(String sourceAccountNumber) {
        this.sourceAccountNumber = sourceAccountNumber;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public static String validate(PayLoanRequest payLoanRequest) {
        if (payLoanRequest == null) {
            return "Pay loan request cannot be null";
        }
        if (payLoanRequest.getSourceAccountNumber() == null) {
            return "Destination account number cannot be null";
        }

        if (payLoanRequest.getAmount() < 100) {
            return "Loan amount cannot be less than 100";
        }
            return "Pay loan request okay!";
    }
}
