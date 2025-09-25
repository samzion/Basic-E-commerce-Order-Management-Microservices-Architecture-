package orderManagement.services;

import orderManagement.models.entties.CartItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CartItemService {
    Connection connection;
    public CartItemService(Connection connection){
        this.connection = connection;
    }

    public CartItem getCartItem(int cartId, int productId){
        String sql = "SELECT * FROM cart_items WHERE cart_id = ? AND product_id = ?";
        CartItem cartItem = new CartItem();
        try (
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1,cartId);
            statement.setInt(2,productId);
            ResultSet rs= statement.executeQuery();
            if(rs.next()){
                cartItem.setId(rs.getInt("id"));
                cartItem.setCartId(rs.getInt("cart_id"));
                cartItem.setProductId(rs.getInt("product_id"));
                cartItem.setQuantity(rs.getInt("quantity"));
                cartItem.setPriceAtAdd(rs.getDouble("price_at_add"));
                cartItem.setCreatedOn(rs.getTimestamp("created_on").toLocalDateTime());
                cartItem.setUpdatedOn(rs.getTimestamp("updated_on").toLocalDateTime());
            }
        } catch (SQLException e) {
            System.out.println("unknown error");
            return null;
        }
        return cartItem;
    }

    public boolean addCartItem(CartItem cartItem) throws SQLException {
        String insertSql = "INSERT INTO cart_items " +
                "(cart_id, product_id, price_at_add, quantity) VALUES (?, ?, ?, ?)";
        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
            insertStmt.setInt(1, cartItem.getCartId());
            insertStmt.setInt(2, cartItem.getProductId());
            insertStmt.setDouble(3, cartItem.getPriceAtAdd());
            insertStmt.setInt(4, cartItem.getQuantity());
            insertStmt.executeUpdate();
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
