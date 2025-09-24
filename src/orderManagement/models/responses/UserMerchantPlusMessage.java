package orderManagement.models.responses;

public class UserMerchantPlusMessage {
    private UserMerchantDetails userMerchantDetails;
    private String errorMessage;


    public UserMerchantDetails getUserMerchantDetails() {
        return userMerchantDetails;
    }

    public void setUserMerchantDetails(UserMerchantDetails userMerchantDetails) {
        this.userMerchantDetails = userMerchantDetails;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
