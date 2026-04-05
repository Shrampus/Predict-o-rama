# Predict-o-rama

Your all-in-one family gambling platform for predicting football match outcomes.

## Repository Structure

```
Predict-o-rama/
├── frontend/        # React + Vite frontend
├── backend/         # Spring Boot backend API
├── local-env/       # Docker Compose for local development
└── docs/plans/      # Implementation plans and study notes
```

## Requirements

Frontend:

* Node.js (>=18 recommended)
* npm

Backend:

* Java 21
* Maven (wrapper included)

Docker (local dev / production):

* [Rancher Desktop](https://rancherdesktop.io/) or Docker Desktop (use **Moby/dockerd** runtime)

---

# Running the Frontend

```bash
cd frontend
npm install
npm run dev
```

## Tailwind CSS (Frontend Styling)

Tailwind CSS is installed in the frontend. It lets you style UI directly in JSX using utility classes like `p-4`, `text-lg`, and `bg-blue-600`.

Where to look:
- `frontend/tailwind.config.js` for Tailwind setup
- `frontend/src/index.css` (and/or `frontend/src/styles/index.css`) for `@tailwind` directives

Tip: when building new UI, prefer Tailwind utility classes first, then add custom CSS only when needed.

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

# Running with Docker (local)

Local dev spins up all three services: frontend, backend, and PostgreSQL.

```bash
cd local-env
docker compose up --build
```

| Service    | URL                   |
|------------|-----------------------|
| Frontend   | http://localhost      |
| Backend    | http://localhost:8080 |
| PostgreSQL | localhost:5432        |

Dev credentials (hardcoded in `local-env/docker-compose.yml`):

| Variable          | Value          |
|-------------------|----------------|
| `POSTGRES_DB`     | predictorama   |
| `POSTGRES_USER`   | predictorama   |
| `POSTGRES_PASSWORD` | predictorama |

The database uses a named volume (`pgdata`) that persists across `docker compose down` / `up`. To wipe the DB and start fresh, use `docker compose down -v`.

To stop:

```bash
docker compose down
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

# Production (VPS)

The app is deployed on a VPS via GitHub Actions CI/CD.

| Service    | URL                            |
|------------|--------------------------------|
| Frontend   | http://193.40.157.126          |
| Backend    | http://193.40.157.126:8080     |
| PostgreSQL | 127.0.0.1:5432 (VPS-local only) |

Deployment happens automatically on push to `dev`. The pipeline:
1. Builds and pushes Docker images to GHCR
2. SSHes into the VPS, writes `.env` from GitHub Secrets, runs `docker compose up -d`

Production credentials are stored as GitHub Secrets: `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`. The DB port is bound to `127.0.0.1` only (not exposed to the internet). Access it via SSH tunnel:

```bash
ssh -L 5432:localhost:5432 your-vps-user@193.40.157.126
```

Then connect with pgAdmin/DBeaver to `localhost:5432`.

---

# Database

* **PostgreSQL 17** in a Docker container with a named volume (`pgdata`)
* **Liquibase** manages schema migrations (`backend/src/main/resources/db/changelog/`)
* **Hibernate** `ddl-auto=validate` — Liquibase owns the schema, Hibernate only validates
* Spring profiles: `dev` (local defaults) and `prod` (env vars, no defaults)

To add a new migration:
1. Create a new changeset file (e.g., `002-create-users.yaml`) in `backend/src/main/resources/db/changelog/`
2. Add an include entry in `db.changelog-master.yaml`

---

# Development Notes

* The backend currently provides only skeleton endpoints to support early frontend development and CI/CD setup.
* Unit tests should mock the repository layer (no `@SpringBootTest` with a live DB).
