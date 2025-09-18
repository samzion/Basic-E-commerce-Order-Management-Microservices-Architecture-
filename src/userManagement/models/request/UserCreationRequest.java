package userManagement.models.request;


import userManagement.models.User;
import userManagement.utilities.PasswordUtil;

public class UserCreationRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private  String gender;
    private String password;

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static String validate(UserCreationRequest userCreationRequest){
        if(userCreationRequest == null){
            return "User creation request cannot be null";
        }
        if(userCreationRequest.getFirstName() == null || userCreationRequest.getFirstName().isEmpty()){
            return "First name cannot be null or empty";
        }
        if(userCreationRequest.getLastName() == null || userCreationRequest.getLastName().isEmpty()){
            return "Last name cannot be null or empty";
        }
        if(userCreationRequest.getGender() == null || userCreationRequest.getGender().isEmpty()){
            return "Gender cannot be null or empty";
        }
        if(!(userCreationRequest.getGender().equalsIgnoreCase("M") || userCreationRequest.getGender().equalsIgnoreCase("F"))){
            return "Gender can either be M or F";
        }
        if(userCreationRequest.getAddress() == null || userCreationRequest.getAddress().isEmpty()){
            return "Address cannot be null or empty";
        }
        if(userCreationRequest.getPassword() == null ||userCreationRequest.getPassword().isEmpty()){
            return "Password cannot be null or empty";
        }
        final String regularExpression = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{8,100}$";
        if(!userCreationRequest.getPassword().matches(regularExpression)){
            return "Password should be at least 8 characters and contains at least one uppercase, at least one lower case and at least a number.";
        }

        return "User creation request okay!";
    }

    public static User createUserObject(UserCreationRequest userCreationRequest){
        User user = new User();

        user.setFirstName(userCreationRequest.getFirstName());
        user.setLastName(userCreationRequest.getLastName());
        user.setEmail(userCreationRequest.getEmail());
        user.setAddress(userCreationRequest.getAddress());
        user.setGender(userCreationRequest.getGender());
        String passwordHash = PasswordUtil.hashPassword(userCreationRequest.getPassword());
        user.setPasswordHash(passwordHash);
        return user;
    }

}
