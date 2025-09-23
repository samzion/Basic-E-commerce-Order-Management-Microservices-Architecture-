package userManagement.services;

import userManagement.db.DataBaseConnection;
import userManagement.models.Role;
import userManagement.models.User;
import userManagement.models.request.UserCreationRequest;
import userManagement.utilities.PasswordUtil;

import java.sql.*;
import java.time.LocalDateTime;

public class UserService {
    private Connection connection ;

    public UserService() throws SQLException, ClassNotFoundException {
        this.connection = DataBaseConnection.getConnection();
    }

    public UserService(Connection connection) {
        this.connection = connection;
    }

    public User createUser(UserCreationRequest userCreationRequest) throws SQLException {
        User user = UserCreationRequest.createUserObject(userCreationRequest);
        if(createUser(user)){
            return getUserDetailsByEmail(user.getEmail());
        }
        return  null;

    }

    private User getUserDetailsByEmail(String email) throws SQLException {
        String queryLoginDetails = "SELECT * FROM users WHERE email = ?";
        PreparedStatement pStatement = connection.prepareStatement(queryLoginDetails);
        pStatement.setString(1, email);
        ResultSet rs = pStatement.executeQuery();
        User user=null;
        if(rs.next()){
            user = new User();
            System.out.println("A user with this email and password exist.");
            user.setId( rs.getInt("id")) ;
            user.setFirstName(rs.getString("firstname"));
            user.setLastName(rs.getString("lastname"));
            user.setAddress(rs.getString("address"));
            user.setEmail(rs.getString("email"));
            user.setGender(rs.getString("gender"));
            user.setUserToken(rs.getString("user_token"));
            user.setPasswordHash(rs.getString("password_hash"));
            user.setRole(Role.valueOf(rs.getString("role")));
            user.setCreatedOn(rs.getTimestamp("created_on").toLocalDateTime());
            user.setUpdatedOn(rs.getTimestamp("updated_on").toLocalDateTime());
            return  user;
        }
        return user;
    }


