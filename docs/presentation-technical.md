# Predict-o-rama — Tehniline ülevaade

**Põhifunktsioonid:**
- Kasutaja autentimine — e-mail/parool ja Google OAuth
- Gruppide loomine ja kutselinkide kaudu liitumine
- Matšide ennustamine (skoor + võitja) enne mängu algust
- Automaatne tulemuste skoorimine matši lõppedes, konfigureeritava reeglistiku alusel
- Edetabel grupi ja turniiri lõikes
- Automaatne matšide, meeskondade ja turniiride importimine football-data.org API-st

---

## 2. Tehnoloogia stack

| Kiht | Tehnoloogiad                                                                     |
|------|----------------------------------------------------------------------------------|
| **Frontend** | React 19, TypeScript, Vite, Tailwind CSS 4, React Router 7, i18next, Vitest      |
| **Backend** | Spring Boot 4, Java 21, Spring Web MVC, Spring Data JPA, Spring Security, Lombok |
| **Autentimine** | Google OAuth (google-api-client), JWT (jjwt)                         |
| **Andmebaas** | PostgreSQL 17, Liquibase (migratsioonid), Hibernate                              |
| **Välised teenused** | football-data.org (matšiandmed), Google Identity (sisselogimine)                 |
| **API dokumentatsioon** | SpringDoc OpenAPI / Swagger UI                                                   |
| **Infra / DevOps** | VM, Docker Compose, GitHub Actions CI/CD, GHCR, Github Secrets                   |

---

## 3. Arhitektuur

```mermaid
flowchart LR
    User([Kasutaja brauseris])

    subgraph FE["Frontend — React SPA"]
        UI[React + TypeScript<br/>Vite · Tailwind CSS]
    end

    subgraph BE["Backend — Spring Boot"]
        direction TB
        REST["REST kontrollerid<br/>/api/v1/*<br/>(adapter kiht)"]
        Domain["Domeeniteenused<br/>Prediction · Scoring · Group · Auth<br/>(domain kiht)"]
        Ports["Pordid — liidesed<br/>RepositoryPort · ExternalPort<br/>(domain kiht)"]
        REST --> Domain --> Ports
    end

    subgraph Adapters["Adapterid (adapter kiht)"]
        JPA[JPA / Hibernate<br/>persistence adapter]
        FD[football-data.org<br/>HTTP klient]
        GOOG[Google OAuth<br/>klient]
    end

    DB[(PostgreSQL 17<br/>Liquibase migratsioonid)]
    ExtAPI[football-data.org API]
    GoogleAuth[Google Identity API]
    Scheduler["FixtureSyncScheduler<br/>(ajastatud töö)"]

    User -->|HTTPS| UI
    UI -->|"JSON + JWT Bearer"| REST
    Ports --> JPA & FD & GOOG
    JPA --> DB
    FD -->|HTTP| ExtAPI
    GOOG -->|HTTPS| GoogleAuth
    Scheduler -->|"perioodiline kutse"| Domain
```

## 4. Backend

Backend järgib **heksagonaalset (portide ja adapterite) arhitektuuri** — domeeniloogika ei sõltu raamistikust, andmebaasist ega välistest integratsioonidest.

**Kihtide rollid:**

| Kiht | Sisu |
|------|------|
| **Domain** | Äriloogika: teenused, domeeniobjektid, pordi liidesed. Ei sõltu Spring raamistikust ega JPA-st. |
| **REST adapter** | Kontrollerid, DTO-d, päringu/vastuse mapperid, `JwtAuthFilter`, `GlobalExceptionHandler` |
| **Persistence adapter** | JPA entity-d, Spring Data repository-d, mapperid domeeniobjektideks |
| **External adapter** | football-data.org REST klient, Google OAuth token verifier |
| **Scheduler** | `FixtureSyncScheduler` kutsub domeeniteenust perioodiliselt — uuendab matšide staatused ja tulemused |

**Autentimine:** Spring Security + JWT Bearer token. Iga kaitstud päring läbib `JwtAuthFilter` → kasutaja ID lisatakse `SecurityContext`-i → kontrollerid loevad selle `AuthUtils.currentUserId()` kaudu.
**Swagger UI** on vaikimisi sisse lülitatud: `https://predictorama.online/swagger-ui/index.html`


```
com.predictorama.backend/
├── Application.java
├── config/
├── domain/                           # Zero framework dependencies
│   ├── entity/                       # Pure domain objects (no JPA / Spring)
│   ├── port/
│   │   ├── persistence/              # Repository port interfaces
│   │   └── external/                 # External service port interfaces
│   ├── service/                      # All business logic lives here
│   └── exception/
│
└── adapter/
    ├── rest/                         # HTTP layer
    │   ├── controller/               # PredictionController, GroupController, ...
    │   ├── dto/                      # Request / Response DTOs
    │   ├── mapper/                   # Domain → DTO mappers
    │   └── GlobalExceptionHandler.java
    ├── persistence/                  # JPA layer
    │   ├── adapter/                  # XxRepositoryAdapter (implements ports)
    │   ├── entity/                   # JPA entities (extend BaseEntity)
    │   ├── mapper/                   # Entity ↔ Domain mappers
    │   └── repository/               # Spring Data JPA interfaces
    ├── external/
    │   ├── footballdata/             # football-data.org HTTP client + response DTOs
    │   └── google/                   # Google OAuth token validator
    └── scheduler/                    # FixtureSyncScheduler — periodic fixture sync
```


---

