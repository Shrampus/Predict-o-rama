# Plan: Add PostgreSQL Database Infrastructure

## Study Notes: Questions, Answers & Reasoning

This section captures the design questions we worked through and why each decision was made.

---

### Q1: What areas need to be thought through when adding a database?

Nine categories of questions matter:

1. **Schema & Data Modeling** — What entities, relationships, data types?
2. **Schema Migration Strategy** — Flyway vs Liquibase vs Hibernate auto-DDL? Seed data?
3. **Connection & Credentials** — How do credentials reach containers? Dev vs prod separation?
4. **Docker Compose & Networking** — Port exposure, startup ordering, volume naming
5. **Production vs Local Dev Parity** — Same DB setup in both environments?
6. **Performance & Tuning** — Connection pooling (HikariCP), Postgres tuning
7. **Backup & Recovery** — `pg_dump`, backup volumes
8. **Security** — Network isolation, non-default users, `.env` in `.gitignore`
9. **CI/CD Impact** — Do tests need a DB? Does the Docker build still work?

---

### Q2: Should the Postgres port be exposed on the VPS?

**Three options:**

| Option | What it means | Security |
|---|---|---|
| **Internal only** | No port exposed. Backend connects via Docker network. You can only access DB via `docker exec -it db psql` | Most secure, but inconvenient for debugging |
| **Localhost only (`127.0.0.1:5432`)** | Accessible from the VPS host but NOT from the internet. You can SSH-tunnel from your laptop: `ssh -L 5432:localhost:5432 your-vps` and then use pgAdmin/DBeaver locally | Nearly as secure, much more practical |
| **Fully exposed (`0.0.0.0:5432`)** | Anyone on the internet can try to connect | Never do this on a public VPS |

**Decision: Localhost only.** Best balance of security and usability. The one-line difference:
```yaml
ports:
  - "127.0.0.1:5432:5432"  # localhost only
  - "5432:5432"             # fully exposed (don't!)
  # (no ports line at all)  # internal only
```

---

### Q3: Should local dev use a containerized Postgres?

**Yes.** One `docker compose up` gives you everything. No "works on my machine" issues. If another dev joins, they get the same setup. The alternative (installing Postgres natively on Windows) means your local setup diverges from production.

---

### Q4: How should production credentials be managed?

**Options considered:**

| Option | Pros | Cons |
|---|---|---|
| **Manual `.env` on VPS** | Simple, one-time setup | Must remember to recreate if VPS is rebuilt |
| **GitHub Secrets + CI/CD** | Fully automated, reproducible deploys. If VPS dies, re-run pipeline | Slightly more complex CI/CD |
| **Inline in compose** | Easy | Credentials in a public repo = disaster |
| **Host env vars** | No file to leak | Easy to forget after server rebuild |

**Decision: GitHub Secrets + CI/CD.** The deploy workflow SSHes into the VPS and writes a `.env` file from GitHub Secrets. The `.env` file is never in git. A `.env.example` template is committed so devs know what variables are needed.

---

### Q5: How does `depends_on` work, and why isn't it enough?

`depends_on: [db]` only waits for the Postgres **container** to start — not for Postgres to be **ready to accept connections**. Spring Boot will crash if it tries to connect too early.

**Solution:** Add a `healthcheck` to the Postgres service using `pg_isready` (a built-in Postgres utility), then use the extended `depends_on` syntax:
```yaml
depends_on:
  db:
    condition: service_healthy
```
This makes Docker wait until `pg_isready` returns success before starting the backend.

---

### Q6: What is `spring.jpa.hibernate.ddl-auto` and why use `validate`?

This property controls what Hibernate does with your database schema on startup:

| Value | What it does | When to use |
|---|---|---|
| `none` | Does nothing to the schema | When something else manages it |
| `validate` | Checks entity classes match existing tables. **App crashes if mismatch** | Production — catches mismatches early |
| `update` | Auto-alters tables to match entities. Never drops columns | Tempting but dangerous — can't rename, can't migrate data, unpredictable |
| `create` | Drops all tables, recreates from entities every startup | Throwaway prototyping only |
| `create-drop` | Same as create, also drops on shutdown | Tests only |

