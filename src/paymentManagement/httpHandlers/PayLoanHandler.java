package paymentManagement.httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import oop.SimpleBankRestApiApplication;
import oop.TransferProcessor;
import oop.models.entities.Account;
import oop.models.entities.Loan;
import oop.models.entities.User;
import oop.models.requests.PayLoanRequest;
import oop.models.response.AccountOperationResponse;
import oop.services.AccountService;
import oop.services.LoanService;
import oop.services.UserService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

public class PayLoanHandler extends BaseHandler implements HttpHandler {
    public AccountService accountService;
    public LoanService loanService;
    public TransferProcessor transferProcessor;
    String bankCentralAccountNumber = "1018521126";//Sammy Bank Central account number

    public PayLoanHandler(UserService userService, AccountService accountService, LoanService loanService, TransferProcessor transferProcessor, String bankCentralAccountNumber){
        super(userService);
        this.accountService = accountService;
        this.loanService = loanService;
        this.transferProcessor = transferProcessor;
        this.bankCentralAccountNumber = bankCentralAccountNumber;
    }


    public void handle (HttpExchange exchange) throws IOException {
        if(!this.isValidRequestMethod(exchange, "post")) {
            // Handle the request
            String response = "Method not allowed";
            SimpleBankRestApiApplication.writeHttpResponse(exchange, 405, response);
            return;
        }
        User authenticatedUser = this.getAuthenticatedUser(exchange);
        if(authenticatedUser == null) {
            SimpleBankRestApiApplication.writeHttpResponse(exchange, 401, "Unauthorized!");
            return;
        }
        String body = "{}";
        try (InputStream input = exchange.getRequestBody()) {
            body =  new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        PayLoanRequest payLoanRequest = gson.fromJson(body, PayLoanRequest.class);
        String validationMessage = PayLoanRequest.validate(payLoanRequest);
        if(!validationMessage.equals("Pay loan request okay!")){
            SimpleBankRestApiApplication.writeHttpResponse(exchange, 400, validationMessage);
            return;
        }

        Account sourceAccount = new Account();
        try {
            sourceAccount = this.accountService.confirmAccountDetails(payLoanRequest.getSourceAccountNumber());

            if (sourceAccount == null) {
                SimpleBankRestApiApplication.writeHttpResponse(exchange, 404, "Source account not found");
                return;
            }

        } catch (SQLException e) {
            SimpleBankRestApiApplication.writeHttpResponse(exchange, 500, "Unknown error from server");
        }

        try{

            List<Loan> loans;
            loans = loanService.listLoans(sourceAccount);
            if(loans.isEmpty()){
                SimpleBankRestApiApplication.writeHttpResponse(exchange, 500, "You never taken any loans on this account before ");
                return;
            }
            double amount = payLoanRequest.getAmount();
            String transferResultMessage = "";
            boolean isLoanAvailable = false;
            boolean isLoanAvailableSuccessfullyPaid = true;
            for(Loan loan: loans){
                //check if there is any loan left to be paid and check amount to be paid
                if(loan.getAmountPaid() == loan.getAmountBorrowed()){
                    continue;
                }

                double amountToBePaid = loan.getAmountBorrowed() - loan.getAmountPaid();
                if(loan.getAmountPaid() < loan.getAmountBorrowed() && amount > amountToBePaid){
                    AccountOperationResponse transferResponse = this.transferProcessor.transfer(authenticatedUser, payLoanRequest.getSourceAccountNumber(), bankCentralAccountNumber,amountToBePaid);
                    transferResultMessage += "  " + transferResponse.getMessage();
                    if(transferResponse.getStatusCode()== 200){
                        loan.setAmountPaid( loan.getAmountPaid() + amountToBePaid);
                        amount = amount - amountToBePaid;
                        loanService.updateLoan(loan);
                    }else {
                        System.out.println(transferResponse.getMessage());
                        isLoanAvailableSuccessfullyPaid =false;
                    }
                    isLoanAvailable = true;
                } else if(loan.getAmountPaid() < loan.getAmountBorrowed() && amount <= amountToBePaid) {
                    AccountOperationResponse transferResponse =this.transferProcessor.transfer(authenticatedUser, payLoanRequest.getSourceAccountNumber(), bankCentralAccountNumber,amount);
                    transferResultMessage += "  " + transferResponse.getMessage();
                    if(transferResponse.getStatusCode()== 200){
                        loan.setAmountPaid( loan.getAmountPaid() + amount);
                        loanService.updateLoan(loan);
                    }else {
                        System.out.println(transferResponse.getMessage());
                        isLoanAvailableSuccessfullyPaid =false;
                    }
                    isLoanAvailable = true;
                }
            }
            if(isLoanAvailable){
                if(isLoanAvailableSuccessfullyPaid){
                    SimpleBankRestApiApplication.writeHttpResponse(exchange, 200, " Loan payment successful!!!");
                }else {
                    SimpleBankRestApiApplication.writeHttpResponse(exchange, 200, " Partial loan(s) settlement done. " + transferResultMessage);
                }

            } else {
                SimpleBankRestApiApplication.writeHttpResponse(exchange, 400, "No unsettled loans available for repayment");
            }
        } catch (Exception e) {
            SimpleBankRestApiApplication.writeHttpResponse(exchange, 500, "Unknown error from server");
        }
    }
}
