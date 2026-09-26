# Product Requirements Document (PRD): Event-Driven Order Pipeline

## 1. Project Overview

**Name:** Event-Driven Order Pipeline (Quarkus Showcase)
**Purpose:** A portfolio project designed to demonstrate proficiency in Quarkus for a developer transitioning from Spring Boot. It highlights Quarkus-specific paradigms: Build-time optimization, Dev Services (zero-config Testcontainers), Hibernate with Panache (Active Record pattern), and Reactive Messaging.
**Target Audience for Codebase:** Technical interviewers, open-source contributors, and AI code generation agents.

## 2. Architecture & Tech Stack

* **Language:** Java 25

* **Framework:** Quarkus

* **Database:** PostgreSQL

* **Message Broker:** Apache Kafka

* **Key Quarkus Extensions:**

  * `quarkus-resteasy-reactive-jackson` (Non-blocking REST endpoints)

  * `quarkus-hibernate-orm-panache` (Active Record persistence)

  * `quarkus-jdbc-postgresql` (Database driver)

  * `quarkus-smallrye-reactive-messaging-kafka` (Event streaming)

## 3. Core Features & User Stories

1. **Order Ingestion:** As a client, I can submit an order via a REST API. The system saves it with a `PENDING` status.

2. **Event Emitting:** Upon saving the order, the system publishes an `OrderCreated` event to a Kafka topic.

3. **Asynchronous Processing:** A background worker listens to the Kafka topic, simulates order processing, and updates the database record status to `PROCESSED`.

4. **Order Tracking:** As a client, I can fetch the status of an order via its ID to see if it has transitioned from `PENDING` to `PROCESSED`.

## 4. Implementation Specifications (CRITICAL FOR AGENTS)

### 4.1. Avoid Spring Boot Idioms

* **Do NOT** use `@RestController`, `@Autowired`, or `@Service` (use JAX-RS `@Path`, CDI `@Inject`, `@ApplicationScoped`).

* **Do NOT** use the Repository Pattern (e.g., `OrderRepository`).

### 4.2. Database & Persistence (Hibernate with Panache)

* Create an `Order` entity class that `extends PanacheEntity`.

* Fields: `String item`, `int quantity`, `String customerId`, `String status`.

* All database interactions (persist, findById) MUST be done using the Active Record pattern directly on the `Order` class (e.g., `Order.persist()`, `Order.findById(id)`).

### 4.3. Messaging (SmallRye Reactive Messaging)

* **Topic In:** `orders-in`

* **Topic Out:** `orders-out` (optional, for future expansion)

* Use `@Channel` and `Emitter<Order>` in the REST resource to send messages to Kafka when an order is created.

* Create an `OrderProcessor` bean using `@Incoming("orders-in")` to consume the Kafka message.

* The processor must use `@Transactional` to update the order status to `PROCESSED` in the database.

### 4.4. Dev Services (Zero-Configuration)

* **Do NOT** create a `docker-compose.yml` file.

* Rely entirely on Quarkus Dev Services. The application properties (`application.properties`) should only contain Kafka topic mappings, but **no** database URLs, usernames, passwords, or Kafka bootstrap server URLs. Quarkus will inject these at dev time using Testcontainers.

* Required properties:

  ```
  mp.messaging.outgoing.order-events.connector=smallrye-kafka
  mp.messaging.outgoing.order-events.topic=orders-in
  
  mp.messaging.incoming.orders-in.connector=smallrye-kafka
  mp.messaging.incoming.orders-in.topic=orders-in
  mp.messaging.incoming.orders-in.group.id=order-processor-group
  
  ```

## 5. API Specification

### 5.1 Create Order

* **Endpoint:** `POST /api/orders`

* **Payload:**

  ```
  {
    "item": "Laptop",
    "quantity": 1,
    "customerId": "CUST-123"
  }
  
  ```

* **Response (201 Created):** Returns the created order with its generated ID and `PENDING` status.

### 5.2 Get Order

* **Endpoint:** `GET /api/orders/{id}`

* **Response (200 OK):** Returns the order object.

## 6. Build and Deployment Requirements

* The project must be buildable as a native GraalVM executable.

* Provide a `README.md` containing commands for:

  1. Running in Dev Mode: `./mvnw compile quarkus:dev`

  2. Building natively: `./mvnw package -Dnative`

  3. A brief section explaining the startup time and memory footprint differences compared to Spring Boot.