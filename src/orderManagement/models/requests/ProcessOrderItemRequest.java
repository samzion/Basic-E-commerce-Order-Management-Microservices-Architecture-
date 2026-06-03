package orderManagement.models.requests;

public class ProcessOrderItemRequest {
    private int id;
    private String status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static String validate(ProcessOrderItemRequest processOrderItemRequest) {
        if (processOrderItemRequest == null) {
            return "Request cannot be null";
        }
        if (processOrderItemRequest.getId() <= 0) {
            return "Order item id cannot be zero or negative";
        }
        if (!(processOrderItemRequest.getStatus().equalsIgnoreCase("PROCESSING")
                || processOrderItemRequest.getStatus().equalsIgnoreCase("SHIPPED")
                || processOrderItemRequest.getStatus().equalsIgnoreCase("CANCELLED")) ){
            return "Order status can be only any of the following: PROCESSING, SHIPPED OR CANCELLED";
        }
        return "Request okay!";
    }
}