**Why `validate` + a migration tool (not `update` alone):**
- `update` never drops columns — rename a field and you get a ghost column forever
- `update` can't do data migrations (e.g., splitting `full_name` into `first_name` + `last_name`)
- `update` is unpredictable — you don't see what SQL runs against your DB
- A migration tool (Liquibase/Flyway) gives you version-controlled, reviewable changes. Hibernate just validates that your Java entities match what the migration tool created

**Workflow once entities exist:**
1. Write the entity class (e.g., `Prediction.java`)
2. Write a Liquibase changeset (`002-create-predictions.yaml`) with the `createTable` change
3. Start the app — Liquibase runs the changeset, Hibernate validates entity matches the new table
4. If there's a mismatch, `validate` catches it immediately at startup

---

### Q7: Flyway or Liquibase?

| Tool | Approach | Complexity |
|---|---|---|
| **Flyway** | Plain SQL migration files (`V1__create_users.sql`, `V2__add_predictions.sql`) | Simple, explicit |
| **Liquibase** | XML/YAML/JSON changelogs | More flexible but more complex |

| Aspect | Flyway | Liquibase |
|---|---|---|
| **Format** | Plain SQL files | YAML, XML, JSON, or SQL |
| **Learning curve** | Low — if you know SQL, you know Flyway | Medium — DSL syntax on top of SQL |
| **Rollback** | Manual (write your own undo SQL) | Auto-rollback for many operations |
| **DB-agnostic** | No — SQL is for one DB engine | Yes — YAML/XML translates to any DB dialect |
| **Diff/generate** | No | Can auto-generate changelogs by comparing DB to entities |
| **Contexts/labels** | No | Yes — tag changesets for specific environments |
| **Enterprise adoption** | Common in smaller/mid projects | More common in large/complex systems |

**Decision: Liquibase.** Our workplace uses Liquibase on larger systems, so practicing it here builds transferable skills. It offers auto-rollback, DB-agnostic changelogs, and richer features like contexts and preconditions. Spring Boot supports both equally well.

**Same migration in both formats (for reference):**

Flyway (`V1__create_users.sql`):
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) NOT NULL UNIQUE
);
```

Liquibase (`changelog/001-create-users.yaml`):
```yaml
databaseChangeLog:
  - changeSet:
      id: 001-create-users
      author: helts
      changes:
        - createTable:
            tableName: users
            columns:
              - column:
                  name: id
                  type: uuid
                  defaultValueComputed: uuid_generate_v4()
                  constraints:
                    primaryKey: true
              - column:
                  name: username
                  type: varchar(50)
                  constraints:
                    nullable: false
                    unique: true
