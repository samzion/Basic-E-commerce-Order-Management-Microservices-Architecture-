package orderManagement.models.requests;
public class PayForItemRequest {
    private int productId;
    private int quantity;
    private boolean payLater;

    public boolean isPayLater() {
        return payLater;
    }

    public void setPayLater(boolean payLater) {
        this.payLater = payLater;
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

