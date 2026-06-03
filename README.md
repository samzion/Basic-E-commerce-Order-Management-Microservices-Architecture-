# Basic E-Commerce Order Management — Microservices Architecture

A backend e-commerce system built in pure Java following a microservices architecture. Each service owns its own PostgreSQL database and communicates with other services over HTTP. No Spring or Quarkus — uses Java's built-in `com.sun.net.httpserver`, raw JDBC, and Gson.

---

## Microservices Overview

| Service | Port | Database | Responsibility |
|---------|------|----------|----------------|
| User Management | 8000 | `user_db` | Registration, login, authentication, roles |
| Payment Management | 8001 | `paymentmanagement` | Accounts, fund transfers, transactions, loans |
| Order Management | 8002 | `ordermanagement` | Products, carts, orders, fulfillment |

Each service starts its own HTTP server, runs its own DB migrations at startup, and is independently deployable.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17+ |
| HTTP Server | `com.sun.net.httpserver` (built-in) |
| Database | PostgreSQL |
| DB Access | Raw JDBC (no ORM) |
| JSON | Gson 2.13.1 |
| Password Hashing | jBCrypt 0.4 |
| DB Client (dev) | DBeaver |
| API Testing | Postman |
| Containers | Docker (databases) |

---

## Architecture Diagram

```
                         ┌──────────────────────────────┐
                         │       Client (Postman/App)    │
                         └──────────────┬───────────────┘
                                        │
             ┌──────────────────────────┼─────────────────────────┐
             │                          │                          │
    ┌────────▼────────┐      ┌──────────▼──────────┐   ┌──────────▼──────────┐
    │  User Mgmt MS   │      │  Order Mgmt MS       │   │  Payment Mgmt MS    │
    │   Port 8000     │◄─────│   Port 8002          │──►│   Port 8001         │
    │                 │      │                      │   │                     │
    │ - Register      │      │ - Products           │   │ - Accounts          │
    │ - Login         │      │ - Cart               │   │ - Fund Transfers    │
    │ - Auth          │      │ - Orders             │   │ - Transactions      │
    │ - Roles         │      │ - Fulfillment        │   │ - Loans (pay later) │
    └────────┬────────┘      └──────────┬───────────┘   └──────────┬──────────┘
             │                          │                           │
    ┌────────▼────────┐      ┌──────────▼───────────┐  ┌───────────▼──────────┐
    │  user_db        │      │  ordermanagement DB   │  │  paymentmanagement   │
    │  (PostgreSQL)   │      │  (PostgreSQL)         │  │  DB (PostgreSQL)     │
    └─────────────────┘      └───────────────────────┘  └──────────────────────┘
```

---

## User Roles

| Role | Capabilities |
|------|-------------|
| **Guest** | Browse and filter products (no auth required) |
| **Customer** | Add to cart, checkout, pay, view own orders |
| **Merchant** | List products, manage stock, process/fulfill order items |
| **Admin** | Higher-level platform management (in progress) |

---

## API Endpoints

### Order Management Service (Port 8002)

#### Products
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/insert-product` | Merchant | Create a new product listing |
| GET | `/all-products` | None | List all in-stock products |
| GET | `/products` | None | Filter products by category, merchant, name, price range |
| POST | `/increase-stock` | Merchant | Add stock units to a product |
| POST | `/reduce-stock` | Merchant | Remove stock units from a product |

#### Cart & Orders
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/add-to-cart` | Customer | Add a product to the shopping cart |
| POST | `/check-out` | Customer | Checkout cart → creates order → triggers payment |
| POST | `/pay-for-this-item` | Customer | Direct single-product purchase (bypasses cart) |
| GET | `/user-get-orders` | Customer | Retrieve authenticated user's orders |

#### Merchant Fulfillment
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/process-order-item` | Merchant | Advance an order item's status |
| GET | `/get-order-items` | Merchant | View order items for the merchant's products |

### Payment Management Service (Port 8001)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/pay-now` | Internal | Process payment; distribute funds to merchants |
| GET | `/create-account` | Customer/Merchant | Create a bank account in the payment system |

### Authentication Header Format
All protected endpoints require:
```
Authorization: {userId}/{token}
```
The User Management service validates the token and returns the full user/merchant profile.

---

## Core Flows

