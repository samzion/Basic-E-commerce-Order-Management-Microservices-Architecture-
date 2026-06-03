package orderManagement.models.entties;

public class MerchantPayment {
        private int merchantId;
        private double amount;
        // getters and setters

    public int getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
