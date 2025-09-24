package orderManagement.models.entties;

import com.google.gson.Gson;
import orderManagement.requests.InsertProductRequest;

import java.time.LocalDateTime;

public class Product {
    private int id;
    private int merchantId;
    private String name;
    private String category;
    private double price;
    private int stock;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;

    public Product() {
    }

    public Product(InsertProductRequest insertProductRequest, int merchantId) {
        this.merchantId = merchantId;
        this.name = insertProductRequest.getName();
        this.category = insertProductRequest.getCategory();
        this.price = insertProductRequest.getPrice();
        this.stock = insertProductRequest.getStock();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
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

    public static Product fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Product.class);
    }

}
