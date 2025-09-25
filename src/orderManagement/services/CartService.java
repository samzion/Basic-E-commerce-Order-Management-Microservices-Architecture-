package orderManagement.services;

import orderManagement.models.entties.Cart;

import java.sql.*;

public class CartService {
    Connection connection;
    public CartService(Connection connection){
        this.connection = connection;
    }
    public Cart getActiveCartDetails(int userId) throws SQLException {
        String sql = """ 
                SELECT *
                FROM carts
                WHERE user_id = ? AND status = ?
                LIMIT 1
                ;
                """;
        Cart cart = new Cart();
        try (PreparedStatement pStatement = connection.prepareStatement(sql)) {
            pStatement.setInt(1, userId);
            pStatement.setString(2, "OPEN");
            try (ResultSet rs = pStatement.executeQuery()) {
                if (rs.next()) {
                    cart.setId(rs.getInt("id"));
                    cart.setUserId(rs.getInt("user_id"));
                    cart.setStatus(rs.getString("status"));
                    cart.setCreatedOn(rs.getTimestamp("created_on").toLocalDateTime());
                    cart.setUpdatedOn(rs.getTimestamp("updated_on").toLocalDateTime());
                }

            }
        } catch (SQLException e) {
            System.out.println("Unknown error");
            return null; //error occurred
        }
        return cart; //cart found or not found
    }

    public Cart getCartDetails(int userId, int cartId) throws SQLException {
        String sql = """ 
                SELECT *
                FROM carts
                WHERE user_id = ? AND id = ?
                ;
                """;
        Cart cart = new Cart();
        try (PreparedStatement pStatement = connection.prepareStatement(sql)) {
            pStatement.setInt(1, userId);
            pStatement.setInt(2, cartId);
            try (ResultSet rs = pStatement.executeQuery()) {
                if (rs.next()) {
                    cart.setId(rs.getInt("id"));
                    cart.setUserId(rs.getInt("user_id"));
                    cart.setStatus(rs.getString("status"));
                    cart.setCreatedOn(rs.getTimestamp("created_on").toLocalDateTime());
                    cart.setUpdatedOn(rs.getTimestamp("updated_on").toLocalDateTime());
                }

            }
        } catch (SQLException e) {
            System.out.println("Unknown error");
            return null; //error occurred
        }
        return cart; //cart found or not found
    }

    public Cart createCart(int userId){
        String sql = "INSERT INTO carts (user_id) " +
                "VALUES (?)";
        Cart cart = new Cart();
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int productId = keys.getInt(1);
                cart.setId(productId);
            }
            return cart;
        } catch (SQLException e) {
            System.out.println("Unknown error");
            return null;
        }

    }
}
