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
- Structure: `config/` (CORS), `controller/` (REST endpoints)
- All REST endpoints are under `/api/` namespace
- **CORS** is configured to allow only `http://localhost:5173` — update `CorsConfig.java` for production
- **PostgreSQL driver** is present but JPA/DataSource auto-configuration is currently **disabled** in `application.properties` (database not yet connected); re-enable those exclusions when wiring up the DB
- Current endpoints: `GET /health`, `GET /api/predictions` (placeholder)

### Dev workflow
Run both services simultaneously:
```bash
# Terminal 1
cd backend && ./mvnw spring-boot:run

# Terminal 2
cd frontend && npm run dev
```
Then open `http://localhost:5173` — API calls are proxied automatically.
