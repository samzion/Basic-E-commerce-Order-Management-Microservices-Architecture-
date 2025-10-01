package orderManagement.services;

import orderManagement.models.entties.OrderItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderItemService {
    Connection connection;
    public OrderItemService(Connection connection){
        this.connection = connection;
    }

    public OrderItem addOrderItem(OrderItem orderItem) throws SQLException {
        String insertSql = "INSERT INTO order_items " +
                "(order_id, product_id, price, quantity) VALUES (?, ?, ?, ?)";
        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
            insertStmt.setInt(1, orderItem.getOrderId());
            insertStmt.setInt(2, orderItem.getProductId());
            insertStmt.setDouble(3, orderItem.getPrice());
            insertStmt.setInt(4, orderItem.getQuantity());
            insertStmt.executeUpdate();
            ResultSet keys = insertStmt.getGeneratedKeys();
            if (keys.next()) {
                int orderId = keys.getInt(1);
                orderItem.setId(orderId);
                return orderItem;
            }
        }
        return orderItem;
    }

    public boolean copyCartItemsToOrderItems(int orderId, int cartId) {
        String query = """
            INSERT INTO order_items (order_id, product_id, quantity, price, status, created_on, updated_on)
            SELECT ?, ci.product_id, ci.quantity, p.price, 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
            FROM cart_items ci
            JOIN products p ON ci.product_id = p.id
            WHERE ci.cart_id = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, orderId); // new order ID
            stmt.setInt(2, cartId);  // cart being checked out

            int rowsInserted = stmt.executeUpdate();
            System.out.println(rowsInserted + " items copied from cart to order_items.");

            return rowsInserted > 0;
        } catch (SQLException e) {
            System.err.println("Error copying cart items to order_items: " + e.getMessage());
            return false;
        }
    }

    public boolean confirmOrderItems(int orderId) {
        String query = """
            UPDATE order_items
            SET status = 'CONFIRMED',
                updated_on = CURRENT_TIMESTAMP
            WHERE order_id = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            int rowsUpdated = stmt.executeUpdate();
            System.out.println(rowsUpdated + " order_items updated to CONFIRMED.");
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.err.println("Error updating order_items to CONFIRMED: " + e.getMessage());
            return false;
        }
    }





    public boolean updateOrderItem(OrderItem orderItem) throws SQLException {
        String sql = """
                UPDATE order_items
                SET status = 'CONFIRMED',
                    updated_on = CURRENT_TIMESTAMP
                WHERE id = ?;
                """;
        try ( PreparedStatement pStatement = connection.prepareStatement(sql)){

            pStatement.setInt(1, orderItem.getId());


            int rowsUpdated = pStatement.executeUpdate();
            return rowsUpdated > 0; // true if stock updated successfully
        } catch (SQLException e) {
            System.err.println("Error updating stock: " + e.getMessage());
            return false;
        }
    }


}
