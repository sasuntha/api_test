# API Automation Framework

A production-ready REST API test automation framework built with **Java**, **Rest Assured**, **TestNG**, and **Maven**, targeting the [FakeStore API](https://fakestoreapi.com). Covers authentication, users, products, and orders with JSON schema validation, structured request/response logging, environment switching, and detailed HTML reporting.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Features](#features)
- [Test Coverage](#test-coverage)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Running Tests](#running-tests)
- [Reports](#reports)
- [Environment Switching](#environment-switching)
- [Framework Architecture](#framework-architecture)

---

## Tech Stack

| Tool | Version | Purpose |
|---|---|---|
| Java | 11 | Language |
| Rest Assured | 5.4.0 | HTTP client & assertions |
| TestNG | 7.9.0 | Test runner & suite management |
| Maven | 3.x | Build & dependency management |
| Jackson Databind | 2.17.1 | JSON serialization / POJO mapping |
| ExtentReports | 5.1.1 | HTML test reports |
| JSON Schema Validator | 5.4.0 | Response contract validation |
| Logback | 1.5.6 | Structured console + file logging |

---

## Project Structure

```
apitest/
├── pom.xml
├── .gitignore
├── README.md
└── src/
    └── test/
        ├── java/com/apiframework/
        │   ├── base/
        │   │   └── BaseTest.java               # RestAssured setup, lifecycle hooks, report wiring
        │   ├── config/
        │   │   └── ConfigManager.java          # Singleton env config (qa / dev switching)
        │   ├── endpoints/
        │   │   └── APIEndpoints.java           # All API path constants
        │   ├── models/
        │   │   ├── LoginRequest.java
        │   │   ├── UserRequest.java            # Nested Name / Address / Geolocation POJOs
        │   │   ├── ProductRequest.java
        │   │   └── OrderRequest.java           # Nested OrderItem POJO
        │   ├── tests/
        │   │   ├── LoginTest.java              # 6 test cases
        │   │   ├── UserTest.java               # 10 test cases
        │   │   ├── ProductTest.java            # 11 test cases
        │   │   └── OrderTest.java              # 10 test cases
        │   └── utils/
        │       ├── AuthManager.java            # JWT token cache + refresh
        │       ├── RequestResponseLogger.java  # Custom Rest Assured Filter
        │       ├── SchemaValidator.java        # JSON schema assertion wrapper
        │       └── ReportManager.java          # ExtentReports lifecycle manager
        └── resources/
            ├── testng.xml                      # Suite definition (4 test groups)
            ├── extent.properties               # ExtentReports output config
            ├── logback-test.xml                # Console + rolling file logging
            ├── config/
            │   └── config.properties           # Base URLs, credentials, timeouts per env
            └── schemas/
                ├── login_schema.json
                ├── user_schema.json
                ├── product_schema.json
                └── order_schema.json
```

---

## Features

### Request / Response Logging
Every HTTP call is intercepted by `RequestResponseLogger`, a custom Rest Assured `Filter` that prints a structured block to the console and log file showing method, URL, request headers/body, response status, response headers/body, and elapsed time. Authorization header values are automatically masked.

### JSON Schema Validation
Each `GET` response is validated against a draft-07 JSON schema stored in `src/test/resources/schemas/`. The `SchemaValidator` utility wraps Rest Assured's `JsonSchemaValidator` and throws a descriptive assertion failure if the response contract is broken.

### Authentication Handling
`AuthManager` is a thread-safe singleton that calls `POST /auth/login` once per test run, caches the JWT token, and exposes `getBearerToken()` for all subsequent requests. Calling `refreshToken()` forces a new login.

### Environment Switching
`ConfigManager` reads the active environment from the JVM property `-Denv=`. Supported values: `qa` (default), `dev`. All base URLs, credentials, and timeouts are resolved from `config.properties` without any code changes.

### HTML Reports
`ReportManager` initialises an **ExtentReports Spark** reporter with a dark theme. Each test node contains:
- Collapsible **Request** panel (method, URL, body)
- Collapsible **Response** panel (status code highlighted green/red, body)
- Pass / Fail / Skip status with full stack trace on failure
- System info sidebar (environment, base URL, timestamp, executor)

---

## Test Coverage

| Suite | Class | Tests | Scenarios |
|---|---|---|---|
| Authentication | `LoginTest` | 6 | Valid login, schema validation, wrong password, unknown user, response time, token caching |
| Users | `UserTest` | 10 | GET all, GET by ID, schema, limit, sort desc, POST create, PUT update, DELETE, GET 404, GET user carts |
| Products | `ProductTest` | 11 | GET all, GET by ID, schema, limit, sort desc, GET categories, GET by category, POST, PUT, DELETE, price assertion |
| Orders | `OrderTest` | 10 | GET all, GET by ID, schema, limit, date range filter, POST, PUT, DELETE, product field validation, GET 404 |
| **Total** | | **37** | |

---

## Prerequisites

- **Java 11+** — verify with `java -version`
- **Maven 3.6+** — verify with `mvn -version`
- Internet access to reach `https://fakestoreapi.com`

---

## Getting Started

```bash
# Clone the repository
git clone https://github.com/your-username/api-automation-framework.git
cd api-automation-framework

# Install dependencies (downloads all JARs to local Maven cache)
mvn dependency:resolve
```

---

## Running Tests

```bash
# Run the full suite (QA environment by default)
mvn test

# Run against the DEV environment
mvn test -Denv=dev

# Run a single test class
mvn test -Dtest=LoginTest
mvn test -Dtest=ProductTest

# Run multiple specific classes
mvn test -Dtest="LoginTest,UserTest"

# Run a single test method
mvn test -Dtest=ProductTest#test_GetAllProducts_Returns200

# Skip tests (compile only)
mvn test-compile
```

---

## Reports

Two report formats are generated after every `mvn test` run:

### ExtentReports (Primary — Dark Theme HTML)
```
test-output/ExtentReports/APITestReport.html
```
Open this file in any browser for the full interactive report with request/response panels, system info, and pass/fail breakdown.

### TestNG Surefire Reports
```
target/surefire-reports/index.html
target/surefire-reports/emailable-report.html        ← single-file, shareable
target/surefire-reports/FakeStore API Automation Suite/
    ├── Authentication Tests.html
    ├── User API Tests.html
    ├── Product API Tests.html
    └── Order API Tests.html
```

### Log File
```
test-output/logs/api-test.log
```
Rolling daily log with full request/response details for every test run.

---

## Environment Switching

Edit `src/test/resources/config/config.properties` to add or modify environments:

```properties
# Active environment (overridable via -Denv=)
env=qa

# QA
qa.base.url=https://fakestoreapi.com
qa.username=mor_2314
qa.password=83r5^_

# DEV
dev.base.url=https://fakestoreapi.com
dev.username=mor_2314
dev.password=83r5^_
```

Switch at runtime — no rebuild needed:
```bash
mvn test -Denv=dev
```

---

## Framework Architecture

```
TestNG Suite (testng.xml)
        │
        ▼
  BaseTest (@BeforeSuite / @BeforeMethod / @AfterMethod / @AfterSuite)
        │
        ├── ConfigManager ──────► config.properties (env-aware)
        │
        ├── AuthManager ────────► POST /auth/login → cached JWT
        │
        ├── RestAssured RequestSpec
        │       └── RequestResponseLogger (Filter) ──► logback → api-test.log
        │
        ├── Test Classes (LoginTest / UserTest / ProductTest / OrderTest)
        │       ├── unauthenticatedRequest() ─► public endpoints
        │       ├── authenticatedRequest()   ─► bearer token injected
        │       └── SchemaValidator          ─► JSON schema assertion
        │
        └── ReportManager ──────► ExtentReports ──► APITestReport.html
```
