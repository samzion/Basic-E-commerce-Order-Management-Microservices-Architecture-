package userManagement.models.request;

public class UserLoginRequest {
    private String email;
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static String validate(UserLoginRequest userLoginRequest){
        if(userLoginRequest == null){
            return "User login request cannot be null";
        }
        if(userLoginRequest.getPassword() == null ||userLoginRequest.getPassword().isEmpty()){
            return "Password cannot be null or empty";
        }
        if(userLoginRequest.getEmail() == null ||userLoginRequest.getEmail().isEmpty()){
            return "Email cannot be null or empty";
        }

        return "User login request okay!";
    }
}
