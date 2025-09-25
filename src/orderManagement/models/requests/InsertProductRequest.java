package orderManagement.models.requests;

public class InsertProductRequest {
    private String name;
    private String category;
    private double price;
    private int stock;

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

    public static String validate(InsertProductRequest insertProductRequest) {
        if(insertProductRequest == null){
            return "Insert Product request cannot be null";
        }
        if(insertProductRequest.getName() == null || insertProductRequest.getName().isEmpty()){
            return "Product name cannot be null or empty";
        }
        if(insertProductRequest.getCategory() == null || insertProductRequest.getCategory().isEmpty()){
            return "Category cannot be null or empty";
        }
        if(insertProductRequest.getPrice() == 0 ){
            return "Price cannot be zero";
        }

        return "Insert product request okay!";
    }
}
