# 🚀 TaskFlow — Advanced Jira-like Task Management System

A production-ready, highly scalable Task Management System built from scratch to demonstrate core and advanced Spring Boot patterns: **Role-Based Access Control**, **task delegation**, **JWT security**, **caching**, **auditing**, **AOP**, and **background scheduling**.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0--SNAPSHOT-brightgreen)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blue)
![Redis](https://img.shields.io/badge/Redis-Caching-red)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

---

## 📋 Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Core & Advanced Features](#core--advanced-features)
- [API Reference & RBAC Matrix](#api-reference--rbac-matrix)
- [Database Schema](#database-schema)
- [Getting Started](#getting-started)
- [API Documentation (Swagger UI)](#api-documentation-swagger-ui)
- [Running Tests](#running-tests)
- [System Design Notes](#system-design-notes)

---

## Overview

TaskFlow is a stateless, JWT-secured REST API for managing tasks across a team, modeled after Jira's permission and delegation patterns. Every task has a **reporter** (who created it) and an **assignee** (who's responsible for it), and every action is gated by role: `ADMIN`, `MANAGER`, or `USER`. A background scheduler sweeps the database every 60 seconds to flag overdue tasks automatically.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (Records, Pattern Matching) |
| Framework | Spring Boot 4.1.0-SNAPSHOT / Spring Framework 7 |
| Security | Spring Security 6 + Stateless JWT |
| Persistence | Spring Data JPA + PostgreSQL |
| Caching | Redis (Spring Data Redis) |
| API Docs | SpringDoc OpenAPI 3 (Swagger UI, Jackson 3 compatible) |
| Testing | JUnit 5, Mockito, MockMvc, Testcontainers |
| Build Tool | Maven |
| Containerization | Docker & Docker Compose (multi-stage build) |

> **Note:** This project tracks a Spring Boot 4.1.0 **snapshot**. Expect third-party library compatibility (SpringDoc, JJWT, Testcontainers) to need re-verification on every Boot upgrade.

---

## Core & Advanced Features

### 1. Enterprise Security & RBAC
A strict, Jira-like permission system enforced with `@PreAuthorize`:

| Role | Permissions |
|---|---|
| `ADMIN` | Full system access — create, read, update, delete any task |
| `MANAGER` | Create tasks, assign them, manage any task in the system |
| `USER` | View only tasks assigned to or created by them; can update **status only**, via a dedicated `PATCH` endpoint |

### 2. Task Delegation — Reporter vs. Assignee
- **Reporter**: the user who created the task (immutable relationship).
- **Assignee**: the user responsible for completing it (mutable — can be reassigned).
- Custom JPA queries (`findByIdAndReporterIdOrAssigneeId`) enforce secure data-access boundaries so users can only reach tasks they're actually party to.

### 3. Modern Java Constructs
- **Records** for all DTOs (`UserRequest`, `TaskResponse`, `TaskUpdateRequest`) — no boilerplate getters/setters.
- **Enums** for `Role` (`ADMIN`, `MANAGER`, `USER`) and `TaskStatus` (`PENDING`, `IN_PROGRESS`, `COMPLETED`, `OVERDUE`).

### 4. Spring Data JPA & Auditing
- **Entity auditing**: `createdAt`, `updatedAt`, `createdBy`, `updatedBy` populated automatically via a custom `AuditorAware` that reads the user from the JWT security context.
- **Custom queries**: `@Modifying` bulk updates for overdue tasks — no N+1 entity loading.
- **Performance**: `FetchType.LAZY` on all entity relationships.

### 5. Stateless JWT Architecture
- Tokens embed the user's **email** and **role** in their claims.
- `JwtAuthenticationFilter` extracts roles from the token and builds `GrantedAuthority`s dynamically (e.g. `ROLE_ADMIN`).
- `JwtAuthenticationEntryPoint` returns structured JSON on `401 Unauthorized` instead of the default HTML error page.

### 6. Performance & Caching (Redis)
- `RedisCacheManager` configured for the app.
- `@Cacheable` on `loadUserByUsername`, so repeated token validations skip PostgreSQL after the first lookup.

### 7. Aspect-Oriented Programming (AOP)
- Custom `@LogExecutionTime` annotation.
- `@Around` advice measures and logs method execution time in milliseconds — zero impact on business-logic readability.

### 8. Background Processing (Scheduling)
- `@EnableScheduling` + a `@Scheduled` job running every 60 seconds.
- Automatically finds tasks past their `dueDate` and flips their status to `OVERDUE` in bulk.

### 9. Global Exception Handling
- `@RestControllerAdvice` normalizes `ValidationException`, `DataIntegrityViolationException`, `BadCredentialsException`, and a custom `ResourceNotFoundException` into consistent JSON error responses.

### 10. Testing Strategy
- `@WebMvcTest` — isolated controller tests with `MockMvc`, no full application context.
- **Testcontainers** — spins up a real PostgreSQL instance in Docker for `UserRepositoryTests`, then tears it down automatically.

---

## API Reference & RBAC Matrix

### Auth

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register a new user (defaults to `USER` role) |
| `POST` | `/api/auth/login` | Public | Authenticate and receive a JWT + role |

### Users

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/users/me` | Authenticated | Get the current user's profile |
| `GET` | `/api/users` | Authenticated | List all users (for assigning tasks) |

### Tasks

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/tasks` | `ADMIN`, `MANAGER` | Create a new task and assign it |
| `GET` | `/api/tasks/all` | `ADMIN`, `MANAGER` | View every task in the system |
| `GET` | `/api/tasks` | Authenticated | Get tasks created by me |
| `GET` | `/api/tasks?assigned=true` | Authenticated | Get tasks assigned to me |
| `GET` | `/api/tasks/{id}` | Creator / Assignee | Get details for a specific task |
| `PUT` | `/api/tasks/{id}` | `ADMIN`, `MANAGER` | Fully or partially update a task |
| `PATCH` | `/api/tasks/{id}/status` | Creator / Assignee | Update task status only (for day-to-day use) |
| `DELETE` | `/api/tasks/{id}` | `ADMIN`, `MANAGER` | Permanently delete a task |

---

## Database Schema

**`users`**
`id`, `username`, `email`, `password`, `role` (enum), `created_at`, `updated_at`, `created_by`, `updated_by`

**`tasks`**
`id`, `title`, `description`, `due_date`, `status` (enum), `reporter_id` (FK), `assignee_id` (FK), `created_at`

---

## Getting Started

### Prerequisites
- JDK 21
- Docker running
- *(Optional)* PostgreSQL running locally, if you prefer running via an IDE instead of Docker

### 1. Clone the repo
```bash
git clone https://github.com/YOUR_USERNAME/taskflow.git
cd taskflow
```

### 2. Run via Docker (recommended)

Spins up the entire stack — app, PostgreSQL, and Redis — in one command:

```bash
docker-compose up --build
```

The app will be available at `http://localhost:8080`.

### 3. Run via IDE (IntelliJ / Eclipse)

If you'd rather run the backend locally for debugging:

1. Start Redis: `docker run -d --name taskflow-redis -p 6379:6379 redis:alpine`
2. Update `src/main/resources/application.properties` with your local PostgreSQL credentials.
3. Run `TaskflowApplication.java`.

---

## API Documentation (Swagger UI)

Once the app is running, the interactive API docs are available at:

```
http://localhost:8080/swagger-ui.html
```

1. Register a user via `/api/auth/register`.
2. Log in via `/api/auth/login` and copy the returned token.
3. Click **Authorize** at the top of the Swagger page.
4. Enter `Bearer <your_token>` and confirm.
5. Test any secured endpoint directly from the UI.

---

## Running Tests

```bash
mvn test
```

> `UserRepositoryTests` automatically spins up a PostgreSQL container via Testcontainers, runs against a real database, and tears the container down afterward.

---

## System Design Notes

**Why no `@OneToMany` on the `User` entity?**
To avoid N+1 queries and infinite JSON recursion, task relationships are unidirectional: tasks know their reporter and assignee, but `User` doesn't hold a list of tasks. Tasks are fetched via `TaskRepository` whenever needed.

**Why no `CascadeType.ALL` on tasks?**
In an enterprise system, deleting a user should never cascade-delete the tasks they reported. Data integrity here is enforced through database constraints and explicit logic — not JPA cascades.