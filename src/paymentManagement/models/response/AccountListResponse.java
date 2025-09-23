package paymentManagement.models.response;


import paymentManagement.models.entities.Account;

import java.util.List;

public class AccountListResponse {
    private List<Account> accounts ;

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }
}
