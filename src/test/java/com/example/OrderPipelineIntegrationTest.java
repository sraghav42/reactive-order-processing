package com.example;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.time.Duration;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

@QuarkusTest
class OrderPipelineIntegrationTest {

    @Test
    void createsOrderAndProcessesItAsynchronously() {
        var response = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "item": "Headphones",
                          "quantity": 1,
                          "customerId": "customer-e2e"
                        }
                        """)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("status", equalTo(Order.PENDING))
                .extract()
                .response();

        long orderId = ((Number) response.path("id")).longValue();

        Awaitility.await()
                .atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofMillis(250))
                .untilAsserted(() -> given()
                        .when()
                        .get("/api/orders/{id}", orderId)
                        .then()
                        .statusCode(200)
                        .body("status", equalTo(Order.PROCESSED)));

        Order processedOrder = QuarkusTransaction.requiringNew()
                .call(() -> Order.findById(orderId));

        assertNotNull(processedOrder);
        assertEquals(Order.PROCESSED, processedOrder.status);
    }
}
