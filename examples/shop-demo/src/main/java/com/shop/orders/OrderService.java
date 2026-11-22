package com.shop.orders;

import com.shop.payments.PaymentProcessor;

public class OrderService {

    private final PaymentProcessor processor = new PaymentProcessor();

    public boolean placeOrder(double total) {
        return processor.processPayment(total);
    }
}
