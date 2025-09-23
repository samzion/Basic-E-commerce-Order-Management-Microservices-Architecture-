package paymentManagement.models.requests;

public class CollectLoanRequest {
    private String destinationAccountNumber;
    private double loanAmount;

    public String getDestinationAccountNumber() {
        return destinationAccountNumber;
    }

    public void setDestinationAccountNumber(String destinationAccountNumber) {
        this.destinationAccountNumber = destinationAccountNumber;
    }

    public double getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(double loanAmount) {
        this.loanAmount = loanAmount;
    }

    public static String validate(CollectLoanRequest collectLoanRequest) {
        if (collectLoanRequest == null) {
            return "Collect loan request cannot be null";
        }
        if (collectLoanRequest.getDestinationAccountNumber() == null) {
            return "Destination account number cannot be null";
        }

        if (collectLoanRequest.getLoanAmount() < 5000) {
            return "Loan amount cannot be less than 5000";
        }
            return "Collect loan request okay!";
    }
}