    public boolean createUser(User user) throws SQLException {
        boolean flag = false;
        //check if email exist in database before
        String checkEmail = "SELECT * FROM users WHERE email = ?";
        PreparedStatement pStatement = connection.prepareStatement(checkEmail);
        pStatement.setString(1, user.getEmail());
        ResultSet rs = pStatement.executeQuery();

        if(rs.next()){
            System.out.println("A user with this email exist. ");
            return  false;
        }

        String query = """
                        INSERT INTO users
                            (firstname, lastname, gender, email, address, password_hash, created_on, updated_on)
                            values (?, ?, ?, ?, ?,?,?,?)
                       """;
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, user.getFirstName());
        statement.setString(2, user.getLastName());
        statement.setString(3, user.getGender());
        statement.setString(4, user.getEmail());
        statement.setString(5, user.getAddress());
        statement.setString(6, user.getPasswordHash());
        statement.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
        statement.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
        statement.executeUpdate();
        System.out.println("User Created successfully!");
        return true;
    }

    public User confirmUserLoginDetails(String email, String password) throws SQLException {
        String queryLoginDetails = "SELECT * FROM users WHERE email = ? ";
        PreparedStatement pStatement = connection.prepareStatement(queryLoginDetails);
        pStatement.setString(1, email);

        ResultSet rs = pStatement.executeQuery();

        User user =  null;
        if(!rs.next()){
            return user;
        }

        user = new User();
        System.out.println("A user with this email and password exist.");
        user.setId( rs.getInt("id")) ;
        user.setFirstName(rs.getString("firstname"));
        user.setLastName(rs.getString("lastname"));
        user.setAddress(rs.getString("address"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setGender(rs.getString("gender"));
        user.setRole(Role.valueOf(rs.getString("role")));
        user.setCreatedOn(rs.getTimestamp("created_on").toLocalDateTime());
        user.setUpdatedOn(rs.getTimestamp("updated_on").toLocalDateTime());
        String passwordHash = user.getPasswordHash();
        if( !PasswordUtil.checkPassword(password, passwordHash)){
            return  null;
        }
        return  user;
    }

//    public User getUserByUserId(int userId) throws SQLException {
//        String queryLoginDetails = "SELECT * FROM users WHERE id = ?";
//        PreparedStatement pStatement = connection.prepareStatement(queryLoginDetails);
//        pStatement.setInt(1,userId);
//
//        ResultSet rs = pStatement.executeQuery();
//
//        User user =  null;
//
//        if(rs.next()){
//            user = new User();
//            System.out.println("A user with this email and password exist.");
//            user.setId( rs.getInt("id")) ;
//            user.setFirstName(rs.getString("firstname"));
//            user.setLastName(rs.getString("lastname"));
//            user.setAddress(rs.getString("address"));
//            user.setEmail(rs.getString("email"));
//            user.setPassword(rs.getString("password"));
//            user.setGender(rs.getString("gender"));
//            user.setCreatedOn(rs.getTimestamp("created_on").toLocalDateTime());
//            user.setUpdatedOn(rs.getTimestamp("updated_on").toLocalDateTime());
//            return  user;
//        }
//        return user;
//    }

    public String confirmUserEmail(String email) throws SQLException {
        String queryEmail = "SELECT * FROM users WHERE email = ?";
        PreparedStatement pStatementEmail = connection.prepareStatement(queryEmail);
        pStatementEmail.setString(1, email);
        ResultSet rsEmail = pStatementEmail.executeQuery();
        User user = new User();
        if(rsEmail == null){
            return "A user with this email does not exist";
        }
        return "A user with this email exist";
    }

    public User getUserDetailsByUserToken(String userToken) throws SQLException {
        String queryLoginDetails = "SELECT * FROM users WHERE user_token = ?";
        PreparedStatement pStatement = connection.prepareStatement(queryLoginDetails);
        pStatement.setString(1, userToken);
        ResultSet rs = pStatement.executeQuery();
        User user=null;
        if(rs.next()){
            user = new User();
            System.out.println("A user with this email and password exist.");
            user.setId( rs.getInt("id")) ;
            user.setFirstName(rs.getString("firstname"));
            user.setLastName(rs.getString("lastname"));
            user.setAddress(rs.getString("address"));
            user.setEmail(rs.getString("email"));
            user.setGender(rs.getString("gender"));
            user.setRole(Role.valueOf(rs.getString("role")));
            user.setCreatedOn(rs.getTimestamp("created_on").toLocalDateTime());
            user.setUpdatedOn(rs.getTimestamp("updated_on").toLocalDateTime());
            return  user;
        }
        return user;
    }

    public boolean verifyUserIsMerchant(String userToken) throws SQLException {
        String queryLoginDetails = "SELECT * FROM users WHERE user_token = ? AND role = ?";
        PreparedStatement pStatement = connection.prepareStatement(queryLoginDetails);
        pStatement.setString(1, userToken);
        pStatement.setString(1, String.valueOf(Role.MERCHANT));
        ResultSet rs = pStatement.executeQuery();
        return  rs.next();
    }
//TODO: handle verification request for registered users and merchants
    public void updateToken(User existingUser) throws SQLException {
        String sql = "UPDATE users SET user_token = ? WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, existingUser.getUserToken());
        statement.setInt(2, existingUser.getId());
        statement.executeUpdate();
    }


    public void updateRole(User user, Role role) throws SQLException {
        String query = """
                        UPDATE users
                        SET role = ?,
                            updated_on = CURRENT_TIMESTAMP
                        WHERE id = ?;
                        """;

        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, String.valueOf(role));
        statement.setInt(2, user.getId());
        statement.executeUpdate();
        System.out.println("User id " + user.getId() + "updated to " + role + "successfully!");
    }
}
