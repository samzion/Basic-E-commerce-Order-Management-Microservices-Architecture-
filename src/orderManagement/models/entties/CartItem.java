package orderManagement.models.entties;

import com.google.gson.Gson;

import java.time.LocalDateTime;

public class CartItem {
    private int id;
    private int cartId;
    private int productId;
    private int quantity;
    private double priceAtAdd;
    private String status;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public double getPriceAtAdd() {
        return priceAtAdd;
    }

    public void setPriceAtAdd(double priceAtAdd) {
        this.priceAtAdd = priceAtAdd;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(LocalDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    public static CartItem fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, CartItem.class);
    }
}
