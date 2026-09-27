package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

class CreateOrderRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserializesRequestJson() throws Exception {
        CreateOrderRequest request = objectMapper.readValue(
                """
                {
                  "item": "Keyboard",
                  "quantity": 2,
                  "customerId": "customer-123"
                }
                """,
                CreateOrderRequest.class);

        assertEquals("Keyboard", request.item);
        assertEquals(2, request.quantity);
        assertEquals("customer-123", request.customerId);
    }

    @Test
    void validatesRequiredFieldsAndPositiveQuantity() throws Exception {
        CreateOrderRequest request = objectMapper.readValue(
                """
                {
                  "item": " ",
                  "quantity": 0,
                  "customerId": ""
                }
                """,
                CreateOrderRequest.class);

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();

            assertEquals(3, validator.validate(request).size());
            assertTrue(validator.validate(request).stream()
                    .anyMatch(violation -> violation.getPropertyPath().toString().equals("item")));
            assertTrue(validator.validate(request).stream()
                    .anyMatch(violation -> violation.getPropertyPath().toString().equals("quantity")));
            assertTrue(validator.validate(request).stream()
                    .anyMatch(violation -> violation.getPropertyPath().toString().equals("customerId")));
        }
    }
}