### 1. Shopping Cart → Checkout → Payment
```
User adds items to cart (price locked at add-time)
       ↓
User calls /check-out
       ↓
Cart marked CHECKEDOUT → Order created (PENDING)
       ↓
CartItems copied to OrderItems with current product prices
       ↓
Per-merchant payment amounts calculated
       ↓
PaymentRequest sent to Payment MS (/pay-now)
       ↓
  ┌─── Payment success ──────────────────────────────┐
  │  - All OrderItems → CONFIRMED                    │
  │  - Order.transactionId set                       │
  │  - Product stock reduced atomically              │
  └──────────────────────────────────────────────────┘
  ┌─── Insufficient funds + payLater=true ───────────┐
  │  - Loan created (amount × 1.10 interest)         │
  │  - Due in 2 weeks                                │
  └──────────────────────────────────────────────────┘
```

### 2. Payment Distribution (within Payment MS)
```
Customer account deducted (100%)
       ↓
Per merchant in order:
  97.0% → Merchant's account
   2.5% → Ecommerce admin account
   0.5% → Retained by payment gateway
```

### 3. Order Item Lifecycle (Merchant Side)
```
PENDING → CONFIRMED → PROCESSING → SHIPPED → COMPLETED
                   └─────────────────────────────────→ CANCELLED
```

---

## Database Schema

### Order Management DB

```
products        (id, merchant_id, name, category, price, stock, ...)
carts           (id, user_id, status[OPEN|CHECKEDOUT], ...)
cart_items      (id, cart_id, product_id, quantity, price_at_add, status, ...)
orders          (id, user_id, status[PENDING|CONFIRMED|CANCELLED], transaction_id, ...)
order_items     (id, order_id, product_id, quantity, price, total[GENERATED], status, ...)
```

### Payment Management DB

```
accounts        (id, user_id, merchant_id, account_number, bank, balance, ...)
transactions    (id, account_number, amount, transaction_type[CREDIT|DEBIT], balance_on_source, ...)
loans           (id, account_id, amount_borrowed, amount_paid, due_date, ...)
```

---

## Project Structure

```
src/
├── Main.java
├── orderManagement/
│   ├── RunOrderManagement.java          # Entry point, port 8002
│   ├── db/
│   │   ├── DataBaseConnection.java
│   │   └── migrations/
│   │       ├── IMigration.java
│   │       ├── MigrationRunner.java
│   │       ├── ProductMigration.java
│   │       ├── CartMigration.java
│   │       ├── CartItemMigration.java
│   │       ├── OrderMigration.java
│   │       ├── OrderItemMigration.java
│   │       ├── ShipmentMigration.java
│   │       └── AddTransactionToOrderMigration.java
│   ├── httpHandlers/
│   │   ├── BaseHandler.java
│   │   ├── InsertProductHandler.java
│   │   ├── ListAvailProductsHandler.java
│   │   ├── GetProductsByFilterHandler.java
│   │   ├── IncreaseStockHandler.java
│   │   ├── ReduceStockHandler.java
│   │   ├── AddItemToCartHandler.java
│   │   ├── CheckOutHandler.java
│   │   ├── PayForItemtHandler.java
│   │   ├── ProcessOrderItemHandler.java  # NEW
│   │   ├── MerchantGetOrderItemHandler.java # NEW
│   │   └── UserGetOrdersHandler.java     # NEW
│   ├── models/
│   │   ├── entties/
│   │   │   ├── Product.java
│   │   │   ├── Cart.java / CartItem.java
│   │   │   ├── Order.java
│   │   │   ├── OrderItem.java
│   │   │   └── MerchantPayment.java
│   │   ├── enums/
│   │   │   └── OrderItemStatus.java      # NEW
│   │   ├── requests/
│   │   │   ├── InsertProductRequest.java
│   │   │   ├── AddItemToCartRequest.java
│   │   │   ├── CheckOutRequest.java
│   │   │   ├── PayForItemRequest.java
│   │   │   ├── PaymentRequest.java
│   │   │   ├── ProcessOrderItemRequest.java # NEW
│   │   │   ├── IncreaseStockRequest.java
│   │   │   └── ReduceStockRequest.java
│   │   └── responses/
│   │       ├── PaymentResponse.java
│   │       ├── UserMerchantDetails.java
│   │       ├── UserMerchantPlusMessage.java
│   │       └── CartItemResponse.java
│   └── services/
│       ├── ProductService.java
│       ├── CartService.java
│       ├── CartItemService.java
│       ├── OrderService.java
│       ├── OrderItemService.java
│       ├── UserServiceClient.java
│       └── PaymentServiceClient.java
├── paymentManagement/
│   ├── RunPaymentManagement.java         # Entry point, port 8001
│   ├── db/ ...
│   ├── httpHandlers/
│   │   ├── PayHandler.java
│   │   └── AccountCreationHandler.java
│   ├── models/
│   │   ├── bank/ (ITransfer, DefaultTransfer, GTBTransfer, UBATransfer)
│   │   ├── entities/ (Account, Transaction, Loan)
│   │   ├── requests/ (PayRequest, ...)
│   │   └── response/ (PaymentResponse, AccountOperationResponse)
│   └── services/
│       ├── AccountService.java
│       ├── TransactionService.java
│       ├── LoanService.java
│       └── UserServiceClient.java
└── userManagement/
    └── RunUserManagement.java            # Entry point, port 8000
```

