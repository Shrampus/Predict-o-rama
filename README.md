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

### Frontend environment variables

Copy `frontend/.env.example` to `frontend/.env.local` to override defaults locally.

| Variable | Default | Purpose |
|----------|---------|---------|
| `VITE_API_BASE_URL` | empty (relative paths) | Prefix for backend API calls. Leave empty when the frontend is served from the same origin as the backend API path, or when a reverse proxy (Vite dev proxy, nginx, etc.) forwards `/api` (including `/api/v1`) to the backend. Set to a full origin (e.g. `https://api.example.com`) when the UI is deployed on a different origin than the backend. |

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

### Swagger / OpenAPI

After the backend starts, open:

* Swagger UI: `http://localhost:8080/swagger-ui/index.html`
* OpenAPI JSON: `http://localhost:8080/v3/api-docs`

For protected endpoints, use the Swagger UI `Authorize` button with a bearer JWT returned by `POST /api/v1/auth/login` or `POST /api/v1/auth/google`.

Swagger is **enabled by default**. To disable it (e.g. in production), set the environment variable:

```
SPRINGDOC_ENABLED=false
```

This disables both the Swagger UI and the `/v3/api-docs` endpoint, and removes their security `permitAll` rules so the routes are no longer publicly accessible.

### Manual fixture sync from terminal

Manual fixture sync is terminal-only and is not exposed through the frontend or public REST API.

Run it from the server like this:

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.arguments="--manual-sync.competition=PL --manual-sync.date-from=2026-04-01 --manual-sync.date-to=2026-04-24"
```

Required arguments:

* `manual-sync.competition` - competition code such as `PL`, `CL`, or `WC`
* `manual-sync.date-from` - start date in `YYYY-MM-DD` format
* `manual-sync.date-to` - end date in `YYYY-MM-DD` format

By default the app exits after the manual sync finishes.

---

# Running with Docker (local)

Local dev spins up all three services: frontend, backend, and PostgreSQL.

Add .env file for local dev, check the example file.
- JWT_SECRET: each dev can generate their own secret for local dev: openssl rand -base64 64

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

The backend loads **`FOOTBALL_DATA_API_KEY`** for the [football-data.org](https://www.football-data.org/) client (upcoming fixtures for supported competitions). For Docker, copy `local-env/.env.example` to `local-env/.env` and set a real key.

The database uses a named volume (`pgdata`) that persists across `docker compose down` / `up`. To wipe the DB and start fresh, use `docker compose down -v`.

On the **`dev`** Liquibase context, migrations may seed test users and a test group (see `007-seed-test-users.yaml`, `008-seed-test-groups.yaml`) so local prediction flows have stable UUIDs.

To stop:

```bash
docker compose down
```

---

# API (selected endpoints)

Versioning note: the backend API routes are versioned under `/api/v1/...`.

## Health

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

## Auth (JWT bearer token)

Login returns a JWT; protected endpoints require the token in the `Authorization` header.

Authorization header:

```http
Authorization: Bearer <token>
```

| Method | Path | Notes |
|--------|------|--------|
| `POST` | `/api/v1/auth/login` | Body: `{ "email": "…", "password": "…" }` — returns auth token |
| `POST` | `/api/v1/auth/google` | Body: `{ "idToken": "…" }` — Google sign-in |
| `GET` | `/api/v1/auth/me` | Current user, or `401` if not logged in |
| `POST` | `/api/v1/auth/complete-profile` | Body: `{ "username": "…" }` — completes onboarding and returns refreshed auth token |

Example login request. Dev profile seeds users such as `bob@test.com` with password `password123` (see `007-seed-test-users.yaml` / `docs/plans/13-temp-simple-login.md`).

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"bob@test.com\",\"password\":\"password123\"}"
```

Copy the returned `token` and send it as a bearer token on protected requests.

## Predictions

Both calls require authentication. Send the JWT as a bearer token.

Example:

```bash
curl -X GET "http://localhost:8080/api/v1/predictions?competition=PL&groupId=aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa" \
  -H "Authorization: Bearer YOUR_JWT_HERE"
```

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/predictions?competition={code}&groupId={uuid}` | Upcoming matches for a competition plus the current user’s saved prediction per match (if any). |
| `POST` | `/api/v1/predictions` | Create or update a prediction for `(user, group, match)`. |

**`competition`** must be a football-data competition code the app allows (e.g. `CL`, `PL`, `WC`; see `CompetitionCatalog` in the backend).

**`POST` body** (JSON):

```json
{
  "groupId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
  "matchId": "…",
  "homeScore": 2,
  "awayScore": 1,
  "predictedWinner": "HOME"
}
```

`predictedWinner` is one of: `HOME`, `AWAY`, `DRAW`.

**`GET` response** shape:

```json
{
  "tournamentName": "UEFA Champions League",
  "matches": [
    {
      "matchId": "…",
      "externalMatchId": "…",
      "homeTeamName": "…",
      "awayTeamName": "…",
      "homeTeamImage": "…",
      "awayTeamImage": "…",
      "kickoffTime": "2026-04-10T18:00:00Z",
      "matchStatus": "SCHEDULED",
      "predictionId": null,
      "predictedHomeScore": null,
      "predictedAwayScore": null,
      "predictedWinner": null
    }
  ]
}
```

When the user has already predicted a match, `predictionId` and the `predicted*` fields are set.

## Matches

Both calls require authentication. Send the JWT as a bearer token.

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/matches/upcoming` | Upcoming matches with tournament and group availability metadata. |
| `GET` | `/api/v1/matches/upcoming/my` | Upcoming matches scoped to groups where the current user participates. |

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

* Predictions integrate with PostgreSQL (saved predictions and cached matches/teams) and optionally the football-data API when no matches are cached for the requested window.
* Unit tests should mock the repository layer (no `@SpringBootTest` with a live DB).
