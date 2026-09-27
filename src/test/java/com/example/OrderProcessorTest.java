package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class OrderProcessorTest {

    @Inject
    OrderProcessor orderProcessor;

    @Test
    void processesOrderEventAndCommitsStatusUpdate() {
        Long orderId = QuarkusTransaction.requiringNew().call(() -> {
            Order order = new Order();
            order.item = "Keyboard";
            order.quantity = 2;
            order.customerId = "customer-processor";
            order.persist();
            return order.id;
        });

        Order event = new Order();
        event.id = orderId;
        orderProcessor.process(event);

        String status = QuarkusTransaction.requiringNew()
                .call(() -> ((Order) Order.findById(orderId)).status);
        assertEquals(Order.PROCESSED, status);
    }

    @Test
    void rejectsEventWithoutOrderId() {
        assertThrows(IllegalArgumentException.class, () -> orderProcessor.process(new Order()));
    }

    @Test
    void rejectsEventForUnknownOrder() {
        Order event = new Order();
        event.id = Long.MAX_VALUE;

        assertThrows(IllegalStateException.class, () -> orderProcessor.process(event));
    }
}
