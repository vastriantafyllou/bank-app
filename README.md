# BankApp

A Spring Boot application for **bank account management** (deposits, withdrawals, transfers) with **login/register** via Spring Security.

## Tech Stack

- Java 21
- Spring Boot 4
- Spring MVC + Thymeleaf
- Spring Security (form login)
- Spring Data JPA
- MySQL
- Lombok

## Features

- Create bank accounts
- View account details & transaction history
- Deposit / Withdraw
- Transfer (only allowed between accounts of the same user)
- Authentication:
  - `GET /login`, `GET/POST /register`
  - Access to `/accounts/**` requires authentication
- Admin seed:
  - Admin user is created/ensured on startup
  - Any orphan accounts (without owner) are assigned to admin

## Requirements

- Java 21
- MySQL

## Configuration

The project loads environment variables from `.env` (see `spring.config.import: optional:file:.env[.properties]`).

### Database

Add the following to `.env` (in the project root):

```properties
DB_URL=jdbc:mysql://localhost:3306/bankapp?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=your_dbo
DB_PASSWORD=your_password
```

### Admin credentials

The admin user is seeded on startup from properties:

- `ADMIN_USERNAME` (default: `admin`)
- `ADMIN_PASSWORD` (default: `admin`)

Optionally in `.env`:

```properties
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin
```

## Run locally

Using Gradle Wrapper:

```bash
./gradlew bootRun
```

App URL:

- `http://localhost:8080/`

## Useful routes

- `/` (home)
- `/login`
- `/register`
- `/accounts` (requires login)

## Notes

- The app uses `spring.jpa.hibernate.ddl-auto=update`, so the schema is updated automatically.
- When committing/pushing, make sure **not** to upload real credentials in `.env`.
