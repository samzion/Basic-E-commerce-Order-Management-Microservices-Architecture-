package orderManagement.services;

import orderManagement.models.entties.OrderItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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


    public boolean updateOrderItem(OrderItem orderItem, String status) throws SQLException {
        String sql = """
                UPDATE order_items
                SET status = ?,
                    updated_on = CURRENT_TIMESTAMP
                WHERE id = ?;
                """;
        try ( PreparedStatement pStatement = connection.prepareStatement(sql)){
            pStatement.setString(1,status);
            pStatement.setInt(2, orderItem.getId());


            int rowsUpdated = pStatement.executeUpdate();
            return rowsUpdated > 0; // true if stock updated successfully
        } catch (SQLException e) {
            System.err.println("Error updating stock: " + e.getMessage());
            return false;
        }
    }

    public OrderItem existingOrderItem(int orderItemId) throws SQLException {
        String sql = """
                SELECT *
                FROM order_items
                WHERE id = ?;
        """;
        PreparedStatement pStatement = connection.prepareStatement(sql);
        pStatement.setInt(1, orderItemId);
        ResultSet rs = pStatement.executeQuery();
        OrderItem orderItem= new OrderItem();
        if (rs.next()) {
            System.out.println("Order Item exists.");
            orderItem.setId(rs.getInt("id"));
            orderItem.setOrderId(rs.getInt("order_id"));
            orderItem.setProductId(rs.getInt("product_id"));
            orderItem.setQuantity(rs.getInt("quantity"));
            orderItem.setStatus(rs.getString("status"));
            orderItem.setPrice(rs.getDouble("price"));
            orderItem.setTotalAmount(rs.getDouble("total"));
            orderItem.setCreatedOn(rs.getTimestamp("created_on").toLocalDateTime());
            orderItem.setUpdatedOn(rs.getTimestamp("updated_on").toLocalDateTime());
            return orderItem;
        }
        return null;
    }

    public OrderItem existingOrderItemByMerchantId(int orderItemId, int merchantId) throws SQLException {
        String sql = """
                        SELECT
                            oi.id AS order_item_id, oi.order_id, oi.product_id, oi.quantity,
                            oi.status, oi.price, total, 
                            oi.created_on, oi.updated_on 
                        FROM order_items as oi
                        JOIN products p ON oi.product_id = p.id
                        WHERE p.merchant_id = ? AND oi.id = ?;
                """;
        PreparedStatement pStatement = connection.prepareStatement(sql);
        pStatement.setInt(2, orderItemId);
        pStatement.setInt(1, merchantId);
        ResultSet rs = pStatement.executeQuery();
        OrderItem orderItem = new OrderItem();
        if (rs.next()) {
            System.out.println("Order Item exists.");
            orderItem.setId(rs.getInt("order_item_id"));
            orderItem.setOrderId(rs.getInt("order_id"));
            orderItem.setProductId(rs.getInt("product_id"));
            orderItem.setQuantity(rs.getInt("quantity"));
            orderItem.setStatus(rs.getString("status"));
            orderItem.setPrice(rs.getDouble("price"));
            orderItem.setTotalAmount(rs.getDouble("total"));
            orderItem.setCreatedOn(rs.getTimestamp("created_on").toLocalDateTime());
            orderItem.setUpdatedOn(rs.getTimestamp("updated_on").toLocalDateTime());
            return orderItem;
        }
        return null;
    }

    public List<OrderItem> getOrderItemsByFilters(String status, Integer productId, Integer merchantId) {
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM order_items AS oi " +
                        "JOIN products AS p " +
                        "ON oi.product_id = p.id " +
                "WHERE  1=1 ");

        List<Object> params = new ArrayList<>();

        sql.append("AND p.merchant_id = ? ");
        params.add(merchantId);
        if (status!= null && !status.isEmpty()) {
            sql.append("AND status = ? ");
            params.add(status);
        }
        if (productId != null) {
            sql.append("AND product_id = ? ");
            params.add(productId);
        }
        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            List<OrderItem> orderItems = new ArrayList<>();
            while (rs.next()) {
                OrderItem orderItem = new OrderItem();
                System.out.println("Order Item exists.");
                orderItem.setId(rs.getInt("id"));
                orderItem.setOrderId(rs.getInt("order_id"));
                orderItem.setProductId(rs.getInt("product_id"));
                orderItem.setQuantity(rs.getInt("quantity"));
                orderItem.setStatus(rs.getString("status"));
                orderItem.setPrice(rs.getDouble("price"));
                orderItem.setTotalAmount(rs.getDouble("total"));
                orderItem.setCreatedOn(rs.getTimestamp("created_on").toLocalDateTime());
                orderItem.setUpdatedOn(rs.getTimestamp("updated_on").toLocalDateTime());
                orderItems.add(orderItem);
            }
            return orderItems;
        } catch (SQLException e) {
            System.out.println("Unknown error!");
            return null;
        }
    }
}


