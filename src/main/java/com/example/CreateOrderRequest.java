package com.example;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class CreateOrderRequest {

    @NotBlank
    public String item;

    @Positive
    public int quantity;

    @NotBlank
    public String customerId;
}
