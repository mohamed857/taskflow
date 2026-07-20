# 🚀 TaskFlow — Production-Ready Task Management API

A stateless, JWT-secured Task Management System (Jira/Trello-style backend) built to demonstrate a modern, production-grade Spring Boot architecture.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blue)
![Redis](https://img.shields.io/badge/Redis-Caching-red)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

---

## 📋 Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Features](#features)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Reference](#api-reference)
- [Authentication Flow](#authentication-flow)
- [Error Handling](#error-handling)
- [Background Jobs](#background-jobs)
- [Testing](#testing)
- [Known Issues & Notes](#known-issues--notes)
- [Roadmap](#roadmap)

---

## Overview

TaskFlow is a layered, stateless REST API for managing users and tasks. It focuses on demonstrating correct, idiomatic use of the Spring ecosystem — security, caching, auditing, AOP, and scheduling — rather than being a toy CRUD app.

Every user authenticates via JWT; every task belongs to exactly one user; overdue tasks are detected and flagged automatically by a background scheduler.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (Records, Pattern Matching) |
| Framework | Spring Boot 4.1.0 |
| Security | Spring Security 7 + Stateless JWT (JJWT 0.12.x) |
| Persistence | Spring Data JPA + PostgreSQL |
| Caching | Spring Data Redis (`@Cacheable`) |
| API Docs | springdoc-openapi 2.8.5 (Swagger UI) |
| AOP | Spring AOP (`@Aspect`, custom `@LogExecutionTime`) |
| Scheduling | Spring `@Scheduled` |
| Build Tool | Maven |
| Testing | JUnit 5, Mockito, MockMvc, Testcontainers |

> **Note:** This project tracks Spring Boot 4.x. Because the ecosystem around Boot 4 is still maturing, third-party library versions (like springdoc) are pinned deliberately — see [Known Issues & Notes](#known-issues--notes) before upgrading any dependency.

---

## Architecture

```
Client
  │
  ▼
JwtAuthenticationFilter  ──► validates Bearer token, populates SecurityContext
  │
  ▼
Controller Layer   (REST endpoints, request validation)
  │
  ▼
Service Layer      (business logic, @LogExecutionTime via AOP)
  │
  ▼
Repository Layer   (Spring Data JPA)
  │
  ▼
PostgreSQL              Redis (cache-aside for UserDetails)
```

**Cross-cutting concerns:**
- **AOP** (`LoggingAspect`) wraps any method annotated `@LogExecutionTime` and logs its duration — zero impact on business logic readability.
- **Auditing** (`AuditorAwareConfig`) automatically stamps `createdBy` / `updatedBy` on entities using the authenticated principal's email, falling back to `"SYSTEM"` for unauthenticated contexts (e.g. the scheduler).
- **Scheduling** (`TaskScheduler`) runs independently of the request/response cycle, sweeping the database every 60 seconds.

---

## Features

### 1. Authentication & Security
- Fully **stateless** — no HTTP sessions, `SessionCreationPolicy.STATELESS`.
- Custom `JwtAuthenticationFilter` (`OncePerRequestFilter`) validates the `Authorization: Bearer <token>` header on every request.
- Custom `JwtAuthenticationEntryPoint` returns structured JSON on `401 Unauthorized` (instead of the default HTML error page), including a specific `auth_error` reason when available.
- Passwords hashed with `BCryptPasswordEncoder`.
- `UserPrincipal` implements `UserDetails` and wraps the `User` entity — the security context exposes `id`, `email`, and `username` directly, avoiding a DB round-trip in controllers.

### 2. Caching
- `CustomUserDetailsService.loadUserByUsername` is annotated `@Cacheable(value = "users", key = "#email")`, so repeated authentications (e.g. every request carrying a JWT) skip the database after the first lookup.
- Backed by Redis via `spring.cache.type=redis`.
- **Requires Redis to be running** — if Redis is unreachable, Spring wraps the resulting exception as `InternalAuthenticationServiceException`, which is now handled generically (see [Error Handling](#error-handling)).

### 3. Auditing
- `@EnableJpaAuditing` + `AuditorAware<String>` bean automatically populates `createdAt`, `updatedAt`, `createdBy`, `updatedBy` on every entity implementing the auditing annotations — no manual timestamp/user tracking in service code.

### 4. AOP — Execution Time Logging
- Custom `@LogExecutionTime` annotation + `@Around` advice in `LoggingAspect` measures and logs method execution time in milliseconds for any annotated service method.

### 5. Background Scheduling
- `TaskScheduler.checkForOverdueTask()` runs every 60 seconds (`fixedRate = 60000`), bulk-updating any `PENDING` task whose `dueDate` has passed to `OVERDUE` via a single `@Modifying` JPQL query — no N+1 entity loading.

### 6. Validation & Error Handling
- Jakarta Bean Validation (`@NotBlank`, `@Email`) on all request DTOs.
- Centralized `@RestControllerAdvice` (`GlobalExceptionHandler`) normalizes all error responses to consistent JSON shapes.

### 7. API Documentation
- Swagger UI via springdoc-openapi, with a `bearerAuth` HTTP security scheme configured so JWTs can be tested directly from the "Authorize" button.

---

## Project Structure

```
com.taskflow
├── TaskflowApplication.java
├── annotation/
│   └── LogExecutionTime.java
├── config/
│   ├── SecurityConfig.java
│   ├── JwtService.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtAuthenticationEntryPoint.java
│   ├── CustomUserDetailsService.java
│   ├── UserPrincipal.java
│   ├── AuditorAwareConfig.java
│   ├── AuditConfig.java
│   ├── LoggingAspect.java
│   └── OpenApiConfig.java
├── controller/
│   ├── AuthController.java
│   ├── UserController.java
│   └── TaskController.java
├── service/
│   ├── UserService.java
│   └── TaskService.java
├── repository/
│   ├── UserRepository.java
│   └── TaskRepository.java
├── entity/
│   ├── User.java
│   └── Task.java
├── dto/
│   ├── UserRequest.java / UserResponse.java
│   └── TaskRequest.java / TaskResponse.java
├── exception/
│   ├── UserAlreadyExistsException.java
│   └── GlobalExceptionHandler.java
└── scheduler/
    └── TaskScheduler.java
```

---

## Getting Started

### Prerequisites
- JDK 21+
- Maven
- Docker (for Redis)
- PostgreSQL running locally, with a database named `taskflow_db`

### 1. Clone the repo
```bash
git clone https://github.com/mohamed857/taskflow.git
cd taskflow
```

### 2. Start Redis
```bash
docker run -d --name taskflow-redis -p 6379:6379 redis:alpine
```

### 3. Configure environment variables

Rather than hardcoding secrets in `application.properties`, export these before running the app:

```bash
export DB_PASSWORD=your_postgres_password
export JWT_SECRET=$(openssl rand -base64 32)
```

See [Configuration](#configuration) for the corresponding `application.properties` setup.

### 4. Run
```bash
mvn spring-boot:run
```

### 5. Explore the API
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI spec (JSON): `http://localhost:8080/v3/api-docs`

---

## Configuration

`src/main/resources/application.properties`:

```properties
spring.application.name=TaskFlow

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/taskflow_db
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.redis.timeout=2000ms
spring.cache.type=redis

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000
```

> ⚠️ **Do not commit real secrets.** Add `application.properties` (or a `.env` file, if you switch to `spring-dotenv`) to `.gitignore` if it ever contains real credentials. Use environment variables or a secrets manager in any shared/production environment.

---

## API Reference

### Auth

| Method | Endpoint | Auth Required | Description |
|---|---|:---:|---|
| `POST` | `/api/auth/register` | ❌ | Create a new user account |
| `POST` | `/api/auth/login` | ❌ | Authenticate and receive a JWT |

**Register**
```json
POST /api/auth/register
{
  "username": "mohamed",
  "email": "mohamed@example.com",
  "password": "SecurePass123"
}
```

**Login**
```json
POST /api/auth/login
{
  "email": "mohamed@example.com",
  "password": "SecurePass123"
}
```
```json
// 200 OK
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

### Users

| Method | Endpoint | Auth Required | Description |
|---|---|:---:|---|
| `GET` | `/api/users/me` | ✅ | Get the currently authenticated user |

### Tasks

| Method | Endpoint | Auth Required | Description |
|---|---|:---:|---|
| `POST` | `/api/tasks` | ✅ | Create a task for the authenticated user |

**Create Task**
```json
POST /api/tasks
Authorization: Bearer <token>

{
  "title": "Finish the README",
  "description": "Write strong documentation",
  "dueDate": "2026-07-20T10:00:00"
}
```
```json
// 201 Created
{
  "id": 1,
  "title": "Finish the README",
  "description": "Write strong documentation",
  "dueTime": "2026-07-20T10:00:00",
  "status": "PENDING"
}
```

> Set `dueDate` in the past to watch the scheduler flip the status to `OVERDUE` within 60 seconds.

---

## Authentication Flow

1. Client calls `POST /api/auth/register` → user is persisted with a BCrypt-hashed password.
2. Client calls `POST /api/auth/login` → `AuthenticationManager` delegates to `DaoAuthenticationProvider`, which loads the user via `CustomUserDetailsService` (Redis-cached) and verifies the password.
3. On success, `JwtService` issues a signed JWT (`HS256`, configurable expiration).
4. Client stores the token and sends it as `Authorization: Bearer <token>` on every subsequent request.
5. `JwtAuthenticationFilter` intercepts the request, validates the signature/expiry, loads the user, and populates the `SecurityContext` — no session, no server-side state.
6. Downstream controllers access the authenticated user via `@AuthenticationPrincipal UserPrincipal`.

```
Register ──► Login ──► JWT issued ──► Bearer <token> on every request ──► Filter validates ──► SecurityContext set
```

---

## Error Handling

All errors are normalized to JSON by `GlobalExceptionHandler`:

| Exception | HTTP Status | Notes |
|---|:---:|---|
| `UserAlreadyExistsException` | 409 Conflict | Duplicate email on register |
| `DataIntegrityViolationException` | 409 Conflict | DB-level uniqueness violation |
| `MethodArgumentNotValidException` | 400 Bad Request | Field-level validation errors, keyed by field name |
| `AuthenticationException` (and subtypes, e.g. `BadCredentialsException`, `InternalAuthenticationServiceException`) | 401 Unauthorized | Covers bad credentials **and** infrastructure failures during authentication (e.g. Redis unreachable) |

Any exception **not** caught by `GlobalExceptionHandler` and thrown from within the security filter chain is instead formatted by `JwtAuthenticationEntryPoint`, which returns:

```json
{
  "timestamp": "2026-07-19T09:13:15.259Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Missing or invalid authentication token",
  "path": "/api/auth/login"
}
```

---

## Background Jobs

| Job | Trigger | Behavior |
|---|---|---|
| `TaskScheduler.checkForOverdueTask()` | Every 60s (`fixedRate`) | Bulk-updates all `PENDING` tasks with `dueDate < now()` to `OVERDUE` in a single transactional query; logs a warning with the count of affected rows |

---

## Testing

```bash
mvn test
```

- `@WebMvcTest` — isolated controller tests with `MockMvc`, no full context or DB.
- `Testcontainers` — spins up a real PostgreSQL container for repository-level tests, torn down automatically afterward.
- `@MockBean` — mocks `UserService` / `PasswordEncoder` in web-layer tests.

---

## Known Issues & Notes

- **Spring Boot 4.1.0 is a fast-moving target.** Because this project tracks Boot 4 directly, third-party library compatibility (springdoc, JJWT, Testcontainers, etc.) must be re-verified on every Boot upgrade. If you bump `spring-boot-starter-parent`, re-check springdoc's compatibility matrix before assuming Swagger will still work.
- **Redis is a hard dependency at login time**, not just a performance optimization — because `loadUserByUsername` is `@Cacheable`, a Redis outage currently surfaces as an authentication failure rather than a silent cache-miss fallback to the DB. If you want login to degrade gracefully when Redis is down, consider setting `spring.cache.redis.cache-null-values=false` and wrapping the cache lookup with a fallback, or switching to a resilient `CacheErrorHandler`.
- **JWT secret must be a fixed, externally-supplied value** (`jwt.secret`) — never generate it at startup, or every restart invalidates all previously issued tokens.

---

## Roadmap

- [ ] Role-based authorization (currently every user is `ROLE_USER`)
- [ ] Task update / delete endpoints
- [ ] Pagination on task listing
- [ ] Refresh tokens
- [ ] Rate limiting on `/api/auth/**`
- [ ] Dockerize the full stack (`docker-compose.yml` for app + Postgres + Redis)