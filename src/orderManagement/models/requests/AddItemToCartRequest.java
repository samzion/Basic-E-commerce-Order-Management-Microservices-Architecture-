package orderManagement.models.requests;

public class AddItemToCartRequest {
    private int productId;
    private int quantity;
    private int cartId;

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

    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public static String validate(AddItemToCartRequest addItemToCartRequest){
        if(addItemToCartRequest == null){
            return "Request cannot be null";
        }
        if(addItemToCartRequest.getProductId() <= 0 || addItemToCartRequest.getQuantity() <= 0 ){
            return "Product id and quantity cannot be zero or negative";
        }
        return "Request okay!";
    }
}