```

---

### Q8: Do tests need a database?

**No.** The existing `ApplicationTests.java` is a `@SpringBootTest` that loads the full Spring context (which would need a DB). We'll **delete it**. All future tests will be unit tests that mock the repository layer — no DB connection needed.

The production Dockerfile already uses `-DskipTests`, so the Docker build won't break. CI tests will have nothing to run initially (0 tests = pass), which is fine until entity tests are added.

---

### Q9: What's a named Docker volume, and what survives restarts?

A named volume (e.g., `pgdata`) is explicitly declared in docker-compose and persists across container lifecycle:

| Command | Volume survives? |
|---|---|
| `docker compose stop` / `start` | Yes |
| `docker compose down` | Yes |
| `docker compose down -v` | **NO — volume is destroyed** |
| `docker compose up --force-recreate` | Yes |
| Server reboot | Yes |

**Key rule:** Never use `docker compose down -v` on the VPS unless you intend to wipe the database.

---

### Q10: Spring profiles — how do dev and prod differ?

| Setting | `dev` profile | `prod` profile |
|---|---|---|
| DB URL | Defaults to `localhost:5432` (overridable) | No default — must be set via env var |
| Credentials | Dev defaults (`predictorama/predictorama`) | No defaults — fails fast if missing |
| SQL logging | Enabled (debugging) | Disabled |
| HikariCP pool | Spring defaults | Explicit: max 10, min idle 2 |

Profiles are activated via `SPRING_PROFILES_ACTIVE=dev` or `prod` environment variable in docker-compose.

---

## Context

Predict-o-rama needs a database. The Spring Boot backend already has `spring-boot-starter-data-jpa` and `postgresql` driver in `pom.xml`, but JPA/DataSource auto-configuration is disabled. This plan wires up PostgreSQL in Docker, adds Liquibase for migrations, and configures Spring profiles for dev/prod. **No entities or repositories** — that's a separate task.

---

## Decisions

| Decision | Choice |
|---|---|
| DB port on VPS | `127.0.0.1:5432` (localhost only, SSH-tunnel for access) |
| Local dev DB | Containerized in `local-env/docker-compose.yml` |
| Prod credentials | GitHub Secrets → CI/CD writes `.env` on VPS during deploy |
| Startup ordering | Postgres healthcheck (`pg_isready`) + `depends_on: condition: service_healthy` |
| Migrations | Liquibase (YAML changelogs in `src/main/resources/db/changelog/`) |
| Tests | Unit tests with mocked repos — no test DB needed |
| Hibernate ddl-auto | `validate` (Liquibase owns the schema) |

---

## Steps

### 1. Add Liquibase dependency to `pom.xml`

**File:** `backend/pom.xml`

Add Liquibase core (version managed by Spring Boot BOM):
```xml
<dependency>
    <groupId>org.liquibase</groupId>
    <artifactId>liquibase-core</artifactId>
</dependency>
```

### 2. Rewrite `application.properties` (shared config)

**File:** `backend/src/main/resources/application.properties`

Remove the `spring.autoconfigure.exclude` line. Replace with:
```properties
spring.application.name=backend
server.port=8080
spring.jpa.open-in-view=false
spring.jpa.hibernate.ddl-auto=validate
spring.liquibase.enabled=true
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.yaml
```

### 3. Create `application-dev.properties`

**File:** `backend/src/main/resources/application-dev.properties`

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/predictorama}
spring.datasource.username=${DB_USERNAME:predictorama}
spring.datasource.password=${DB_PASSWORD:predictorama}
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Defaults allow running `mvn spring-boot:run` locally with a Postgres on localhost. Env vars override when running in Docker.

### 4. Create `application-prod.properties`

**File:** `backend/src/main/resources/application-prod.properties`

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
spring.jpa.show-sql=false
```

No defaults — fails fast if env vars are missing.

### 5. Delete `ApplicationTests.java`

**File:** `backend/src/test/java/com/predictorama/backend/ApplicationTests.java`

Delete this file. It's a `@SpringBootTest` context load test that would require a database once JPA is enabled. All future tests will be unit tests with mocked repositories — no DB needed.

### 6. Create Liquibase changelog files

**New directories:** `backend/src/main/resources/db/changelog/`

**File:** `backend/src/main/resources/db/changelog/db.changelog-master.yaml`

This is the master changelog that includes all individual changelogs:
```yaml
databaseChangeLog:
  - include:
      file: db/changelog/001-init.yaml
```

**File:** `backend/src/main/resources/db/changelog/001-init.yaml`

```yaml
databaseChangeLog:
  - changeSet:
      id: 001-init
      author: predictorama
      comment: Placeholder changeset to validate Liquibase is configured correctly
      changes:
        - sql:
            sql: SELECT 1
```

Future entity tables will be added as new changeset files (e.g., `002-create-users.yaml`) and included in the master changelog.

### 7. Update production `docker-compose.yml`

**File:** `docker-compose.yml`

Add `db` service, update `backend` service:

