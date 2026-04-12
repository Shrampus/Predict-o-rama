# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Predict-o-rama is a full-stack football prediction web app. The repo is a monorepo with a React/TypeScript frontend and a Spring Boot backend.

## Commands


### Backend (`cd backend`)
```bash
./mvnw spring-boot:run  # Start dev server at http://localhost:8080
./mvnw test             # Run tests
./mvnw clean package    # Build JAR
```

## Architecture

### Backend
- **Spring Boot 4.0.3 / Java 21 / Maven**
- Package root: `com.predictorama.backend`
- Layout: `adapter/` (REST controllers, persistence, external APIs), `domain/` (entities, services, ports), `config/`
- All REST endpoints under `/api/` (plus `GET /health` at root)
- **CORS** is configured via `CorsConfig.java` — reads `ALLOWED_ORIGIN` env var (defaults to `http://localhost:5173`)
- **Auth (session cookie):** `POST /api/auth/login`, `GET /api/auth/me`, `POST /api/auth/logout` — see `AuthController`, `SessionService`
- **Predictions:** `GET /api/predictions?competition=…&groupId=…` (tournament matches + user’s predictions for that group), `POST /api/predictions` (upsert) — require authenticated session; see `PredictionController`, `TournamentPredictionQueryService`, `PredictionService`
- **External fixtures:** football-data.org — `football-data.base-url` and `football-data.api-key` (env `FOOTBALL_DATA_API_KEY`) in `application.properties`

### Database
- **PostgreSQL 17** connected via Spring profiles (`dev`, `prod`)
- **Liquibase** manages schema migrations at `backend/src/main/resources/db/changelog/`
- **Hibernate** `ddl-auto=validate` — Liquibase owns the schema, Hibernate only validates
- Unit tests mock the repository layer (no test DB needed)
- Environment variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `SPRING_PROFILES_ACTIVE`, `FOOTBALL_DATA_API_KEY` (fixtures)

### Dev workflow

**Option 1: Docker (recommended)**
```bash
cd local-env && docker compose up --build
```
Starts frontend, backend, and PostgreSQL. Frontend at http://localhost, backend at http://localhost:8080.

**Option 2: Manual**
```bash
# Terminal 1 — requires a local Postgres running on port 5432
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Terminal 2
cd frontend && npm run dev
```
Then open `http://localhost:5173` — API calls are proxied automatically.
