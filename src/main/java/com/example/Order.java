package com.example;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order extends PanacheEntity {

    public static final String PENDING = "PENDING";
    public static final String PROCESSED = "PROCESSED";

    public String item;
    public int quantity;
    public String customerId;
    public String status = PENDING;
}