---

## Configuration

Each service reads a `.properties` file at startup:

**`orderManagementConfiguration.properties`**
```properties
dbUrl=jdbc:postgresql://localhost:5432/ordermanagement
dbUser=postgres
dbPassword=<password>
dbDriver=org.postgresql.Driver
getUserByAuthorisationUrl=http://localhost:8000/all-user-details
paymentClientUrl=http://localhost:8001/pay-now
```

**`paymentManagementConfiguration.properties`**
```properties
dbUrl=jdbc:postgresql://localhost:5433/paymentmanagement
dbUser=admin
dbPassword=<password>
dbDriver=org.postgresql.Driver
getUserByAuthorisationUrl=http://localhost:8000/all-user-details
ecommerceAdminAccount=<account_number>
paymentServiceAdminAccount=<account_number>
```

---

## Running the App

### Prerequisites
- Java 17+
- PostgreSQL (or Docker)
- All three databases created: `user_db`, `ordermanagement`, `paymentmanagement`

### Start Order
1. Start User Management Service (port 8000) — must be up first for auth to work
2. Start Payment Management Service (port 8001)
3. Start Order Management Service (port 8002)

Each service auto-runs DB migrations on first boot.

---

## What's Been Built (Current State)

- [x] User registration, login, and role-based auth (User MS)
- [x] Product listing, filtering, and merchant stock management
- [x] Shopping cart with multi-product support and price locking
- [x] Cart checkout → order creation pipeline
- [x] Direct single-item purchase (bypasses cart)
- [x] Payment processing with per-merchant fund distribution
- [x] Pay-later via loan (10% interest, 2-week due date)
- [x] Order item status lifecycle management (merchant-side)
- [x] Merchant order item retrieval with filters
- [x] User order history with filters
- [x] Transaction ID stored on confirmed orders
- [x] Bank transfer strategy pattern (GTB, UBA, Default)
- [x] Schema migration runner

---

## Planned Features (Roadmap)

### Near-Term
- [ ] Loan repayment endpoint (`POST /repay-loan`)
- [ ] Stock reversal when an order item is CANCELLED
- [ ] Cart item quantity update endpoint
- [ ] Abandoned cart expiry (TTL / scheduled cleanup)
- [ ] Idempotency keys on payment to prevent double-charging
- [ ] Standardized JSON error response format

### Architecture Improvements
- [ ] JWT-based authentication (replace plaintext userId/token header)
- [ ] HikariCP connection pooling (replace per-request JDBC connections)
- [ ] DB transaction wrapping for checkout → pay → stock-reduce (ACID guarantee)
- [ ] Saga/compensation logic for partial payment failure across merchants
- [ ] Pagination on all list endpoints (`?page=0&size=20`)
- [ ] Structured logging with SLF4J + Logback

### Infrastructure
- [ ] Maven or Gradle build descriptor
- [ ] Dockerfile for each service
- [ ] `docker-compose.yml` for local full-stack startup
- [ ] GitHub Actions CI pipeline (build → test → image push)
- [ ] Move credentials to environment variables (12-factor config)

### API & Documentation
- [ ] API versioning (`/v1/...`)
- [ ] OpenAPI / Swagger specification
- [ ] `GET /health` liveness endpoint on each service
- [ ] Unit tests (JUnit 5 + Mockito)
- [ ] Integration tests (Testcontainers)

---

## Known Issues

| # | Location | Description |
|---|----------|-------------|
| 1 | `MerchantGetOrderItemHandler` | Returns `products` list instead of `orderItems` when filtering by productId |
| 2 | `ProductService.sufficientStockProduct()` | SQL references column `quantity`; correct column name is `stock` |
| 3 | `TransactionService.listTransactions()` | Queries `account_id` but `transactions` table stores `account_number` |
| 4 | `OrderItemStatus` enum | `COMPETED` should be `COMPLETED` (typo) |
| 5 | `models/entties/` | Directory name typo (should be `entities`) |

---

## Contributing

This project is under active development. Raise issues or PRs against the `main` branch. Feature branches follow the pattern `Add<FeatureName>`.
