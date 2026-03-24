# CI/CD Pipeline Plan
24.03.2026

## Overview

GitHub Actions pipeline that:
- Runs lint + tests on every PR targeting `dev` (catches issues before merge)
- Automatically builds Docker images and deploys to the team's Ubuntu VPS on every push/merge to `dev`

**Stack:** GitHub Actions + GitHub Container Registry (ghcr.io) + SSH + Docker Compose on server

---

## Files to Create

| File | Purpose |
|---|---|
| `.github/workflows/ci.yml` | PR checks: frontend lint + backend tests (run in parallel) |
| `.github/workflows/deploy.yml` | Push-to-dev: build images → push to ghcr.io → SSH redeploy |
| `frontend/Dockerfile` | Multi-stage: Node 22 build → nginx 1.27 serve |
| `frontend/nginx.conf` | Static file serving + `/api` and `/health` proxy to backend container |
| `backend/Dockerfile` | Multi-stage: JDK 21 Maven build → JRE 21 run |
| `docker-compose.yml` | Root-level compose file used on the server |

## Files to Modify

- `backend/src/main/java/com/predictorama/backend/config/CorsConfig.java`
  Add `ALLOWED_ORIGIN` env var support alongside `localhost:5173` so production browser requests aren't blocked by CORS

---

## Step 1 — GitHub Secrets (manual, one-time)

In the GitHub repo: **Settings → Secrets and variables → Actions → New repository secret**

| Secret | Value |
|---|---|
| `SERVER_HOST` | VPS IP address or hostname |
| `SERVER_USER` | SSH username (e.g. `ubuntu`) |
| `SERVER_SSH_KEY` | Full private key PEM contents (including `-----BEGIN...` and `-----END...` lines) |
| `SERVER_PORT` | SSH port — omit if using default port 22 |

Add corresponding ssh public key `~/.ssh/authorized_keys` on the server.

---

## Step 2 — One-Time Server Setup

SSH into the VPS and run once:

```bash
sudo mkdir -p /opt/predictorama
sudo chown ubuntu:ubuntu /opt/predictorama

# Authenticate Docker to ghcr.io (needed while packages are private)
# Generate a GitHub PAT with read:packages scope at:
# GitHub > Settings > Developer settings > Personal access tokens
echo "YOUR_GITHUB_PAT" | docker login ghcr.io -u YOUR_GITHUB_USERNAME --password-stdin
```

Also stop the system nginx if it's running (it conflicts with the Docker frontend on port 80):
```bash
sudo systemctl stop nginx
sudo systemctl disable nginx
```

`docker-compose.yml` is automatically copied to the server by the deploy pipeline — no manual copy needed.

---

## Step 3 — `.github/workflows/ci.yml`

**Triggers on:** `pull_request` targeting `dev`

Two **parallel** jobs:

**`frontend-lint`**
1. `actions/checkout@v4`
2. `actions/setup-node@v4` — Node 22, npm cache keyed on `frontend/package-lock.json`
3. `npm ci`
4. `npm run lint`

**`backend-test`**
1. `actions/checkout@v4`
2. `actions/setup-java@v4` — temurin-21, Maven cache
3. `./mvnw test --no-transfer-progress`

> Note: backend tests pass without a database because `application.properties` already excludes JPA/DataSource auto-configuration. Do not remove those exclusions.

---

## Step 4 — `.github/workflows/deploy.yml`

**Triggers on:** `push` to `dev` (i.e. every merged PR)

### Job 1: `build-and-push`

Requires `permissions: packages: write` so `GITHUB_TOKEN` can push to ghcr.io.

1. Checkout
2. Login to ghcr.io with `docker/login-action@v3` using `GITHUB_TOKEN`
3. `docker/setup-buildx-action@v3`
4. Build + push frontend image (`docker/build-push-action@v6`)
   - Tags: `predictorama-frontend:latest` and `predictorama-frontend:<git-sha>`
   - Layer cache: `cache-from/cache-to: type=gha`
5. Build + push backend image (same pattern)

### Job 2: `deploy`

Runs after `build-and-push` succeeds (`needs: build-and-push`).

Uses `appleboy/ssh-action@v1` with the four server secrets:
```bash
cd /opt/predictorama
docker compose pull
docker compose up -d
docker image prune -f
```

`docker image prune -f` removes old dangling image layers to prevent disk accumulation over time.

---

## Step 5 — `frontend/Dockerfile`

```
Stage 1 (builder): node:22-alpine
  COPY package.json package-lock.json → npm ci → COPY . → npm run build

Stage 2 (runner): nginx:1.27-alpine
  Remove default html → COPY dist from builder → COPY nginx.conf → EXPOSE 80
```

---

## Step 6 — `frontend/nginx.conf`

```nginx
server {
    listen 80;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;   # Required for React Router
    }

    location /api {
        proxy_pass http://backend:8080;     # "backend" = Docker Compose service name
    }

    location /health {
        proxy_pass http://backend:8080;
    }
}
```

`try_files ... /index.html` is required — without it, refreshing on any route other than `/` returns a 404.
`http://backend:8080` resolves via Docker's internal DNS from the service name in `docker-compose.yml`.

---

## Step 7 — `backend/Dockerfile`

```
Stage 1 (builder): eclipse-temurin:21-jdk
  COPY mvnw + pom.xml → mvnw dependency:go-offline  ← cached layer, only invalidates on pom.xml change
  COPY src → mvnw clean package -DskipTests --no-transfer-progress

Stage 2 (runner): eclipse-temurin:21-jre   ← JRE is ~200MB smaller than JDK
  COPY target/backend-0.0.1-SNAPSHOT.jar app.jar
  EXPOSE 8080
  ENTRYPOINT ["java", "-jar", "app.jar"]
```

JAR name `backend-0.0.1-SNAPSHOT.jar` comes from `pom.xml` (`<artifactId>backend</artifactId>`, `<version>0.0.1-SNAPSHOT</version>`).

---

## Step 8 — `docker-compose.yml` (repo root)

```yaml
services:
  frontend:
    image: ghcr.io/OWNER/predictorama-frontend:latest
    ports:
      - "80:80"
    depends_on:
      - backend
    restart: unless-stopped

  backend:
    image: ghcr.io/OWNER/predictorama-backend:latest
    ports:
      - "8080:8080"
    environment:
      - ALLOWED_ORIGIN=http://YOUR_SERVER_IP_OR_DOMAIN
    restart: unless-stopped
```

Replace `OWNER` with the **lowercase** GitHub org/username (e.g. `shrampus`).
`restart: unless-stopped` brings containers back automatically after server reboots.

---

## Step 9 — CORS Update in `CorsConfig.java`

The current hardcoded `localhost:5173` will reject all browser requests in production. Update to read from env:

```java
.allowedOrigins(
    "http://localhost:5173",
    System.getenv().getOrDefault("ALLOWED_ORIGIN", "http://localhost:5173")
)
```

The `ALLOWED_ORIGIN` value is injected by the `environment:` block in `docker-compose.yml`.

---

## Verification Checklist

1. **CI checks**: Open a PR to `dev` → GitHub Checks tab shows "Frontend Lint" + "Backend Test" both green
2. **Image build**: Merge the PR → Actions tab shows "Deploy" workflow running; new images appear in repo Packages
3. **Server deploy**: SSH in → `docker compose -f /opt/predictorama/docker-compose.yml ps` → both containers show `Up`
4. **App works**: `curl http://SERVER_HOST/health` returns `{"status":"ok","service":"backend"}`; open `http://SERVER_HOST` in browser → React app loads
5. **Layer cache**: Second deploy after first one should be noticeably faster (seconds vs minutes)
