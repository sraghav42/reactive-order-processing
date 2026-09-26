package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class OrderPersistenceTest {

    @Test
    void persistsAndRetrievesOrderWithGeneratedId() {
        Long orderId = QuarkusTransaction.requiringNew().call(() -> {
            Order order = new Order();
            order.item = "Keyboard";
            order.quantity = 2;
            order.customerId = "customer-123";
            order.persist();

            return order.id;
        });

        assertNotNull(orderId);

        Order persistedOrder = QuarkusTransaction.requiringNew()
                .call(() -> Order.findById(orderId));

        assertNotNull(persistedOrder);
        assertEquals("Keyboard", persistedOrder.item);
        assertEquals(2, persistedOrder.quantity);
        assertEquals("customer-123", persistedOrder.customerId);
        assertEquals(Order.PENDING, persistedOrder.status);
    }

    @Test
    void commitsStatusUpdatesToPostgres() {
        Long orderId = QuarkusTransaction.requiringNew().call(() -> {
            Order order = new Order();
            order.item = "Mouse";
            order.quantity = 1;
            order.customerId = "customer-456";
            order.persist();

            return order.id;
        });

        QuarkusTransaction.requiringNew().run(() -> {
            Order order = Order.findById(orderId);
            order.status = Order.PROCESSED;
        });

        Order updatedOrder = QuarkusTransaction.requiringNew()
                .call(() -> Order.findById(orderId));

        assertNotNull(updatedOrder);
        assertEquals(Order.PROCESSED, updatedOrder.status);
    }
}
