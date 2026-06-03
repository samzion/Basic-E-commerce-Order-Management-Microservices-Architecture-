package orderManagement.models.responses;

import com.google.gson.Gson;

public class PaymentResponse {
    private int status;
    private Long transactionId;
    private String message;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static PaymentResponse fromJson(String json) {
        return new Gson().fromJson(json, PaymentResponse.class);
    }
}