package orderManagement.models.responses;

public class CartItemResponse {
    private boolean success;
    private String message;

    // cart item details
    private int cartId;
    private int productId;
    private int quantity;

    // product details
    private String productName;
    private double price;
    private String category;
    private int merchantId;

    public CartItemResponse(boolean success, String message,
                            int cartId, int productId, int quantity,
                            String productName, double price, String category, int merchantId) {
        this.success = success;
        this.message = message;
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
        this.productName = productName;
        this.price = price;
        this.category = category;
        this.merchantId = merchantId;
    }

    // getters & setters

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }
}
