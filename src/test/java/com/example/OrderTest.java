package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class OrderTest {

    @Test
    void initializesAnOrderWithPendingStatusAndStoresItsFields() {
        Order order = new Order();
        order.item = "Keyboard";
        order.quantity = 2;
        order.customerId = "customer-123";

        assertEquals("Keyboard", order.item);
        assertEquals(2, order.quantity);
        assertEquals("customer-123", order.customerId);
        assertEquals(Order.PENDING, order.status);
        assertEquals("PROCESSED", Order.PROCESSED);
    }
}
