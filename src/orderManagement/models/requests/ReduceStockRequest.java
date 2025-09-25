package orderManagement.models.requests;

public class ReduceStockRequest {

    private int productId;
    private int quantity;

    public ReduceStockRequest() {
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

    public static String validate(ReduceStockRequest reduceStockRequest) {
        if(reduceStockRequest == null){
            return "Request cannot be null";
        }
        if(reduceStockRequest.getProductId() <= 0 || reduceStockRequest.quantity <= 0){
            return "Quantity or product Id cannot be zero or negative";
        }
        return "Request okay!";
    }
}
