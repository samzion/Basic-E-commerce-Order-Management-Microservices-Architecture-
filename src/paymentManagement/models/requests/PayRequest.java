package paymentManagement.models.requests;

import paymentManagement.models.entities.MerchantPayment;

import java.util.List;

public class PayRequest {
    private double totalAmount;
    private List<MerchantPayment> merchantPayments;
    private boolean payLater;

    public boolean isPayLater() {
        return payLater;
    }

    public void setPayLater(boolean payLater) {
        this.payLater = payLater;
    }

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

    public static String validate(PayRequest payRequest){
        if(payRequest == null){
            return "PayNow request cannot be null";
        }
        if(payRequest.getTotalAmount() <= 0){
            return "Total amount cannot be 0 or negative";
        }
        if( payRequest.getMerchantPayments()==null
                || payRequest.getMerchantPayments().isEmpty()
                || payRequest.getMerchantPayments().getFirst().getMerchantId()==0){
            return "Merchant payment details cannot be null or empty";
        }
        double sum=0;
        for(MerchantPayment merchantPayment : payRequest.getMerchantPayments()){
            sum+=merchantPayment.getAmount();
        }
        if(payRequest.getTotalAmount() != sum){
            return "Merchant payment details inconsistent";
        }
        return "Pay Now request okay!";
    }


}
