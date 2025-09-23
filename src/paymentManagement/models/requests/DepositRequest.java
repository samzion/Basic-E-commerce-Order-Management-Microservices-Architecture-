package paymentManagement.models.requests;

public class DepositRequest {
    private String accountNumber;
    private double depositAmount;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public double getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(double amount) {
        this.depositAmount = amount;
    }

    public static String validate(DepositRequest depositRequest){
        if(depositRequest == null){
            return "Deposit request cannot be null";
        }
        if(depositRequest.getAccountNumber() == null){
            return "Account number cannot be null";
        }
        if(depositRequest.getDepositAmount() < 100 ){
            return "Deposit Amount cannot be less than 100";
        }
        return "Deposit request okay!";
    }

}
