# Basic-E-commerce-Order-Management-Microservices-Architecture
This project is a basic e-commerce order management system built with Java, PostgreSQL, and Docker, following a microservices architecture.
The system is split into three core microservices, each with its own database:

🔹 Microservices

#User Management Service

  Handles user registration, login, and authentication.

  Supports roles: guest, user, merchant, admin.

  Guests can browse products without login, but signup/login is required to buy, checkout, or sell.

#Order Management Service

  Manages shopping carts, order creation, and order tracking.

  Users must be authenticated to place orders.

  Merchants/admins can process or manage orders.

#Payment Management Service

  Records and processes payments.

  Ensures secure transaction flow between buyers and sellers.
  

🔹 Features

Public access (guests) can browse/view products without signup.

Registered users can buy products, add to cart, and track orders.

Merchants can list and manage products for sale.

Admins can perform higher-level management tasks.

Each service has its own PostgreSQL database (isolated schema per service).

All databases are containerized with Docker.

DBeaver is used for database administration and querying.


🔹 Tech Stack

Java (core microservices logic)

PostgreSQL (database for each microservice)

Docker (containerized databases)

DBeaver (database GUI client)

Postman (API testing)


🔹 Project Goals

This project demonstrates how to:

Build and connect microservices without a monolithic structure.

Separate concerns into different services with their own databases.

Manage users, orders, and payments in a distributed system.

Provide role-based access control (guest, user, merchant, admin).

⚡ This repository will evolve over time with API endpoints, database schemas, and integration examples.
