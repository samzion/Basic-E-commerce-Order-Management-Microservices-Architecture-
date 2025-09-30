package paymentManagement.models.bank;

import paymentManagement.models.entities.Account;
import paymentManagement.models.response.AccountOperationResponse;

import java.sql.SQLException;

public interface ITransfer {
     AccountOperationResponse restTransfer(double amount, Account source, Account destination) throws SQLException, ClassNotFoundException;

     String getBank();
}

