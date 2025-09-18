package paymentManagement.models.bankTransfers;

import oop.bank.DefaultTransfer;
import oop.bank.GTBTransfer;
import oop.bank.ITransfer;
import oop.bank.UBATransfer;
import oop.models.entities.Account;
import oop.models.entities.User;
import oop.models.response.AccountOperationResponse;
import oop.services.AccountService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransferProcessor {
    private AccountService accountService;
    private List<ITransfer> iTransferList;

    public TransferProcessor(AccountService accountService,  List<ITransfer> iTransferList){
        this.accountService = accountService;
        this.iTransferList = iTransferList;
    }
    public AccountOperationResponse transfer(User user, String sourceAccountNumber, String destinationAccountNumber, double amount) throws SQLException, ClassNotFoundException {

        AccountOperationResponse accountOperationResponse = new AccountOperationResponse();
        Account sourceAccount = this.accountService.getAccountByUserAndAccountNumber(user,sourceAccountNumber);
        if(sourceAccount == null){
            accountOperationResponse.setStatusCode(404);
            accountOperationResponse.setMessage("Source account not found.");
            return accountOperationResponse;
        }
        Account destinationAccount = this.accountService.confirmAccountDetails(destinationAccountNumber);
        if(destinationAccount == null){
            accountOperationResponse.setStatusCode(404);
            accountOperationResponse.setMessage("Destination account not found.");
            return accountOperationResponse;
        }

        String bank = destinationAccount.getBank();

        for (ITransfer genericTransfer1: this.iTransferList){
            String currentBank = genericTransfer1.getBank();
            if (bank.equals(currentBank) || currentBank.equals("generic")){
                accountOperationResponse =  genericTransfer1.restTransfer(amount, sourceAccount, destinationAccount);

            }
        }
        return accountOperationResponse;
    }
    public static boolean transfer(double amount, Account source, Account destination){
        DefaultTransfer genericTransfer = new DefaultTransfer();
        GTBTransfer gtbTransfer = new GTBTransfer();
        UBATransfer ubaTransfer = new UBATransfer();
        List<ITransfer> genericTransfers = new ArrayList<>();
        genericTransfers.add(gtbTransfer);
        genericTransfers.add(ubaTransfer);
        genericTransfers.add(genericTransfer);

        String bank = destination.getBank();


        for (ITransfer genericTransfer1: genericTransfers){
            String currentBank = genericTransfer1.getBank();
            if (bank.equals(currentBank) || currentBank.equals("generic")){
                if(!genericTransfer1.transfer(amount, source, destination)){
                    return false;
                }

            }
        }
        return true;
    }
}