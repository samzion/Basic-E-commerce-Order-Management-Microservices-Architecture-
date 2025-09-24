package orderManagement.services;

import orderManagement.models.entties.Product;

import java.sql.*;

public class ProductService {
    public Connection connection;

    public ProductService(Connection connection){
        this.connection = connection;
    }

    public Product getProductByNameAndMerchant(String name, int merchantId) throws SQLException {

        String sql = """ 
                SELECT *
                FROM products
                WHERE name = ? AND merchant_id = ?
                ;
                """;
        PreparedStatement pStatement = connection.prepareStatement(sql);
        pStatement.setInt(2, merchantId);
        pStatement.setString(1, name);
        ResultSet rs = pStatement.executeQuery();
        Product product = new Product();
        if(rs.next()){
            System.out.println("A user with this email and password exist.");
            product.setId( rs.getInt("id"));
            product.setMerchantId( rs.getInt("merchant_id"));
            product.setName(rs.getString("name"));
            product.setCategory(rs.getString("category"));
            product.setPrice(rs.getDouble("price"));
            product.setStock(rs.getInt("stock"));
            product.setCreatedOn(rs.getTimestamp("created_on").toLocalDateTime());
            product.setUpdatedOn(rs.getTimestamp("updated_on").toLocalDateTime());
            return  product;
        }
        return null;
    }

    public Product insertProduct(Product product) throws SQLException {

        String sql = "INSERT INTO products (merchant_id, name, category, price, stock) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, product.getMerchantId());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getCategory());
            stmt.setDouble(4, product.getPrice());  // assuming price is BigDecimal
            stmt.setInt(5, product.getStock());
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int productId = keys.getInt(1);
                product.setId(productId);
        }
            return product;
        }
    }

    public Product ExistingProduct(int productId) throws SQLException {

        String sql = """ 
                SELECT *
                FROM products
                WHERE id = ?
                ;
                """;
        PreparedStatement pStatement = connection.prepareStatement(sql);
        pStatement.setInt(1, productId);
        ResultSet rs = pStatement.executeQuery();
        Product product = new Product();
        if(rs.next()){
            System.out.println("A user with this email and password exist.");
            product.setId( rs.getInt("id"));
            product.setMerchantId( rs.getInt("merchant_id"));
            product.setName(rs.getString("name"));
            product.setCategory(rs.getString("category"));
            product.setPrice(rs.getDouble("price"));
            product.setStock(rs.getInt("stock"));
            product.setCreatedOn(rs.getTimestamp("created_on").toLocalDateTime());
            product.setUpdatedOn(rs.getTimestamp("updated_on").toLocalDateTime());
            return  product;
        }
        return null;
    }

    public boolean reduceStock(int productId, int quantity) {
        String sql =
                "UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?";

        try ( PreparedStatement pStatement = connection.prepareStatement(sql)){

            pStatement.setInt(1, quantity);   // subtract this amount
            pStatement.setInt(2, productId);  // target product
            pStatement.setInt(3, quantity);   // ensure enough stock

            int rowsUpdated = pStatement.executeUpdate();
            return rowsUpdated > 0; // true if stock updated successfully
        } catch (SQLException e) {
            System.err.println("Error updating stock: " + e.getMessage());
            return false;
        }
    }

    public boolean increaseStock(int productId, int quantity) {
        String sql =
                "UPDATE products SET stock = stock + ? WHERE id = ?";

        try ( PreparedStatement pStatement = connection.prepareStatement(sql)){

            pStatement.setInt(1, quantity);   // subtract this amount
            pStatement.setInt(2, productId);  // target product

            int rowsUpdated = pStatement.executeUpdate();
            return rowsUpdated > 0; // true if stock updated successfully
        } catch (SQLException e) {
            System.err.println("Error updating stock: " + e.getMessage());
            return false;
        }
    }
}
