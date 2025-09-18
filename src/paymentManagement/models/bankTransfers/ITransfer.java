package paymentManagement.models.bankTransfers;

import oop.models.entities.Account;
import oop.models.response.AccountOperationResponse;

import java.sql.SQLException;

public interface ITransfer {
     boolean transfer(double amount, Account source, Account destination);
     AccountOperationResponse restTransfer(double amount, Account source, Account destination) throws SQLException, ClassNotFoundException;

     String getBank();
}

