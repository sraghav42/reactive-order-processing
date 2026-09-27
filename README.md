# Reactive Order Processing

A small event-driven order service built with Java 25 and Quarkus. It demonstrates order ingestion over REST, persistence with Hibernate ORM and Panache, and asynchronous order processing through Kafka.

## Architecture

The service exposes a REST API backed by PostgreSQL. When a client submits an order, the application persists it with `PENDING` status and publishes it to the Kafka topic `orders-in`. A background consumer reads the event and updates the stored order to `PROCESSED`. Clients can retrieve an order at any time to check its status.

```text
Client -- POST /api/orders --> Quarkus REST resource -- persist --> PostgreSQL
                                      |
                                      +-- publish --> Kafka: orders-in
                                                           |
                                                           v
Client <-- GET /api/orders/{id} -- PostgreSQL <-- update -- Order processor
```

The project uses:

- Java 25 and Quarkus 3.39.5
- Quarkus REST with Jackson for the HTTP API
- Hibernate ORM with Panache's Active Record pattern
- PostgreSQL for order persistence
- SmallRye Reactive Messaging with Kafka
- Quarkus Dev Services to start PostgreSQL and Kafka for development and tests

## Prerequisites

- JDK 25
- Docker Desktop (or another running Docker-compatible container runtime) for Dev Services and container-based native builds

The Maven Wrapper downloads and uses the project's Maven version; a separate Maven installation is not required. Dev Services starts the PostgreSQL and Kafka containers automatically when the application runs in dev or test mode. No local database or Kafka configuration is needed for those modes.

## Run in development mode

From the repository root:

```shell
./mvnw compile quarkus:dev
```

On Windows PowerShell:

```powershell
.\mvnw.cmd compile quarkus:dev
```

The application listens on <http://localhost:8080>. Quarkus Dev UI is available at <http://localhost:8080/q/dev/>. Stop the application with `Ctrl+C`.

## HTTP API

Create an order:

```shell
curl -i -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"item":"Laptop","quantity":1,"customerId":"CUST-123"}'
```

The API returns `201 Created` with the generated order ID and its initial `PENDING` status. `item` and `customerId` must be non-blank, and `quantity` must be positive.

Retrieve an order by its ID:

```shell
curl http://localhost:8080/api/orders/1
```

The response includes the current status (`PENDING` or `PROCESSED`); processing is asynchronous, so the status may not change immediately. An unknown ID returns `404 Not Found`.

## Run tests

```shell
./mvnw test
```

On Windows PowerShell:

```powershell
.\mvnw.cmd test
```

Quarkus Dev Services supplies PostgreSQL and Kafka for the tests as well, so Docker must be running.

## Package and run on the JVM

Build the standard fast-jar package:

```shell
./mvnw package
```

Run it with:

```shell
java -jar target/quarkus-app/quarkus-run.jar
```

## Build a native executable

With GraalVM Native Image installed and available through `GRAALVM_HOME`, `JAVA_HOME`, or `PATH`, build a native executable with:

```shell
./mvnw package -Dnative
```

To also run the integration tests against the native executable:

```shell
./mvnw verify -Dnative
```

If `native-image` is not installed, Quarkus can build the native executable in a container. Start Docker first:

```shell
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

To build and run native integration tests in the same command:

```shell
./mvnw verify -Dnative -Dquarkus.native.container-build=true
```

On Windows PowerShell, quote the Maven properties so they are passed intact:

```powershell
.\mvnw.cmd verify '-Dnative' '-Dquarkus.native.container-build=true'
```

Container-based native builds require Docker and produce a Linux executable; run that executable on Linux rather than directly on Windows. Native compilation takes more time and memory than the regular JVM package build.

## Quarkus and Spring Boot: architectural differences

Both frameworks support dependency injection, REST APIs, persistence, and messaging. The differences below describe framework tendencies, not guaranteed measurements for every application.

| Area | Quarkus | Spring Boot |
|---|---|---|
| Build-time work | Performs augmentation at build time: it analyzes application code and extensions, processes configuration, and prepares much of the application for runtime. This reduces work that must happen during startup. | Traditionally performs more framework setup at runtime, including auto-configuration and bean processing, though AOT processing and native-image support can move some work to build time. |
| Startup time | Often starts quickly, especially as a native executable, because of build-time augmentation and the option to compile to native code. | JVM applications may do more initialization at startup. Spring AOT and native-image builds can reduce startup time substantially. |
| Memory footprint | Native executables often have a smaller runtime memory footprint than equivalent JVM deployments. A Quarkus JVM deployment still uses JVM memory, and native-image build-time memory usage can be substantial. | JVM memory use depends on the application and JVM configuration. Spring Boot also supports native executables, which can reduce runtime memory compared with JVM mode. |

Actual startup time and memory consumption depend on the application, enabled extensions, deployment configuration, and workload. A meaningful comparison should benchmark equivalent applications in the same environment rather than rely on framework-wide numbers.

## Further reading

- [Quarkus guides](https://quarkus.io/guides/)
- [Quarkus Maven tooling and native builds](https://quarkus.io/guides/maven-tooling)
- [Quarkus Kafka guide](https://quarkus.io/guides/kafka)
- [Hibernate ORM with Panache](https://quarkus.io/guides/hibernate-orm-panache)
- [Quarkus Dev Services](https://quarkus.io/guides/dev-services)
