package paymentManagement.models.requests;

public class TransferRequest {
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private double amount;

    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public void setSourceAccountNumber(String sourceAccountNumber) {
        this.sourceAccountNumber = sourceAccountNumber;
    }

    public String getDestinationAccountNumber() {
        return destinationAccountNumber;
    }

    public void setDestinationAccountNumber(String destinationAccountNumber) {
        this.destinationAccountNumber = destinationAccountNumber;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public static String validate(TransferRequest transferRequest){
        if(transferRequest == null){
            return "Transfer request cannot be null";
        }
        if(transferRequest.getSourceAccountNumber() == null){
            return "Source account number cannot be null";
        }
        if(transferRequest.getDestinationAccountNumber() == null){
            return "Destination account number cannot be null";
        }
        if(transferRequest.getAmount() < 100 ){
            return "Amount to transfer cannot be less than 100";
        }
        return "Transfer request okay!";
    }

}
