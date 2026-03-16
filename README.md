# Predict-o-rama

Your all-in-one family gambling platform for predicting football match outcomes.

## Repository Structure

```
Predict-o-rama/
├── frontend/   # React + Vite frontend
└── backend/    # Spring Boot backend API
```

## Requirements

Frontend:

* Node.js (>=18 recommended)
* npm

Backend:

* Java 21
* Maven (wrapper included)

---

# Running the Frontend

```bash
cd frontend
npm install
npm run dev
```



# Running the Backend

```bash
cd backend
./mvnw spring-boot:run
```

The backend will start on:

```
http://localhost:8080
```

---

# Current API Endpoints (Skeleton)

Health check:

```
GET /health
```

Example:

```bash
curl http://localhost:8080/health
```

Response:

```json
{
  "status": "ok",
  "service": "backend"
}
```

Predictions placeholder:

```
GET /api/predictions
```

Example:

```bash
curl http://localhost:8080/api/predictions
```

Response:

```json
{
  "message": "Predictions endpoint placeholder",
  "data": []
}
```

---

# Development Notes

* The backend database (PostgreSQL) will be added later.
* JPA auto-configuration is currently disabled until a database is connected.
* The backend currently provides only skeleton endpoints to support early frontend development and CI/CD setup.
