# Predict-o-rama — Tehniline ülevaade (3 min)

> Esitluse mustand: ~3 minutit. Iga peatükk = ~30 s rääkimist.
> Diagrammid on Mermaid-formaadis — renderduvad GitHubis ja enamikus Markdown-tööriistades.

---

## 1. Mis on Predict-o-rama? (~20 s)

Predict-o-rama on jalgpalli ennustusmäng peredele ja sõpruskondadele. Kasutajad loovad gruppe, ennustavad päris turniiride matšide tulemusi (Premier League, Champions League, MM), ja saavad punkte vastavalt grupi reeglistikule. Matšiandmed sünkroonitakse automaatselt football-data.org API-st.

**Põhifunktsioonid:**
- Kasutaja autentimine (e-mail/parool + Google OAuth)
- Gruppide loomine ja kutselinkidega liitumine
- Matšide ennustamine (skoor + võitja)
- Automaatne tulemuste skoorimine konfigureeritava reeglistiku alusel
- Edetabel grupi sees turniiri kohta

---

## 2. Arhitektuur (~40 s)

Klassikaline kolmekihiline arhitektuur: React SPA → Spring Boot REST API → PostgreSQL. 

Backend:
- heksagonaalne arhitektuur - **portide ja adapterite** mustrit — domeeniloogika ei sõltu raamistikust, välistest integratsioonidest ega andmebaasist
- domain kiht
  - defineerib ära domeeniobjektid, serviced ja pordid
- adapter kiht
  - defineeritakse controllerid, dto, db entity-d, repository adapterid, external integrations adapterid
  - integratsioonid:
    - football.org API - automatiseeritud andmete import, mis tõmbab perioodiliselt (scheduled jobina) uusi matše ja tulemusi)
    - Google - Google auth kasutades Google Client Id-d -> authenitificaton tulemusena väljastakse JWT
  - REST API:
    - versioneeritud api endpointid
    - Spring security ja JWT securitycontext
  - Persistance
    - JPA repository -
  - 

Andmebaas:
- JPA 
- Andmebaasi migratsioonid kasutades Liquibase'i 

Frontend: 
- React
  **Olulised punktid:**
- Frontend ja backend on Docker-kontainerid, ees Caddy/nginx reverse proxy
- JWT Bearer token autentimisel — stateless, ei vaja sessioonisalvestust
- Domain-kihis on liides (`port`), implementatsioon (`adapter`) on välimises kihis → kergesti testitav, raamistikust sõltumatu
- Ajastatud `FixtureSyncScheduler` tõmbab perioodiliselt uusi matše ja tulemusi


```mermaid
flowchart LR
    User([Kasutaja brauseris])

    subgraph Frontend["Frontend - React SPA"]
        UI[React + TypeScript<br/>Vite + Tailwind]
    end

    subgraph Backend["Backend - Spring Boot"]
        REST[REST kontrollerid<br/>/api/v1/*]
        Domain[Domeeniteenused<br/>Prediction, Scoring, Group, Auth]
        Ports[Pordid<br/>Repository / External]
    end

    subgraph Adapters["Adapterid"]
        JPA[JPA / Hibernate]
        FD[football-data.org klient]
        GOOG[Google OAuth klient]
    end

    DB[(PostgreSQL 17<br/>Liquibase migratsioonid)]
    External[football-data.org API]

    User -->|HTTPS| UI
    UI -->|JSON + JWT Bearer| REST
    REST --> Domain
    Domain --> Ports
    Ports --> JPA
    Ports --> FD
    Ports --> GOOG
    JPA --> DB
    FD -->|HTTP| External

    Scheduler[Fixture sync<br/>scheduler] --> Domain
```


---

## 3. Tehnoloogia stack (~25 s)

| Kiht | Tehnoloogiad |
|------|--------------|
| **Frontend** | React 19, TypeScript, Vite, Tailwind CSS, React Router 7, i18next, Vitest |
| **Backend** | Spring Boot 4, Java 21, Spring Web MVC, Spring Data JPA, Spring Security, Lombok |
| **Auth** | JWT (jjwt), Google OAuth (google-api-client), BCrypt |
| **Andmebaas** | PostgreSQL 17, Liquibase (migratsioonid), Hibernate (ainult validate) |
| **Välised teenused** | football-data.org (matšid), Google Identity (sisselogimine) |
| **Dokumentatsioon** | SpringDoc OpenAPI / Swagger UI |
| **Infra / DevOps** | Docker Compose, GitHub Actions (CI/CD), GHCR, deploy üle SSH VPS-i |

