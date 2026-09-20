# Property Finder API

A small Quarkus/PostgreSQL backend for the Angular Property Finder application.

## Requirements

- Angular 21+
- Java 21+
- Maven 3.9+
- Docker (optional, for PostgreSQL)

## Run PostgreSQL

```bash
docker compose -f backend/docker-compose.yml up -d
```

```or
simple install in system for local development and connect
```

## Run the API

```bash
cd backend
mvn quarkus:dev
```

The API is available at `http://localhost:8080`. Flyway creates the schema on startup.

## Authentication endpoints

- `POST /api/auth/signup` - create a `USER` or `BROKER` account
- `POST /api/auth/login/password` - login with email/mobile and password
- `POST /api/auth/login/otp/request` - request an OTP (logged to the server in development)
- `POST /api/auth/login/otp/verify` - verify the OTP and receive a token

Password login and OTP verification return a bearer token. Send it as `Authorization: Bearer <token>` when calling broker endpoints.

## Property endpoints

- `GET /api/properties?north=...&south=...&east=...&west=...` - properties inside a map viewport
- `GET /api/broker/dashboard` - authenticated broker listing summary

For production, set `APP_JWT_SECRET` to a long random value and configure a real SMS/email OTP provider instead of the development logger.
