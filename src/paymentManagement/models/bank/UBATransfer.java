package paymentManagement.models.bank;


import paymentManagement.models.entities.Account;
import paymentManagement.models.response.AccountOperationResponse;
import paymentManagement.services.AccountService;

import java.sql.SQLException;

public class UBATransfer implements ITransfer {

    protected String bank = "uba";
    public String getBank() {
        return bank;
    }
    public UBATransfer(){
        bank = "uba";
    }
    private AccountService accountService;
    public UBATransfer(AccountService accountService){
        this.accountService = accountService;
    }

    @Override
    public AccountOperationResponse restTransfer(double amount, Account source, Account destination) throws SQLException, ClassNotFoundException, SQLException {

        AccountOperationResponse accountOperationResponse = new AccountOperationResponse();
        AccountOperationResponse withdrawResponse = this.accountService.withdraw(source, amount);
        if(withdrawResponse.getStatusCode()== 200){
            AccountOperationResponse depositResponse = this.accountService.deposit(destination, amount);
            if(depositResponse.getStatusCode() == 200) {
                System.out.println("Using " + bank + " Transfer");
                accountOperationResponse.setStatusCode(200);
                accountOperationResponse.setMessage(source.getAccountName() + " Transferred " + amount + " to " + destination.getAccountName() + " successful!!!");
                return accountOperationResponse;
            } else {
                System.out.println(bank + " Transfer from " + source + " to "  + destination + " not successful");
                return depositResponse;
            }
        } else {
            System.out.println(bank + " Transfer from " + source + " to "  + destination + " not successful");
            return withdrawResponse;
        }
    }
}