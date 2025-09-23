package paymentManagement.models.entities;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private  String gender;
    private String password;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
    private String userToken;

    public User(){

    }

    public User(User user){
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.gender = user.getGender();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.address = user.getAddress();
        this.createdOn = user.getCreatedOn();
        this.updatedOn = user.getUpdatedOn();
        this.userToken=user.getUserToken();
    }

    public User(String firstName, String lastName, String gender, String email, String address, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.email = email;
        this.password = password;
        this.address = address;
    }


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGender() {
        return gender;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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


    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }


    public  String toString(){
        return String.format("firstName: %s, \nlastName: %s, \ngender: %s, \nemail: %s",firstName,lastName,gender,email);
    }

//    public void createAccount(String bank, String accountNumber){
//        String name = this.firstName + " " + this.lastName;
//        Account account = new Account(this, accountNumber, bank);
//        this.accountList.add(account);
//        System.out.println("New account has been created for " + name + " with account number "+ accountNumber);
//    }
//
//    public Account getAccount(String accountNumber) throws Exception {
//        for(Account account:accountList){
//            if(accountNumber.equals(account.getAccNo())){
//                return account;
//            }
//        }
//        throw new Exception("Account number not found. Please enter valid account number.");
//    }
}
