# E-Commerce-Microservices

This repository contains a sample **Java Spring Boot** microservices architecture that demonstrates:

- **Six independent services** (API Gateway, User, Product, Order, Payment, Inventory, Notification) each with its own PostgreSQL database.
- **Asynchronous communication** using **Kafka** for events.
- **JWT authentication** and **rate‑limiting**.
- **Saga orchestration** for distributed transactions with compensation.
- **Resilience4j** circuit breakers, exponential back‑off retries, and dead‑letter queues.
- **Infrastructure as code** with **Terraform** targeting AWS (ECS, RDS, ALB) – the Terraform state is stored locally for easy testing.
- **Local development** using **Docker Compose** + **LocalStack** to emulate AWS services.
- **Testing** with **Mockito**, **Testcontainers**, and **k6** load testing.
- **Observability** via **Prometheus** & **Grafana**.

## Prerequisites

- **Java 17** (or later) and **Maven**
- **Docker** + **Docker Compose**
- **Terraform** (latest)
- **Git**

## Quick Start (Local)

```bash
# Clone the repo (once created) or just cd into the directory
cd microservices-demo

# Start supporting infrastructure (LocalStack, Kafka, PostgreSQL)
docker compose up -d

# Build all services
./mvnw clean package -DskipTests

# Run a service (example User service)
java -jar services/user/target/user-0.0.1-SNAPSHOT.jar
```

## Terraform (LocalStack) 

```bash
cd terraform
terraform init      # uses a local backend (terraform.tfstate file)
terraform apply      # creates mocked AWS resources in LocalStack
```

## Testing

```bash
# Unit & integration tests (uses Testcontainers)
./mvnw test

# Load test with k6 (example script provided)
k6 run k6/load-test.js
```

## Pushing to GitHub

1. Create an empty repository on GitHub.
2. Add the remote:
   ```bash
   git remote add origin https://github.com/<USERNAME>/<REPO>.git
   ```
3. Push the initial commit:
   ```bash
   git push -u origin main
   ```

Replace `<USERNAME>` with your GitHub username and `<REPO>` with the repository name.

---

*This project layout is intentionally minimal – you can expand each service with real business logic, models, and controllers as needed.*