---

## 4. Andmebaasiskeem (~45 s)

Põhientiteedid: **kasutaja → grupp → ennustus → matš → turniir**. Reeglistik (`ruleset`) seob grupi ja turniiri konkreetsete skooripunktidega. Iga match-skoor ja ennustuse-skoor on eraldi tabelites, et toetada lisaaegu ja penaltisid.

```mermaid
erDiagram
    USERS ||--o{ GROUPS : "owns"
    USERS ||--o{ GROUP_MEMBERS : "joins"
    GROUPS ||--o{ GROUP_MEMBERS : "has"
    GROUPS }o--|| RULESETS : "uses"
    GROUPS ||--o{ GROUP_TOURNAMENTS : "tracks"
    TOURNAMENTS ||--o{ GROUP_TOURNAMENTS : "in"
    TOURNAMENTS ||--o{ MATCHES : "contains"
    TEAMS ||--o{ MATCHES : "home/away"
    MATCHES ||--o{ MATCH_SCORES : "actual"
    MATCHES ||--o{ PREDICTIONS : "predicted"
    USERS ||--o{ PREDICTIONS : "submits"
    GROUPS ||--o{ PREDICTIONS : "scope"
    PREDICTIONS ||--o{ PREDICTION_SCORES : "values"
    RULESETS ||--o{ RULESET_RULES : "defines"

    USERS {
        uuid id PK
        string username UK
        string email UK
        string system_role
        string password_hash
    }
    GROUPS {
        uuid id PK
        uuid owner_id FK
        uuid invite_code UK
        uuid ruleset_id FK
        string name
    }
    GROUP_MEMBERS {
        uuid id PK
        uuid user_id FK
        uuid group_id FK
        string status
        string member_role
    }
    TOURNAMENTS {
        uuid id PK
        string name
        string sport
        string season_label
    }
    TEAMS {
        uuid id PK
        string name
        string image_url
        string external_id
    }
    MATCHES {
        uuid id PK
        uuid tournament_id FK
        uuid home_team_id FK
        uuid away_team_id FK
        string match_status
        string winner
        string external_id
        timestamp kickoff_time
    }
    MATCH_SCORES {
        uuid id PK
        uuid match_id FK
        string score_type
        int home_score
        int away_score
    }
    PREDICTIONS {
        uuid id PK
        uuid user_id FK
        uuid match_id FK
        uuid group_id FK
        string predicted_winner
        int result
        timestamp submitted_at
    }
    PREDICTION_SCORES {
        uuid id PK
        uuid prediction_id FK
        string score_type
        int home_score
        int away_score
    }
    RULESETS {
        uuid id PK
        string name
    }
    RULESET_RULES {
        uuid ruleset_id FK
        string rule_name
        int points
    }
    GROUP_TOURNAMENTS {
        uuid group_id FK
        uuid tournament_id FK
    }
```

**Tähelepanekud:**
- `predictions` on unikaalne `(user_id, match_id, group_id)` peale — sama kasutaja võib sama matši kohta erinevates gruppides erineva ennustuse teha
- Skoorid on normaliseeritud eraldi tabelitesse (`match_scores`, `prediction_scores`) → toetab `NORMAL_TIME`, `FULL_TIME`, `EXTRA_TIME`, `PENALTIES` lisamist ilma skeemimuudatuseta
- Liquibase muudatuste fail on rangelt versioneeritud (`001-init.yaml` … `019-configurable-rulesets.yaml`)

---

## 5. Voodiagramm — ennustuse esitamine (~30 s)

Tüüpiline kasutusvoog: kasutaja vaatab oma grupi turniiri, esitab ennustuse, backend valideerib ja salvestab.

