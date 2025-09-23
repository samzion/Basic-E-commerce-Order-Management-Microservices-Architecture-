package paymentManagement.models.bankTransfers;

import oop.models.entities.Account;
import oop.models.response.AccountOperationResponse;
import oop.services.AccountService;

import java.sql.SQLException;

public class GTBTransfer implements ITransfer {

    private AccountService accountService;
    protected String bank = "generic";

    public GTBTransfer(AccountService accountService){
        this.accountService = accountService;
    }

    public String getBank() {
        return bank;
    }
    public GTBTransfer(){
        bank = "gtb";
    }

    public boolean transfer(double amount, Account source, Account destination){
        boolean withdrawSuccessful = source.withdraw(amount);
        if(withdrawSuccessful){
            boolean depositSuccessful = destination.deposit(amount);
            if(depositSuccessful) {
                System.out.println("Using " + bank + " Transfer");
                System.out.println(source.getAccountName() + " Transferred " + amount + " to " + destination.getAccountName() + " successful!!!");
                return true;
            } else {
                System.out.println(bank + " Transfer from " + source + " to "  + destination + " not successful");
                return false;
            }
        } else {
            System.out.println(bank + " Transfer from " + source + " to "  + destination + " not successful");
            return false;
        }
    }

    @Override
    public AccountOperationResponse restTransfer(double amount, Account source, Account destination) throws SQLException, ClassNotFoundException {

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