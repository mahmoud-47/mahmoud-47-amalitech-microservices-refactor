# Food Delivery Platform — Monolith → Microservices Migration

## Architecture Overview

```
Client
  └── API Gateway :8080 (JWT auth, rate limiting, circuit breaker)
        ├── /api/auth/**, /api/customers/**  → customer-service  :8081
        ├── /api/restaurants/**              → restaurant-service :8082
        ├── /api/orders/**                   → order-service      :8083
        └── /api/deliveries/**               → delivery-service   :8084

Config Server  :8888  (serves config to all services)
Eureka         :8761  (service discovery)
RabbitMQ       :5672  (async events)
Redis          :6379  (rate limiter)
PostgreSQL     :5432  (4 separate databases)
```

---

## Key Migration Decisions

### 1. Cross-Domain Relationships → IDs + Feign/Events
**Before:** `order.getCustomer().getFirstName()` — direct JPA entity traversal across domains.  
**After:** `order.getCustomerId()` (Long) — fetch via Feign only when needed at write time; snapshot data at creation time for reads.

### 2. Synchronous Delivery Creation → Async Event
**Before:** `OrderService.placeOrder()` called `deliveryService.createDeliveryForOrder()` synchronously, blocking the response.  
**After:** Order Service publishes `OrderPlacedEvent`. Delivery Service consumes it asynchronously. Order response returns immediately.

### 3. Delivery Status → Order Status (reverse async)
**Before:** `delivery.getOrder().setStatus(DELIVERED)` — Delivery Service directly mutated Order entity.  
**After:** Delivery Service publishes `DeliveryStatusUpdatedEvent`. Order Service consumes it and updates its own status.

### 4. Snapshot Pattern (avoid read-time Feign calls)
- `OrderItem.itemName` — menu item name stored at order time. Reading order history needs no Restaurant Service call.
- `Delivery.restaurantName`, `pickupAddress`, `deliveryAddress` — stored from `OrderPlacedEvent`. Reading a delivery needs no Order/Restaurant call.

### 5. Internal vs Public Endpoints
Cross-service Feign calls hit `/internal/**` endpoints, clearly separated from the public `/api/**` surface. These can be blocked at the gateway.

### 6. Shared DTOs Strategy
Service-private DTOs (e.g. `OrderResponse`) stay in their service.  
Cross-service DTOs live in `common-utils`: `SharedCustomerResponse`, `SharedRestaurantResponse`, `SharedMenuItemResponse`, `SharedOrderResponse`.

### 7. JWT — Generate Once, Validate at Gateway
Customer Service **generates** JWT at login/register.  
API Gateway **validates** all JWTs and injects `X-Authenticated-User-Id` / `X-Authenticated-User-Role` headers.  
Downstream services trust these headers — no JWT library needed in business services.

### 8. Circuit Breakers — Two Layers
- **Gateway level:** `spring-cloud-circuitbreaker-reactor-resilience4j` — protects against total service failure before routing.
- **Feign level:** `feign.circuitbreaker.enabled=true` — per-call fallback when a downstream service is slow or down.

---

## Event Catalogue

| Event | Exchange | Routing Key | Published By | Consumed By | Trigger |
|---|---|---|---|---|---|
| `OrderPlacedEvent` | `order.exchange` | `order.placed` | order-service | delivery-service | Customer places an order |
| `OrderCancelledEvent` | `order.exchange` | `order.cancelled` | order-service | delivery-service | Customer cancels an order |
| `DeliveryStatusUpdatedEvent` | `delivery.exchange` | `delivery.status.updated` | delivery-service | order-service | Driver updates delivery status |

### Event Payloads

**`OrderPlacedEvent`**
```
orderId, customerId, restaurantId, restaurantName,
deliveryAddress, restaurantAddress (snapshot), totalAmount, placedAt
```

**`OrderCancelledEvent`**
```
orderId, customerId, reason, cancelledAt
```

**`DeliveryStatusUpdatedEvent`**
```
deliveryId, orderId, customerId,
newStatus, driverName, driverPhone, updatedAt
```

### Status Mapping (DeliveryStatusUpdatedEvent → Order)
| Delivery Status | Order Status |
|---|---|
| ASSIGNED | CONFIRMED |
| PICKED_UP / IN_TRANSIT | OUT_FOR_DELIVERY |
| DELIVERED | DELIVERED |
| FAILED | CANCELLED |

---

## Feign Client Map

| Caller | Target | Endpoint | Why Sync |
|---|---|---|---|
| order-service | customer-service | `GET /internal/customers/by-username/{u}` | Need delivery address before saving order |
| order-service | restaurant-service | `GET /internal/restaurants/{id}` | Validate restaurant is active |
| order-service | restaurant-service | `GET /internal/menu-items/{id}` | Validate + snapshot item name/price |
| restaurant-service | customer-service | `GET /internal/customers/by-username/{u}` | Validate owner on restaurant create |
| restaurant-service | customer-service | `GET /internal/customers/{id}` | Validate ownership on menu item edit |

---

## Dead Letter Queues

Every queue has a paired DLQ. On failure, RabbitMQ routes the message to the DLQ after retries are exhausted, preventing message loss.

| Queue | DLQ |
|---|---|
| `order.placed.queue` | `order.placed.queue.dlq` |
| `order.cancelled.queue` | `order.cancelled.queue.dlq` |
| `delivery.status.queue` | `delivery.status.queue.dlq` |

---

## Startup Order

```
1. PostgreSQL + RabbitMQ + Redis   (infrastructure, no dependencies)
2. config-service                  (must be up before any Spring service reads config)
3. discovery-service (Eureka)      (must be up before services register)
4. api-gateway                     (depends on Eureka)
5. customer-service                (registers with Eureka, reads config)
6. restaurant-service              (same)
7. order-service                   (same + needs customer + restaurant Feign healthy)
8. delivery-service                (same + consumes from order queues)
```

Docker Compose enforces this with `depends_on` + `healthcheck`.