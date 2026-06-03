package orderManagement.services;

import orderManagement.models.entties.Order;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
                    transaction_id = ?,
                    updated_on = CURRENT_TIMESTAMP
                WHERE id = ?;
                """;
        try ( PreparedStatement pStatement = connection.prepareStatement(sql)){

            pStatement.setLong(1, order.getTransactionId());
            pStatement.setInt(2, order.getId());


            int rowsUpdated = pStatement.executeUpdate();
            return rowsUpdated > 0; // true if stock updated successfully
        } catch (SQLException e) {
            System.err.println("Error updating stock: " + e.getMessage());
            return false;
        }
    }

    public List<Order> getOrders(Integer orderId, String status, Integer userId){
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM orders AS o " +
                        "WHERE  1=1 ");

        List<Object> params = new ArrayList<>();
        if (orderId!= null && !(orderId ==0)) {
            sql.append("AND id = ? ");
            params.add(orderId);
        }

        if (status!= null && !status.isEmpty()) {
            sql.append("AND status = ? ");
            params.add(status);
        }

        if (userId!= null && !(userId ==0)) {
            sql.append("AND user_id = ? ");
            params.add(userId);
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            List<Order> orders = new ArrayList<>();
            while (rs.next()) {
                Order order = new Order();
                System.out.println("Order Item exists.");
                order.setId(rs.getInt("id"));
                order.setUserId(rs.getInt("user_id"));
                order.setStatus(rs.getString("status"));
                order.setTransactionId(rs.getLong("transaction_id"));
                order.setCreatedOn(rs.getTimestamp("created_on").toLocalDateTime());
                order.setUpdatedOn(rs.getTimestamp("updated_on").toLocalDateTime());
                orders.add(order);
            }
            return orders;
        } catch (SQLException e) {
            System.out.println("Unknown error!");
            return null;
        }
    }
}
