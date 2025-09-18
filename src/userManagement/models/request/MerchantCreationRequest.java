package userManagement.models.request;

import userManagement.models.Merchant;
import userManagement.models.User;
import userManagement.utilities.PasswordUtil;

public class MerchantCreationRequest {

    private String businessName;
    private String businessAddress;
    private String phoneNumber;

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessAddress() {
        return businessAddress;
    }

    public void setBusinessAddress(String businessAddress) {
        this.businessAddress = businessAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public static String validate(MerchantCreationRequest merchantCreationRequest){
            if(merchantCreationRequest == null){
                return "Merchant creation request cannot be null";
            }
            if(merchantCreationRequest.getBusinessName() == null || merchantCreationRequest.getBusinessName().isEmpty()){
                return "Business name cannot be null or empty";
            }
            if(merchantCreationRequest.getBusinessAddress() == null || merchantCreationRequest.getBusinessAddress().isEmpty()){
                return "Business Address cannot be null or empty";
            }
            if(merchantCreationRequest.getPhoneNumber() == null || merchantCreationRequest.getPhoneNumber().isEmpty()){
                return "Phone number cannot be null or empty";
            }

            return "Merchant creation request okay!";
        }

//        public static Merchant createMerchantObject(MerchantCreationRequest merchantCreationRequest){
//            Merchant merchant = new Merchant();
//
//            user.setFirstName(userCreationRequest.getFirstName());
//            user.setLastName(userCreationRequest.getLastName());
//            user.setEmail(userCreationRequest.getEmail());
//            user.setAddress(userCreationRequest.getAddress());
//            user.setGender(userCreationRequest.getGender());
//            String passwordHash = PasswordUtil.hashPassword(userCreationRequest.getPassword());
//            user.setPasswordHash(passwordHash);
//            return user;
//        }
}

