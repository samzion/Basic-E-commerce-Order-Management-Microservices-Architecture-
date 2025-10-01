package orderManagement.services;

import orderManagement.models.entties.Order;

import java.sql.*;

public class OrderService {
    Connection connection;
    public OrderService(Connection connection){
        this.connection = connection;
    }
    public Order createOrder(int userId){
        String sql = "INSERT INTO orders (user_id) " +
                "VALUES (?)";
        Order order = new Order();
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int productId = keys.getInt(1);
                order.setId(productId);
            }
            return order;
        } catch (SQLException e) {
            System.out.println("Unknown error");
            return null;
        }
    }

    public boolean updateOrder(Order order) throws SQLException {
        String sql = """
                UPDATE orders
                SET status = 'CONFIRMED',
                    updated_on = CURRENT_TIMESTAMP
                WHERE id = ?;
                """;
        try ( PreparedStatement pStatement = connection.prepareStatement(sql)){

            pStatement.setInt(1, order.getId());


            int rowsUpdated = pStatement.executeUpdate();
            return rowsUpdated > 0; // true if stock updated successfully
        } catch (SQLException e) {
            System.err.println("Error updating stock: " + e.getMessage());
            return false;
        }
    }
}
