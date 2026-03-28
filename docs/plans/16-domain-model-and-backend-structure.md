# Backend Structure & Domain Model

## Overview

The backend is a Spring Boot 4.0.3 / Java 21 application using **hexagonal architecture** (ports & adapters). The core idea: the domain layer is pure Java with no Spring or JPA dependencies, and all infrastructure concerns (database, HTTP) live in adapter layers that depend inward on the domain.

---

## Package Structure

```
com.predictorama.backend/
├── Application.java
├── config/
│   ├── CorsConfig.java               # CORS — reads ALLOWED_ORIGIN env var
│   └── DomainConfig.java             # @Bean wiring for domain services
│
├── domain/
│   ├── entity/                       # Pure Java domain objects (no Spring/JPA)
│   │   ├── User.java
│   │   ├── Role.java                 # Enum: ADMIN, USER (system-wide)
│   │   ├── Winner.java               # Enum: HOME, AWAY, DRAW (shared by Match + Prediction)
│   │   ├── Group.java
│   │   ├── GroupMember.java          # Includes MemberStatus enum (ACTIVE, INACTIVE)
│   │   ├── Tournament.java           # Includes Sport enum (FOOTBALL)
│   │   ├── Match.java                # Includes MatchStatus enum (SCHEDULED, LIVE, COMPLETED, CANCELLED)
│   │   ├── Score.java                # Value object — includes ScoreType enum (NORMAL_TIME, FULL_TIME, PENALTIES)
│   │   ├── Team.java                 # Value object — home/away team name
│   │   └── Prediction.java
│   ├── port/                         # Outbound port interfaces
│   │   ├── UserRepositoryPort.java
│   │   ├── GroupRepositoryPort.java
│   │   ├── GroupMemberRepositoryPort.java
│   │   ├── TournamentRepositoryPort.java
│   │   ├── MatchRepositoryPort.java
│   │   └── PredictionRepositoryPort.java
│   └── service/                      # Business logic — depends only on ports
│       ├── UserService.java
│       └── GroupService.java
│
└── adapter/
    ├── persistence/                  # Database adapter
    │   ├── entity/
    │   │   ├── BaseEntity.java           # @MappedSuperclass — createdAt/updatedAt
    │   │   ├── UserEntity.java
    │   │   ├── GroupEntity.java
    │   │   ├── GroupMemberEntity.java
    │   │   ├── TournamentEntity.java
    │   │   ├── MatchEntity.java
    │   │   ├── MatchScoreEntity.java     # Child table — one row per score stage per match
    │   │   ├── PredictionEntity.java
    │   │   └── PredictionScoreEntity.java  # Child table — one row per score stage per prediction
    │   ├── repository/               # Spring Data JPA interfaces
    │   │   ├── UserJpaRepository.java
    │   │   ├── GroupJpaRepository.java
    │   │   ├── GroupMemberJpaRepository.java
    │   │   ├── TournamentJpaRepository.java
    │   │   ├── MatchJpaRepository.java
    │   │   ├── MatchScoreJpaRepository.java
    │   │   ├── PredictionJpaRepository.java
    │   │   └── PredictionScoreJpaRepository.java
    │   ├── mapper/                   # Static toDomain / toEntity conversions
    │   │   ├── UserMapper.java
    │   │   ├── GroupMapper.java
    │   │   ├── GroupMemberMapper.java
    │   │   ├── TournamentMapper.java
    │   │   ├── MatchMapper.java
    │   │   ├── MatchScoreMapper.java
    │   │   ├── PredictionMapper.java
    │   │   └── PredictionScoreMapper.java
    │   └── adapter/                  # Implements domain ports, delegates to JPA repo
    │       ├── UserRepositoryAdapter.java
    │       ├── GroupRepositoryAdapter.java
    │       ├── GroupMemberRepositoryAdapter.java
    │       ├── TournamentRepositoryAdapter.java
    │       ├── MatchRepositoryAdapter.java
    │       └── PredictionRepositoryAdapter.java
    ├── integrations/                 # External API clients (e.g. sports data providers)
    └── rest/                         # HTTP adapter
        ├── HealthController.java
        ├── controller/
        │   └── UserController.java
        └── dto/
            ├── CreateUserRequest.java
            └── UserResponse.java

```

---

## Domain Model

### Entities

**User**
Represents a registered person. Has a username, email, and a system-level `Role` (ADMIN or USER). The role is system-wide, not group-specific.

**Group**
A named prediction league that users can belong to. Created by an owner, identified by a UUID invite code that others can use to join.

**GroupMember**
The relationship between a User and a Group. Carries a per-group `Role` and a membership status (ACTIVE / INACTIVE). One record per user-group pair.

**GroupInvite**
An explicit email-based invite to join a group. Tracks who sent it and its status (PENDING / ACCEPTED / DECLINED). Separate from the invite-code join flow — used for targeted invitations by email.

**Tournament**
A competition (e.g. FIFA World Cup). Scoped to a sport (currently only FOOTBALL).

