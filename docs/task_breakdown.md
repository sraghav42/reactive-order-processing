# Implementation Tasks: Event-Driven Order Pipeline

This document breaks down the Event-Driven Order Pipeline project into incremental, independently testable units of work.

---

## Phase 1: Project Scaffolding & Configuration

### Task 1.1: Initialize Quarkus Project & Maven Dependencies
- **Description:** Set up the Maven project configuration (`pom.xml`) targeting Java 25 and Quarkus platform.
- **Dependencies:**
  - `quarkus-resteasy-reactive-jackson`
  - `quarkus-hibernate-orm-panache`
  - `quarkus-jdbc-postgresql`
  - `quarkus-smallrye-reactive-messaging-kafka`
  - `quarkus-junit5` and `rest-assured` (for testing)
  - `awaitility` (for async test assertions)
- **Verification / Test:**
  - Run `./mvnw compile` to verify successful dependency resolution and build configuration.

### Task 1.2: Configure Dev Services & Reactive Messaging Properties
- **Description:** Create `src/main/resources/application.properties` configuring SmallRye Reactive Messaging for Kafka channels without manual database or Kafka connection strings (relying on Quarkus Dev Services).
- **Configuration Details:**
  ```properties
  # Reactive Messaging Kafka Channel Setup
  mp.messaging.outgoing.order-events.connector=smallrye-kafka
  mp.messaging.outgoing.order-events.topic=orders-in

  mp.messaging.incoming.orders-in.connector=smallrye-kafka
  mp.messaging.incoming.orders-in.topic=orders-in
  mp.messaging.incoming.orders-in.group.id=order-processor-group
  ```
- **Verification / Test:**
  - Run `./mvnw quarkus:dev` and verify Dev Services containers (PostgreSQL & Kafka Testcontainers) spin up automatically without connection errors.

---

## Phase 2: Domain Model & Persistence (Hibernate with Panache)

### Task 2.1: Implement `Order` Panache Entity
- **Description:** Implement the `Order` entity using Quarkus Active Record pattern (`extends PanacheEntity`).
- **Entity Requirements:**
  - Fields: `public String item`, `public int quantity`, `public String customerId`, `public String status`.
  - Include constant values or helper for status (`PENDING`, `PROCESSED`).
  - Follow Quarkus Panache idioms (public fields, no Spring Data repositories).
- **Verification / Test:**
  - Unit/integration test verifying entity instantiation and validation.

### Task 2.2: Test Active Record Persistence Operations
- **Description:** Write integration tests verifying database operations directly on the entity class.
- **Test Scenarios:**
  - `Order.persist()` successfully saves an entity with generated ID.
  - `Order.findById(id)` retrieves the correct entity.
  - Status updates and transactional commits function correctly with PostgreSQL Dev Services.
- **Verification / Test:**
  - Run `@QuarkusTest` targeting Panache persistence methods.

---

## Phase 3: REST API & Event Ingestion

### Task 3.1: Implement DTOs & Validation
- **Description:** Create request DTO (`CreateOrderRequest`) with fields: `item`, `quantity`, `customerId`.
- **Verification / Test:**
  - Verify JSON deserialization and validation.

### Task 3.2: Implement `OrderResource` (REST Ingestion & Retrieval)
- **Description:** Create JAX-RS resource `OrderResource` (`@Path("/api/orders")`, `@ApplicationScoped`).
- **Endpoints:**
  - `POST /api/orders`:
    - Persists order in DB with `PENDING` status using `order.persist()`.
    - Injects `@Channel("order-events") Emitter<Order>` and emits order to Kafka.
    - Returns HTTP `201 Created` with created `Order` body.
  - `GET /api/orders/{id}`:
    - Calls `Order.findById(id)`.
    - Returns HTTP `200 OK` with order or HTTP `404 Not Found` if not present.
- **Verification / Test:**
  - Integration tests with REST-assured for `POST /api/orders` and `GET /api/orders/{id}`.

---

## Phase 4: Asynchronous Processing (Reactive Messaging Worker)

### Task 4.1: Implement `OrderProcessor` Consumer
- **Description:** Create CDI bean `OrderProcessor` (`@ApplicationScoped`) to consume order events from Kafka.
- **Requirements:**
  - Annotate consumer method with `@Incoming("orders-in")`.
  - Annotate with `@Transactional` (or `@Blocking` if necessary for blocking DB transactions).
  - Look up order by ID using `Order.findById(order.id)`.
  - Update `status` to `PROCESSED`.
  - Save/commit changes to PostgreSQL.
- **Verification / Test:**
  - Unit/component test verifying incoming message handling and entity update.

### Task 4.2: End-to-End Reactive Pipeline Integration Test
- **Description:** Implement end-to-end `@QuarkusTest` testing the complete reactive flow.
- **Test Scenarios:**
  - Send `POST /api/orders` to create an order (assert `201 Created` and status `PENDING`).
  - Use Awaitility to poll `GET /api/orders/{id}` until status transitions to `PROCESSED`.
  - Validate database state and Kafka message consumption in Dev Services environment.
- **Verification / Test:**
  - Run `./mvnw test` to ensure full test suite passes.

---

## Phase 5: Build, Native Compilation & Documentation

### Task 5.1: Native Image Verification
- **Description:** Verify application compiles and passes tests as a native GraalVM executable.
- **Verification / Test:**
  - Run `@QuarkusIntegrationTest` against native executable: `./mvnw verify -Dnative` (or container-based native build).

### Task 5.2: Project Documentation (`README.md`)
- **Description:** Provide comprehensive `README.md` with setup and architecture details.
- **Content Requirements:**
  - Dev mode command: `./mvnw compile quarkus:dev`.
  - Native build command: `./mvnw package -Dnative`.
  - Architectural comparison section contrasting Quarkus build-time optimizations, startup time, and memory footprint with Spring Boot.
- **Verification / Test:**
  - Review documentation against PRD requirements.
