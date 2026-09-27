package com.example;

import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class OrderProcessor {

    @Incoming("orders-in")
    @Blocking
    @Transactional
    public void process(Order event) {
        if (event == null || event.id == null) {
            throw new IllegalArgumentException("Order event must include an order ID");
        }

        Order order = Order.findById(event.id);
        if (order == null) {
            throw new IllegalStateException("Order not found for event ID " + event.id);
        }

        order.status = Order.PROCESSED;
    }
}
