package userManagement.services;


import userManagement.db.DataBaseConnection;
import userManagement.models.Merchant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MerchantService {

    private Connection connection ;

    public MerchantService() throws SQLException, ClassNotFoundException {
        this.connection = DataBaseConnection.getConnection();
    }

    public MerchantService(Connection connection) {
        this.connection = connection;
    }

    public boolean createMerchant(Merchant merchant) throws SQLException {

        String query = """
                        INSERT INTO merchants
                            (user_id, business_name, business_address,phone_number)
                            values (?, ?, ?, ?)
                       """;
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, merchant.getUserId());
        statement.setString(2, merchant.getBusinessName());
        statement.setString(3, merchant.getBusinessAddress());
        statement.setString(4, merchant.getPhoneNumber());
        statement.executeUpdate();
        System.out.println("Merchant Created successfully!");
        return true;
    }

    public Merchant getMerchant(String userId, String businessName) throws SQLException {
        String sql = "SELECT * FROM merchants WHERE user_id = ? AND business_name = ?";
        PreparedStatement pStatement = connection.prepareStatement(sql);
        pStatement.setString(1, userId);
        pStatement.setString(2,businessName);
        ResultSet rs = pStatement.executeQuery();
        Merchant merchant=null;
        if(rs.next()){
            merchant = new Merchant();
            System.out.println("A merchant with this user_id and business_name already exist.");
            merchant.setId( rs.getInt("id")) ;
            merchant.setBusinessName(rs.getString("business_name"));
            merchant.setBusinessAddress(rs.getString("business_address"));
            merchant.setPhoneNumber(rs.getString("phone_number"));
            merchant.setCreatedOn(rs.getTimestamp("created_on").toLocalDateTime());
            merchant.setUpdatedOn(rs.getTimestamp("updated_on").toLocalDateTime());
            return  merchant;
        }
        return merchant;
    }
}
