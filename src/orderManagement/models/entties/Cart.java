package orderManagement.models.entties;

import com.google.gson.Gson;

import java.time.LocalDateTime;

public class Cart {
    private int id;
    private int userId;
    private String status;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;

    public Cart(){

    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    public static Cart fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Cart.class);
    }
}