```mermaid
sequenceDiagram
    actor U as Kasutaja
    participant FE as React Frontend
    participant API as Spring REST API
    participant SVC as PredictionService
    participant DB as PostgreSQL

    U->>FE: Avab turniirilehe
    FE->>API: GET /api/v1/predictions?competition=PL&groupId=...
    API->>DB: SELECT matches + user predictions
    DB-->>API: matšid + olemasolevad ennustused
    API-->>FE: JSON: matches[]
    FE-->>U: Kuvab matšid + vormid

    U->>FE: Sisestab skoori 2:1, võitja HOME
    FE->>API: POST /api/v1/predictions { matchId, homeScore, awayScore, winner }
    API->>API: JwtAuthFilter valideerib tokeni
    API->>SVC: savePrediction(userId, groupId, matchId, ...)
    SVC->>SVC: validatePredictionInput()
    SVC->>DB: findByUserIdAndMatchIdAndGroupId
    DB-->>SVC: Optional<Prediction>
    SVC->>DB: INSERT või UPDATE prediction
    DB-->>SVC: salvestatud
    SVC-->>API: Prediction
    API-->>FE: 200 OK
    FE-->>U: "Ennustus salvestatud"
```

---

## 6. Tegevusdiagramm — automaatne skoorimine (~30 s)

Kui matš lõppeb, käivitatakse skoorimisahel: tegelik tulemus võrreldakse iga ennustusega, ja vastavalt grupi reeglistikule arvutatakse punktid.

```mermaid
flowchart TD
    Start([FixtureSyncScheduler<br/>tõmbab matši tulemused]) --> Update[Uuendab MATCHES.status = FINISHED<br/>+ MATCH_SCORES]
    Update --> Trigger[Käivitab<br/>PredictionScoringService.distributePredictionScores matchId]

    Trigger --> FindMatch{Match leitud?}
    FindMatch -->|Ei| Error1[Viskab erindi]
    FindMatch -->|Jah| GetPreds[Loeb kõik ennustused<br/>selle matši kohta]

    GetPreds --> GroupBy[Grupeerib ennustused<br/>group_id järgi]
    GroupBy --> Loop{Iga grupi kohta}

    Loop --> FindRuleset[Leiab ruleset<br/>group_id + tournament_id alusel]
    FindRuleset --> RulesetExists{Ruleset olemas?}
    RulesetExists -->|Ei| Default[Kasutab DEFAULT_RULE_POINTS]
    RulesetExists -->|Jah| Custom[Kasutab grupi reeglistikku]

    Default --> CalcLoop
    Custom --> CalcLoop[Iga ennustuse kohta<br/>arvuta punktid]

    CalcLoop --> ApplyRules[Iga aktiivne reegel:<br/>ExactScoreRule<br/>CorrectWinnerRule<br/>CorrectGoalDifferenceRule]
    ApplyRules --> Match{Reegel<br/>matchib?}
    Match -->|Jah| AddPoints[Liidab reegli punktid]
    Match -->|Ei| Skip[Skip]
    AddPoints --> NextRule{Veel reegleid?}
    Skip --> NextRule
    NextRule -->|Jah| ApplyRules
    NextRule -->|Ei| Save[UPDATE predictions.result = total]

    Save --> NextPred{Veel ennustusi?}
    NextPred -->|Jah| CalcLoop
    NextPred -->|Ei| NextGroup{Veel gruppe?}
    NextGroup -->|Jah| Loop
    NextGroup -->|Ei| Done([Skoorimine valmis<br/>edetabel uuendatud])
```

**Disainivõit:** uue skoorimisreegli lisamine = uus klass, mis implementeerib `ScoringRule`-liidese. Spring DI korjab selle automaatselt üles, seda saab lisada igasse reeglistikku ilma teisi reegleid muutmata. **Open/Closed põhimõte praktikas.**

---

## 7. Kokkuvõte (~10 s)

- **Selge kihtide eraldatus** (hexagonal): domeeniloogikat saab katsetada ilma andmebaasita
- **Konfigureeritav reeglistik** grupi tasemel — sama matš annab erinevatele gruppidele erinevad punktid
- **Liquibase + ddl-auto=validate** garanteerib, et kood ja andmebaas on alati sünkroonis
- **CI/CD** GitHub Actions kaudu pushib Docker-image'id GHCR-i ja deploib VPS-i automaatselt push-il `dev` haru peale
