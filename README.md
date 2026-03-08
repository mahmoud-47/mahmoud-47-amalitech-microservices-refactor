# Food Delivery Platform - Microservices

Spring Boot microservices architecture decomposed from a monolith. Four independently deployable services communicating via REST and RabbitMQ.

---

## Services

| Service | Port | Database |
|---|---|---|
| API Gateway | 8080 | — |
| Config Server | 8888 | — |
| Eureka Server | 8761 | — |
| Customer Service | 8081 | customer_db |
| Restaurant Service | 8082 | restaurant_db |
| Order Service | 8083 | order_db |
| Delivery Service | 8084 | delivery_db |
| RabbitMQ Management | 15672 | — |
| Redis | 6379 | — |

---

## Prerequisites

- Docker + Docker Compose
- Java 21
- Maven 3.9+

---

## Running the App

```bash
# Clone the repo
git clone https://github.com/mahmoud-47/amalitech-microservices-refactor.git
cd amalitech-microservices-refactor

# Build all modules (shared-library must be built first)
mvn clean install

# Start everything
docker compose up --build
```

System is ready when all services appear in Eureka at `http://localhost:8761`.

---

## Project Structure

```
food-delivery-platform/
├── shared-library/        # Shared events, DTOs, exceptions
├── config-server/         # Centralized config (port 8888)
├── eureka-server/         # Service registry (port 8761)
├── api-gateway/           # Gateway + JWT auth + rate limiting (port 8080)
├── customer-service/      # Customer management + auth (port 8081)
├── restaurant-service/    # Restaurant + menu management (port 8082)
├── order-service/         # Order processing + outbox (port 8083)
├── delivery-service/      # Delivery tracking (port 8084)
├── docker-compose.yml
└── pom.xml
```

---

## Authentication

All requests (except `/api/auth/**` and `/api/restaurants/search/**`) require a Bearer token.

```bash
# Register
POST http://localhost:8080/api/auth/register

# Login — returns JWT
POST http://localhost:8080/api/auth/login

# Use token on all subsequent requests
Authorization: Bearer <token>
```

JWT validation happens at the Gateway only. Downstream services receive `X-Authenticated-User-Id` and `X-Authenticated-User-Role` headers.

---

## Rate Limiting

Configured on `POST /api/orders` at the Gateway level using Spring Cloud Gateway's `RequestRateLimiter` filter backed by Redis.

| Endpoint | Limit |
|---|---|
| POST /api/orders | 10 requests/second per user |

Exceeds limit returns `429 Too Many Requests`.

---

## Key Flows

**Place an order:**
1. Gateway validates JWT + enforces rate limit
2. Order Service validates customer → Customer Service (Feign + circuit breaker)
3. Order Service validates menu items → Restaurant Service (Feign + circuit breaker)
4. Order saved + `OrderPlacedEvent` written to outbox atomically
5. Outbox scheduler publishes event to RabbitMQ
6. Delivery Service consumes event → auto-creates delivery assignment

**Circuit breakers** are configured on all Feign clients. If a downstream service is down, a fallback response is returned rather than timing out.

---

## Environment Variables

Set these before running (or in a `.env` file at root):

```env
JWT_SECRET=your-256-bit-secret
POSTGRES_PASSWORD=your-db-password
RABBITMQ_DEFAULT_USER=guest
RABBITMQ_DEFAULT_PASS=guest
REDIS_PASSWORD=your-redis-password
```

---

## API Testing

Import `postman/food-delivery.postman_collection.json` into Postman.

Full flow covered: register → login → browse restaurants → place order → track delivery.

---

## Architecture

See `docs/architecture-diagram.png` and `docs/migration-decision-log.md` for full architecture and decomposition decisions.