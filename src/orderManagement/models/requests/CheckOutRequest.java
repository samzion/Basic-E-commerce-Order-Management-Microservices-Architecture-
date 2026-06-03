package orderManagement.models.requests;

public class CheckOutRequest {
    private  int cartId;
    private boolean payLater;

    public boolean isPayLater() {
        return payLater;
    }

    public void setPayLater(boolean payLater) {
        this.payLater = payLater;
    }

    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public static String validate(CheckOutRequest checkOutRequest){
        if(checkOutRequest == null){
            return "Request cannot be null";
        }
        if(checkOutRequest.getCartId() <= 0){
            return "Cart id cannot be zero or negative";
        }
        return "Request okay!";
    }
}
