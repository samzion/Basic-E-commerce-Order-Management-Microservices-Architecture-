package userManagement.services;


import userManagement.db.DataBaseConnection;
import userManagement.models.Merchant;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
}
