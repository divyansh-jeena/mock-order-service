# Mock Order Service

A Spring Boot microservice designed to simulate downstream payment processing for the **Payment Event Recovery Engine**. It supports deterministic failure scenarios and database-backed idempotency to help test reliable HTTP delivery, retries, and recovery workflows.

## Overview

In distributed systems, a downstream service may successfully process a request while its response is lost. The calling service may then retry the request without knowing whether the original operation succeeded.

This service simulates such downstream behavior and demonstrates how persistent payment records can help prevent the same payment from being processed repeatedly.

## Key Features

- **REST API:** Accepts payment-processing requests over HTTP.
- **PostgreSQL persistence:** Stores successful payment records.
- **Idempotency:** Uses `paymentId` to identify previously successful payments and return success for repeated requests.
- **Database uniqueness:** Enforces uniqueness for `paymentId` and uses PostgreSQL `ON CONFLICT DO NOTHING` to avoid duplicate records.
- **Deterministic failure simulation:** Supports predefined scenarios such as HTTP 500, HTTP 503, timeout, and connection-failure simulation.
- **Retry testing:** Includes `PAY-007`, which simulates two failed requests followed by success.

## Tech Stack

- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- PostgreSQL
- Maven
- Lombok

## Architecture

```text
Payment Recovery Engine
          |
          | HTTP POST
          v
     OrderController
          |
          v
    MockOrderService
          |
          v
 OrderPaymentRepository
          |
          v
      PostgreSQL
```

The Payment Recovery Engine manages delivery attempts and retry scheduling. This service simulates the downstream system and maintains records of successful payments.

## API

### Process Payment

**Endpoint:** `POST /api/orders/payments`

**Local URL:** `http://localhost:8081/api/orders/payments`

**Request body:**

```json
{
  "paymentId": "PAY-SUCCESS",
  "orderId": "ORD-101",
  "amount": 500,
  "currency": "INR"
}
```

A successful request returns HTTP `200 OK` with a response such as:

```text
Payment accepted
```

Repeated requests for a payment already recorded as successful return success without inserting another payment record.

## Failure Scenarios

The service supports predefined payment IDs for testing:

| Payment ID | Simulated behavior |
|---|---|
| `PAY-SUCCESS` | Success |
| `PAY-500` | HTTP 500 |
| `PAY-503` | HTTP 503 |
| `PAY-TIMEOUT` | Simulated delayed response |
| `PAY-CONNECTION` | Simulated connection-failure response |
| `PAY-007` | Two HTTP 500 responses followed by success |

The timeout and connection-failure scenarios are simulations, not necessarily genuine network-level failures.

## Database Configuration

Create a PostgreSQL database named `mock_order_db`.

Configure the following properties in `src/main/resources/application.properties`:

```properties
spring.application.name=Mock Order Service
server.port=8081

spring.datasource.url=jdbc:postgresql://localhost:5432/mock_order_db
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Set `DB_USERNAME` and `DB_PASSWORD` as environment variables in your IDE or runtime environment.

**Security note:** Never commit database credentials or other secrets to GitHub.

## Running Locally

1. Install a compatible JDK, Maven, and PostgreSQL.
2. Create the `mock_order_db` database.
3. Configure the required environment variables.
4. Start the application from your IDE or run:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

The service starts on port `8081` by default.

## Relationship to the Payment Recovery Engine

This service is a separate downstream application used to test the Recovery Engine's delivery and retry behavior.

- The Recovery Engine manages payment events, retry scheduling, backoff, and delivery status.
- The Mock Order Service simulates downstream processing and stores successful payment records.
- The downstream idempotency mechanism helps prevent repeated processing when the same payment is delivered more than once.

The Recovery Engine's atomic claim mechanism and the downstream service's idempotency mechanism address different concerns: coordinating delivery attempts and preventing duplicate business operations, respectively.

## Current Limitations

- The service simulates downstream payment processing; it is not a real payment gateway.
- Failure scenarios are configured for local testing.
- The current idempotency implementation is based on persisted successful payment records. Full concurrency-safe business processing requires atomic coordination between claiming a payment and executing its business operation.
- Timeout and connection-failure modes do not necessarily reproduce genuine network-level failures.
- Database schema management currently uses Hibernate's `ddl-auto=update`; a migration tool can be introduced for more controlled schema evolution.

## Future Improvements

- Add automated unit and integration tests.
- Test lost-response scenarios and concurrent duplicate requests.
- Strengthen concurrency-safe idempotency.
- Add Docker and Docker Compose support.
- Introduce database migrations with Flyway.
- Improve structured logging and monitoring.

## License

Add a license file if you intend to distribute or open-source this project.
