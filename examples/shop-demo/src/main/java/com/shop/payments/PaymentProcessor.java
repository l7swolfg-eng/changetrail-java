package com.shop.payments;

public class PaymentProcessor {

    private final PaymentService paymentService = new PaymentService();

    public boolean processPayment(double amount) {
        return paymentService.charge(amount);
    }
}
