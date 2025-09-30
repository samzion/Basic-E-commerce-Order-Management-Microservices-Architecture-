package paymentManagement.models.requests;

import paymentManagement.models.entities.MerchantPayment;

import java.util.List;

public class PayNowRequest {
    private double totalAmount;
    private List<MerchantPayment> merchantPayments;

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<MerchantPayment> getMerchantPayments() {
        return merchantPayments;
    }

    public void setMerchantPayments(List<MerchantPayment> merchantPayments) {
        this.merchantPayments = merchantPayments;
    }

    public static String validate(PayNowRequest payNowRequest){
        if(payNowRequest == null){
            return "PayNow request cannot be null";
        }
        if(payNowRequest.getTotalAmount() <= 0){
            return "Total amount cannot be 0 or negative";
        }
        if( payNowRequest.getMerchantPayments()==null
                || payNowRequest.getMerchantPayments().isEmpty()
                || payNowRequest.getMerchantPayments().getFirst().getMerchantId()==0){
            return "Merchant payment details cannot be null or empty";
        }
        double sum=0;
        for(MerchantPayment merchantPayment : payNowRequest.getMerchantPayments()){
            sum+=merchantPayment.getAmount();
        }
        if(payNowRequest.getTotalAmount() != sum){
            return "Merchant payment details inconsistent";
        }
        return "Pay Now request okay!";
    }


}