```yaml
services:
  db:
    image: postgres:17
    restart: unless-stopped
    environment:
      POSTGRES_DB: ${POSTGRES_DB}
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    ports:
      - "127.0.0.1:5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER} -d ${POSTGRES_DB}"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    # ... existing config plus:
    environment:
      - ALLOWED_ORIGIN=http://193.40.157.126
      - SPRING_PROFILES_ACTIVE=prod
      - DB_URL=jdbc:postgresql://db:5432/${POSTGRES_DB}
      - DB_USERNAME=${POSTGRES_USER}
      - DB_PASSWORD=${POSTGRES_PASSWORD}
    depends_on:
      db:
        condition: service_healthy

  frontend:
    # ... unchanged, depends_on: [backend]

volumes:
  pgdata:
```

### 8. Update `local-env/docker-compose.yml`

**File:** `local-env/docker-compose.yml`

Add `db` service with hardcoded dev credentials, update backend:

```yaml
services:
  db:
    image: postgres:17
    container_name: predictorama-db
    environment:
      POSTGRES_DB: predictorama
      POSTGRES_USER: predictorama
      POSTGRES_PASSWORD: predictorama
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U predictorama -d predictorama"]
      interval: 5s
      timeout: 5s
      retries: 5

  backend:
    # ... existing config plus:
    environment:
      - ALLOWED_ORIGIN=http://localhost
      - SPRING_PROFILES_ACTIVE=dev
      - DB_URL=jdbc:postgresql://db:5432/predictorama
      - DB_USERNAME=predictorama
      - DB_PASSWORD=predictorama
    depends_on:
      db:
        condition: service_healthy

volumes:
  pgdata:
```

### 9. Create `.env.example`

**File:** `.env.example`

```env
POSTGRES_DB=predictorama
POSTGRES_USER=predictorama
POSTGRES_PASSWORD=CHANGE_ME_TO_A_STRONG_PASSWORD
```

### 10. Update `.gitignore`

**File:** `.gitignore`

Add:
```
.env
*.env
!.env.example
```

### 11. Update CI/CD deploy workflow

**File:** `.github/workflows/deploy.yml` (or equivalent)

Add a step that writes `.env` from GitHub Secrets before `docker compose up`:
```bash
cat > .env << 'ENVEOF'
POSTGRES_DB=${{ secrets.POSTGRES_DB }}
POSTGRES_USER=${{ secrets.POSTGRES_USER }}
POSTGRES_PASSWORD=${{ secrets.POSTGRES_PASSWORD }}
ENVEOF
```

**Required new GitHub Secrets:** `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`

### 12. Update `CLAUDE.md`

Update the backend architecture section to reflect:
- PostgreSQL connected via Spring profiles (`dev`, `prod`)
- Liquibase manages migrations at `backend/src/main/resources/db/changelog/`
- Unit tests mock the repository layer (no DB needed)
- Document `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `SPRING_PROFILES_ACTIVE` env vars

---

## Verification

1. **Local dev:** `cd local-env && docker compose up` — all 3 containers start, backend connects to Postgres, Liquibase runs 001-init changeset
2. **Production (simulated):** Create a `.env` file at project root, run `docker compose up` — same but with prod profile, DB port on 127.0.0.1 only
3. **Deploy pipeline:** Push to trigger GitHub Actions — verify `.env` is written on VPS and all 3 containers start healthy
4. **Volume persistence:** `docker compose down` then `docker compose up` — Liquibase should detect 001-init already applied (no re-run)
5. **Check Liquibase tracking:** Connect to the DB and verify `DATABASECHANGELOG` table exists with one entry

## Pitfalls to Watch

- **Spring Boot 4.x Hibernate dialect** — verify `spring.jpa.database-platform` property name hasn't changed
- **First deploy** — GitHub Secrets (`POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`) must be configured BEFORE the first deploy, or `.env` will have empty values
- **`docker compose down -v` destroys the named volume** — never use `-v` on the VPS unless you intend to wipe the DB
- **CI tests** — once unit tests exist, they must mock the repository layer (no `@SpringBootTest` without a DB config)
- **Liquibase tracking tables** — Liquibase auto-creates `DATABASECHANGELOG` and `DATABASECHANGELOGLOCK` tables. Don't delete or modify these manually
- **Changeset IDs are immutable** — once a changeset has been applied, never modify it. Create a new changeset for changes
