# E-Commerce Microservices — Pure Java

> A fully functional e-commerce backend built as three independent microservices — **no Spring, no framework**. Raw Java HTTP server, raw JDBC, three separate PostgreSQL databases.

[![Java](https://img.shields.io/badge/Java-17+-007396?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Architecture](https://img.shields.io/badge/Architecture-Microservices-blueviolet)]()
[![Status](https://img.shields.io/badge/Status-Active-brightgreen)]()

---

## Why This Exists

Most Java microservices tutorials hand you Spring Boot and tell you to annotate your way to a working system. This project does it without the scaffolding — using only `com.sun.net.httpserver`, raw JDBC, and Gson. Building this way forces you to understand what frameworks actually do under the hood.

---

## What It Does

A two-sided marketplace with three independently deployable services:

| Service | Port | Database | Responsibility |
|---------|------|----------|----------------|
| **User Management** | 8000 | `user_db` | Registration, login, auth, roles |
| **Payment Management** | 8001 | `paymentmanagement` | Accounts, fund transfers, transactions, pay-later loans |
| **Order Management** | 8002 | `ordermanagement` | Products, carts, orders, fulfilment |

Each service owns its database, runs its own migrations at startup, and is independently deployable. Services communicate over HTTP.

---

## User Roles

| Role | Capabilities |
|------|-------------|
| **Guest** | Browse and filter products — no auth required |
| **Customer** | Add to cart, checkout, pay, view own orders |
| **Merchant** | List products, manage stock, process and fulfil order items |
| **Admin** | Platform management (in progress) |

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17+ |
| HTTP Server | `com.sun.net.httpserver` — built-in, no framework |
| Database | PostgreSQL |
| DB Access | Raw JDBC — no ORM |
| JSON | Gson 2.13.1 |
| Password Hashing | jBCrypt 0.4 |
| Containers | Docker (databases) |

---

## Architecture

```
                    ┌─────────────────────────┐
                    │   Client (Postman/App)   │
                    └────────────┬────────────┘
                                 │
          ┌──────────────────────┼──────────────────────┐
          │                      │                      │
 ┌────────▼────────┐   ┌─────────▼─────────┐  ┌────────▼────────┐
 │  User Mgmt MS   │   │  Order Mgmt MS     │  │ Payment Mgmt MS │
 │   Port 8000     │◄──│   Port 8002        │─►│   Port 8001     │
 │                 │   │                    │  │                 │
 │ Register/Login  │   │ Products · Cart    │  │ Accounts        │
 │ Auth · Roles    │   │ Orders · Fulfilment│  │ Transfers       │
 └────────┬────────┘   └─────────┬──────────┘  │ Loans           │
          │                      │             └────────┬────────┘
    ┌─────▼──────┐        ┌──────▼───────┐      ┌──────▼───────┐
    │  user_db   │        │ ordermgmt DB │      │ payment DB   │
    └────────────┘        └──────────────┘      └──────────────┘
```

---

## Core Flows

### Cart → Checkout → Payment
```
Customer adds items to cart (price locked at add-time)
  ↓
POST /check-out
  ↓
Cart → CHECKEDOUT · Order created (PENDING)
  ↓
Per-merchant payment amounts calculated
  ↓
Payment request sent to Payment MS (/pay-now)
  ↓
  ├── Success
  │     OrderItems → CONFIRMED
  │     Product stock reduced atomically
  │     Transaction ID stored on order
  │
  └── Insufficient funds + payLater=true
        Loan created (principal × 1.10 interest, due in 2 weeks)
```

### Payment Distribution
```
Customer account debited (100%)
  ↓
Per merchant in order:
  97.0% → Merchant account
   2.5% → Platform admin account
   0.5% → Payment gateway retained
```

### Order Item Lifecycle (Merchant)
```
PENDING → CONFIRMED → PROCESSING → SHIPPED → COMPLETED
                  └──────────────────────────────────→ CANCELLED
```

---

## API Reference

### Order Management (Port 8002)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/insert-product` | Merchant | Create a product listing |
| `GET` | `/all-products` | None | List all in-stock products |
| `GET` | `/products` | None | Filter by category, merchant, name, price range |
| `POST` | `/increase-stock` | Merchant | Add stock units |
| `POST` | `/reduce-stock` | Merchant | Remove stock units |
| `POST` | `/add-to-cart` | Customer | Add item to cart |
| `POST` | `/check-out` | Customer | Checkout cart → creates order → triggers payment |
| `POST` | `/pay-for-this-item` | Customer | Direct single-product purchase (bypasses cart) |
| `GET` | `/user-get-orders` | Customer | Fetch authenticated user's orders |
| `POST` | `/process-order-item` | Merchant | Advance an order item's status |
| `GET` | `/get-order-items` | Merchant | View order items for merchant's products |

### Payment Management (Port 8001)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/pay-now` | Internal | Process payment, distribute funds |
| `GET` | `/create-account` | Customer/Merchant | Open a payment account |

**Auth header format:** `Authorization: {userId}/{token}`

---

## Running Locally

**Prerequisites:** Java 17+, PostgreSQL (or Docker)

**Step 1 — Create databases:**
```bash
psql -U postgres -c "CREATE DATABASE user_db;"
psql -U postgres -c "CREATE DATABASE ordermanagement;"
psql -U postgres -c "CREATE DATABASE paymentmanagement;"
```

**Step 2 — Configure each service** by editing the `.properties` files:
```properties
# orderManagementConfiguration.properties
dbUrl=jdbc:postgresql://localhost:5432/ordermanagement
dbUser=postgres
dbPassword=<your_password>
getUserByAuthorisationUrl=http://localhost:8000/all-user-details
paymentClientUrl=http://localhost:8001/pay-now
```

**Step 3 — Start services in order** (User MS must be first — auth depends on it):
```bash
# Terminal 1
java -cp ... userManagement.RunUserManagement      # port 8000

# Terminal 2
java -cp ... paymentManagement.RunPaymentManagement  # port 8001

# Terminal 3
java -cp ... orderManagement.RunOrderManagement     # port 8002
```

Each service runs its DB migrations automatically on first boot.

---

## What's Built

- [x] User registration, login, and role-based auth
- [x] Product listing, filtering, and merchant stock management
- [x] Shopping cart with multi-product support and price locking at add-time
- [x] Cart checkout → order creation pipeline
- [x] Direct single-item purchase (bypasses cart)
- [x] Payment processing with per-merchant fund distribution (97/2.5/0.5 split)
- [x] Pay-later via loan (10% interest, 2-week due date)
- [x] Order item status lifecycle — merchant-side fulfilment
- [x] Merchant order item retrieval with filters
- [x] User order history
- [x] Transaction ID stored on confirmed orders
- [x] Bank transfer strategy pattern — GTB, UBA, Default implementations
- [x] Schema migration runner (custom, no Liquibase)

---

## Roadmap

**Near-term:**
- [ ] Loan repayment endpoint (`POST /repay-loan`)
- [ ] Stock reversal on order item cancellation
- [ ] Idempotency keys on payment (prevent double-charging)
- [ ] Pagination on all list endpoints

**Architecture:**
- [ ] JWT auth (replace plaintext `userId/token` header)
- [ ] HikariCP connection pooling (replace per-request JDBC connections)
- [ ] DB transaction wrapping for checkout → pay → stock-reduce (full ACID guarantee)
- [ ] Saga/compensation logic for partial payment failure

**Infrastructure:**
- [ ] Dockerfile per service + `docker-compose.yml`
- [ ] GitHub Actions CI pipeline
- [ ] Environment variable config (12-factor)
- [ ] Unit + integration tests (JUnit 5, Mockito, Testcontainers)

---

## Author

**Samson Kayode** — Software Engineer  
[LinkedIn](https://linkedin.com/in/kayodesamson) · [GitHub](https://github.com/samzion) · kayodesamson4@gmail.com
