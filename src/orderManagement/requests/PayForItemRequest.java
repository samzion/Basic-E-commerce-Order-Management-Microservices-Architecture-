package orderManagement.requests;

public class PayForItemRequest {
    private int productId;
    private int quantity;

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

    public static String validate(PayForItemRequest payForItemRequest){
        if(payForItemRequest == null){
            return "Request cannot be null";
        }
        if(payForItemRequest.getProductId() <= 0 || payForItemRequest.getQuantity() <= 0 ){
            return "Product id and quantity cannot be zero or negative";
        }
        return "Request okay!";
    }
}
