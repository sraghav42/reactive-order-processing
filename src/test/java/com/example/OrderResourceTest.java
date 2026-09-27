package com.example;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
class OrderResourceTest {

    @Test
    void createsOrderPersistsItAndReturnsCreatedRepresentation() {
        var response = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "item": "Keyboard",
                          "quantity": 2,
                          "customerId": "customer-123"
                        }
                        """)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("item", equalTo("Keyboard"))
                .body("quantity", equalTo(2))
                .body("customerId", equalTo("customer-123"))
                .body("status", equalTo(Order.PENDING))
                .extract()
                .response();

        long orderId = ((Number) response.path("id")).longValue();
        response.then().header("Location", endsWith("/api/orders/" + orderId));

        given()
                .when()
                .get("/api/orders/{id}", orderId)
                .then()
                .statusCode(200)
                .body("item", equalTo("Keyboard"))
                .body("quantity", equalTo(2))
                .body("customerId", equalTo("customer-123"))
                .body("status", anyOf(equalTo(Order.PENDING), equalTo(Order.PROCESSED)));
    }

    @Test
    void rejectsInvalidCreateRequest() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "item": "",
                          "quantity": 0,
                          "customerId": ""
                        }
                        """)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(400);
    }

    @Test
    void returnsNotFoundForMissingOrder() {
        given()
                .when()
                .get("/api/orders/{id}", Long.MAX_VALUE)
                .then()
                .statusCode(404);
    }
}
