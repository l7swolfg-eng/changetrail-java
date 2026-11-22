package com.shop.payments;

public class PaymentService {

    public boolean charge(double amount) {
        return amount > 0;
    }
}