## 5. Frontend
Frontend on ehitatud leheküljekesksele modulaarsele arhitektuurile — iga lehekülg elab oma kaustas src/pages/<PageName>/, millel on oma components/ ja hooks/ alamkaustad lokaalse koodi jaoks.

Kihid on rangelt lahutatud: 
- komponendid vastutavad ainult UI renderdamise eest, 
- hookid haldavad seisundit, efekte ja andmevoogu, ning 
- teenused (src/services/) tegelevad kõigi HTTP/API päringutega.
    
Mitmekeelsus on läbiv arhitektuuriline nõue: iga kasutajale nähtav tekst läbib i18next t()-funktsiooni ja võtmed peavad olema kõigis kolmes lokaalifailis (en, et, ru)
samaaegselt.


```
frontend/src/
├── main.tsx                          # Entry point
├── App.tsx                           # Root component + providers

├── app/
│   ├── routePaths.ts                 # Centralised route path constants
│   └── router/AppRouter.tsx          # Route definitions
├── components/                       # Shared across multiple pages
├── context/
├── services/                         # All HTTP — hooks call these, components never do
│
├── i18n/locales/                     # en.json · et.json · ru.json (must stay in sync)
│
└── pages/                            # One folder per page — the core structural unit
    ├── HomePage/
    │   ├── HomePage.tsx              # Thin composition root
    │   ├── components/               # UI pieces used only by this page
    │   └── hooks/                    # useUpcomingMatches, useUpcomingMatchFilters
    ├── GroupPage/
    │   ├── GroupsPage.tsx
    │   ├── components/               # CreateGroupForm, MyGroupsList, ...
    │   └── hooks/                    # useGroups, useCreateGroupForm, ...
    ├── ...
    └── ...
```

---

## 6. Andmebaasiskeem


```mermaid
erDiagram
    USERS ||--o{ GROUPS : "owner_id → id"
    USERS ||--o{ GROUP_MEMBERS : "user_id → id"
    GROUPS ||--o{ GROUP_MEMBERS : "group_id → id"
    GROUPS ||--o{ GROUP_TOURNAMENTS : "group_id → id"
    TOURNAMENTS ||--o{ GROUP_TOURNAMENTS : "tournament_id → id"
    RULESETS ||--o{ GROUP_TOURNAMENTS : "ruleset_id → id"
    RULESETS ||--o{ RULESET_RULES : "ruleset_id → id"
    TOURNAMENTS ||--o{ MATCHES : "tournament_id → id"
    TEAMS ||--o{ MATCHES : "home_team_id → id"
    TEAMS ||--o{ MATCHES : "away_team_id → id"
    MATCHES ||--o{ MATCH_SCORES : "match_id → id"
    USERS ||--o{ PREDICTIONS : "user_id → id"
    MATCHES ||--o{ PREDICTIONS : "match_id → id"
    GROUPS ||--o{ PREDICTIONS : "group_id → id"
    PREDICTIONS ||--o{ PREDICTION_SCORES : "prediction_id → id"

    USERS {
        uuid id PK
        varchar username UK
        varchar email UK
        varchar system_role
        varchar password_hash
        varchar google_id UK
    }
    GROUPS {
        uuid id PK
        uuid owner_id FK
        uuid invite_code UK
        varchar name
        varchar description
    }
    GROUP_MEMBERS {
        uuid id PK
        uuid user_id FK
        uuid group_id FK
        varchar status
        varchar member_role
    }
    GROUP_TOURNAMENTS {
        uuid group_id FK "PK"
        uuid tournament_id FK "PK"
        uuid ruleset_id FK
    }
    RULESETS {
        uuid id PK
    }
    RULESET_RULES {
        uuid ruleset_id FK "PK"
        varchar rule_name "PK"
        int points
    }
    TOURNAMENTS {
        uuid id PK
        varchar name
        varchar sport
        varchar season_label
        varchar season_identifier
    }
    TEAMS {
        uuid id PK
        varchar name
        varchar image_url
        varchar external_id UK
    }
    MATCHES {
        uuid id PK
        uuid tournament_id FK
        uuid home_team_id FK
        uuid away_team_id FK
        varchar name
        varchar description
        varchar match_status
        timestamptz kickoff_time
        varchar round_identifier
        varchar group_identifier
        int matchday_identifier
        varchar winner
        varchar external_id UK
    }
    MATCH_SCORES {
        uuid id PK
        uuid match_id FK
        varchar score_type
        int home_score
        int away_score
    }
    PREDICTIONS {
        uuid id PK
        uuid user_id FK
        uuid match_id FK
        uuid group_id FK
        varchar predicted_winner
        int result
        timestamptz submitted_at
    }
    PREDICTION_SCORES {
        uuid id PK
        uuid prediction_id FK
        varchar score_type
        int home_score
        int away_score
    }
```

**Olulised disainiotsused:**
- `predictions` on unikaalne `(user_id, match_id, group_id)` — sama kasutaja saab samas grupis iga matši kohta täpselt ühe ennustuse, kuid eri gruppides võib olla erinev ennustus
- Skoorid on normaliseeritud eraldi tabelitesse (`match_scores`, `prediction_scores`) — võimaldab lisada `EXTRA_TIME`, `PENALTIES` ilma skeemi muutmata
- **Reeglistik on seotud `group_tournaments` tasemel**, mitte grupi tasemel — grupp võib ühes turniiris kasutada üht reeglistikku ja teises teist
- `ruleset_rules` tabel on lihtne võtme-väärtuse kaart: reegli nimi (`EXACT_SCORE`, `CORRECT_WINNER`, `CORRECT_GOAL_DIFFERENCE`) → punktid. Uue reegli lisamine ei nõua skeemi muutust.
---
