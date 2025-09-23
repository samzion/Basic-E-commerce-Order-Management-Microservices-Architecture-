package paymentManagement.models.requests;

public class AccountListRequest {
    private String userToken;


    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken= userToken;
    }



    public static String validate(AccountListRequest accountListRequest){
        if(accountListRequest == null){
            return "Account list request cannot be null";
        }

        if(accountListRequest.getUserToken() == null ||accountListRequest.getUserToken().isEmpty()){
            return "user ten cannot be null or empty";
        }

        return "Account list request okay!";
    }
}