**Match**
A single fixture within a tournament. Has a home and away team (`Team` value objects), a status lifecycle (SCHEDULED → LIVE → COMPLETED / CANCELLED), a kickoff time, a list of `Score` records (one per stage played), and an explicit `Winner` (HOME / AWAY / DRAW — nullable until the match is completed).

**Score** *(value object)*
One scored stage of a match or a predicted stage. Holds home goals, away goals, and a `ScoreType` (NORMAL_TIME / FULL_TIME / PENALTIES). A match or prediction can have multiple `Score` records — one per stage. Stored in a dedicated child table (`match_scores` / `prediction_scores`) rather than inlined columns, to support multi-stage results without schema changes.

**Team** *(value object)*
A named team. Simple name wrapper used as home/away team on `Match`.

**Winner** *(enum)*
Shared enum (HOME / AWAY / DRAW) used by both `Match` (actual result) and `Prediction` (predicted outcome). Extracted as a top-level type because it is meaningful independent of either entity.

**Prediction**
A user's prediction for a match within a group. Stores a list of predicted `Score` records (one per stage the user wants to predict), a predicted `Winner`, and an integer result (points awarded after scoring). Unique per user + match + group combination.

---

## Files Required Per Entity

### Domain layer

| File | Package | Purpose |
|------|---------|---------|
| `{Entity}.java` | `domain/entity/` | Pure Java class with Lombok (`@Getter`, `@Builder`, `@AllArgsConstructor`). No Spring/JPA annotations. Enums can be inner or top-level. |
| `{Entity}RepositoryPort.java` | `domain/port/` | Interface defining the persistence contract. Methods: `save`, `findById`, plus query methods the service actually needs. |
| `{Entity}Service.java` | `domain/service/` | Business logic. Receives ports via constructor injection. Registered as a `@Bean` in `DomainConfig`. |

> Value objects (`Score`, `Team`) and shared enums (`Winner`, `Role`) do not get their own port or service — they have no independent lifecycle.

### Persistence adapter layer

| File | Package | Purpose |
|------|---------|---------|
| `{Entity}Entity.java` | `adapter/persistence/entity/` | JPA-annotated class. Extends `BaseEntity` for audit timestamps. `@Enumerated(EnumType.STRING)` for enums. No `@ManyToOne` — use UUID FK fields only. |
| `{Entity}JpaRepository.java` | `adapter/persistence/repository/` | Extends `JpaRepository<{Entity}Entity, UUID>`. Declares derived or `@Query` methods as needed. |
| `{Entity}Mapper.java` | `adapter/persistence/mapper/` | Static utility class with `toDomain` and `toEntity` methods. For entities with child score tables, `toDomain` takes a pre-fetched `List<Score>` and the adapter handles loading. |
| `{Entity}RepositoryAdapter.java` | `adapter/persistence/adapter/` | `@Repository`. Implements the domain port. Save methods are `@Transactional` when they write to multiple tables (e.g. parent entity + child scores). |

**Child score table pattern (Match, Prediction):**
Scores are stored in separate tables (`match_scores`, `prediction_scores`) rather than as inlined columns. The adapter's `save` method is `@Transactional`: it saves the parent entity, deletes existing score rows, then inserts the new set. `findById` and list methods load score rows separately via a private `loadScores()` helper.

### REST adapter layer

| File | Package | Purpose |
|------|---------|---------|
| `{Create/Update}Request.java` | `adapter/rest/dto/` | Java record. Fields are the inputs the API consumer must provide. |
| `{Entity}Response.java` | `adapter/rest/dto/` | Java record with a static `from({Entity})` factory method. Controls what is exposed over the API. |
| `{Entity}Controller.java` | `adapter/rest/controller/` | `@RestController`. Injects the domain service. Maps HTTP requests to service calls and DTOs to responses. |

### Configuration

Each new service must be wired in `config/DomainConfig.java`:
```java
@Bean
public {Entity}Service {entity}Service({Entity}RepositoryPort port) {
    return new {Entity}Service(port);
}
```

### Liquibase migration

Each entity backed by a DB table gets a new changelog file:

| File | Location |
|------|---------|
| `{NNN}-create-{table}.yaml` | `src/main/resources/db/changelog/` |

The file is included in `db.changelog-master.yaml`. Conventions: UUID primary keys (generated in app, not DB), `varchar` for enum columns, no `@ManyToOne` FKs in JPA but FK constraints in the migration are recommended for data integrity.

---

## Implementation Status

| Entity | Domain entity | Port | Service | Persistence adapter | REST controller | Migration |
|--------|:---:|:---:|:---:|:---:|:---:|:---:|
| User | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ `002` |
| Group | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ `003` |
| GroupMember | ✅ | ✅ | (in GroupService) | ✅ | (in GroupController) | ✅ `003` |
| GroupInvite | ✅ | — | — | — | — | — |
| Tournament | ✅ | ✅ | — | ✅ | — | — |
| Match | ✅ | ✅ | — | ✅ | — | — |
| Prediction | ✅ | ✅ | — | ✅ | — | — |
| Score | ✅ (value obj) | — | — | — | — | — |
| Team | ✅ (value obj) | — | — | — | — | — |
| Winner | ✅ (enum) | — | — | — | — | — |
