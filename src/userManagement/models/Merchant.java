package userManagement.models;

import userManagement.models.request.MerchantCreationRequest;

import java.time.LocalDateTime;

public class Merchant {
    private int id;
    private int userId;
    private String  businessName;
    private String businessAddress;
    private String phoneNumber;
    private boolean verified;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
    public Merchant(){

    }

    public Merchant(MerchantCreationRequest merchantCreationRequest, int userId){
        this.userId = userId;
        this.businessName = merchantCreationRequest.getBusinessName();
        this.businessAddress = merchantCreationRequest.getBusinessAddress();
        this.phoneNumber = merchantCreationRequest.getPhoneNumber();
    }
    public Merchant(User user,String  businessName, String businessAddress, String phoneNumber ){
        this.userId = user.getId();
        this.businessName =businessName;
        this.businessAddress = businessAddress;
        this.phoneNumber = phoneNumber;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

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

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(LocalDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }
}

