package orderManagement.services;

import orderManagement.models.entties.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
            System.out.println("Product exists.");
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

    public List<Product> listAllAvailProducts() throws SQLException {
        String sql = "SELECT *  FROM products WHERE stock >0; ";
        PreparedStatement pStatement = connection.prepareStatement(sql);
        ResultSet rs = pStatement.executeQuery();
        List<Product> products = new ArrayList<>();
        while(rs.next()){
            Product product = new Product();
            System.out.println("Available products exist.");
            product.setId( rs.getInt("id"));
            product.setMerchantId( rs.getInt("merchant_id"));
            product.setName(rs.getString("name"));
            product.setCategory(rs.getString("category"));
            product.setPrice(rs.getDouble("price"));
            product.setStock(rs.getInt("stock"));
            product.setCreatedOn(rs.getTimestamp("created_on").toLocalDateTime());
            product.setUpdatedOn(rs.getTimestamp("updated_on").toLocalDateTime());
            products.add(product);
        }
        return products;
    }

    public List<Product> getProductsByCategory(String category) throws SQLException {
        String sql = "SELECT * FROM products WHERE category = ? AND stock > 0";
        PreparedStatement pStatement = connection.prepareStatement(sql);
        pStatement.setString(1, category);
        ResultSet rs = pStatement.executeQuery();
        List<Product> products = new ArrayList<>();
        while(rs.next()){
            Product product = new Product();
            System.out.println("Available products exist.");
            product.setId( rs.getInt("id"));
            product.setMerchantId( rs.getInt("merchant_id"));
            product.setName(rs.getString("name"));
            product.setCategory(rs.getString("category"));
            product.setPrice(rs.getDouble("price"));
            product.setStock(rs.getInt("stock"));
            product.setCreatedOn(rs.getTimestamp("created_on").toLocalDateTime());
            product.setUpdatedOn(rs.getTimestamp("updated_on").toLocalDateTime());
            products.add(product);
        }
        return products;
    }

    public List<Product> getProductsByFilters(String category, Integer merchantId, String name, 
                                              Double minPrice, Double maxPrice, Boolean inStock) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, name, category, merchant_id, stock, price " +
                        "FROM products WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (category != null && !category.isEmpty()) {
            sql.append("AND category = ? ");
            params.add(category);
        }
        if (merchantId != null) {
            sql.append("AND merchant_id = ? ");
            params.add(merchantId);
        }
        if (name != null && !name.isEmpty()) {
            sql.append("AND LOWER(name) LIKE ? ");
            params.add("%" + name.toLowerCase() + "%");
        }
        if (minPrice != null) {
            sql.append("AND price >= ? ");
            params.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append("AND price <= ? ");
            params.add(maxPrice);
        }
        if (inStock != null && inStock) {
            sql.append("AND stock > 0 ");
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            List<Product> products = new ArrayList<>();
            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setName(rs.getString("name"));
                product.setCategory(rs.getString("category"));
                product.setMerchantId(rs.getInt("merchant_id"));
                product.setStock(rs.getInt("stock"));
                product.setPrice(rs.getDouble("price"));
                products.add(product);
            }
            return products;
        } catch (SQLException e) {
            System.out.println("Unknown error!");
           return null;
        }
    }


    public Product sufficientStockProduct(int productId, int quantity) throws SQLException {

        String sql = """ 
                SELECT *
                FROM products
                WHERE id = ? AND
                    quantity >= ?
                ;
                """;
        PreparedStatement pStatement = connection.prepareStatement(sql);
        pStatement.setInt(1, productId);
        pStatement.setInt(2, quantity);
        ResultSet rs = pStatement.executeQuery();
        Product product = new Product();
        if (rs.next()) {
            System.out.println("Product exists.");
            product.setId(rs.getInt("id"));
            product.setMerchantId(rs.getInt("merchant_id"));
            product.setName(rs.getString("name"));
            product.setCategory(rs.getString("category"));
            product.setPrice(rs.getDouble("price"));
            product.setStock(rs.getInt("stock"));
            product.setCreatedOn(rs.getTimestamp("created_on").toLocalDateTime());
            product.setUpdatedOn(rs.getTimestamp("updated_on").toLocalDateTime());
            return product;
        }
        return null;
    }
}
