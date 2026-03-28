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
│   │   ├── Role.java                 # Enum: ADMIN, USER
│   │   ├── Group.java
│   │   ├── GroupMember.java          # Includes MemberStatus enum (ACTIVE, INACTIVE)
│   │   ├── GroupInvite.java          # Includes GroupInviteStatus enum
│   │   ├── Tournament.java           # Includes Sport enum
│   │   ├── Match.java                # Includes MatchStatus enum
│   │   ├── Score.java                # Value object — includes ScoreType enum
│   │   ├── Team.java                 # Value object
│   │   └── Prediction.java
│   ├── port/                         # Outbound port interfaces
│   │   ├── UserRepositoryPort.java
│   │   ├── GroupRepositoryPort.java
│   │   └── GroupMemberRepositoryPort.java
│   └── service/                      # Business logic — depends only on ports
│       ├── UserService.java
│       └── GroupService.java
│
└── adapter/
    ├── persistence/                  # Database adapter
    │   ├── entity/
    │   │   ├── BaseEntity.java       # @MappedSuperclass — createdAt/updatedAt
    │   │   ├── UserEntity.java
    │   │   ├── GroupEntity.java
    │   │   └── GroupMemberEntity.java
    │   ├── repository/               # Spring Data JPA interfaces
    │   │   ├── UserJpaRepository.java
    │   │   ├── GroupJpaRepository.java
    │   │   └── GroupMemberJpaRepository.java
    │   ├── mapper/                   # Static toDomain / toEntity conversions
    │   │   ├── UserMapper.java
    │   │   ├── GroupMapper.java
    │   │   └── GroupMemberMapper.java
    │   └── adapter/                  # Implements domain ports, delegates to JPA repo
    │       ├── UserRepositoryAdapter.java
    │       ├── GroupRepositoryAdapter.java
    │       └── GroupMemberRepositoryAdapter.java
    ├── integrations/                 # E.g external API
    │   └── api123/
    └── rest/                         # HTTP adapter
        ├── HealthController.java
        ├── controller/
        │   ├── UserController.java
        │   └── GroupController.java
        └── dto/
            ├── CreateUserRequest.java
            ├── UserResponse.java

```

---

## Domain Model

### Entities

**User**
Represents a registered person. Has a username, email, and a system-level role (ADMIN or USER). The role is system-wide, not group-specific.

**Group**
A named prediction league that users can belong to. Created by an owner, identified by a UUID invite code that others can use to join.

**GroupMember**
The relationship between a User and a Group. Carries a per-group role (ADMIN or USER via `Role`) and a membership status (ACTIVE / INACTIVE). One record per user-group pair.

**GroupInvite**
An explicit email-based invite to join a group. Tracks who sent it and its status (PENDING / ACCEPTED / DECLINED). Separate from the invite code flow — used for targeted invitations.

**Tournament**
A competition (e.g. FIFA World Cup). Scoped to a sport (currently only FOOTBALL). Groups and predictions are made in the context of a tournament's matches.

**Match**
A single fixture within a tournament. Has a home and away team (`Team` value objects), a status lifecycle (SCHEDULED → LIVE → COMPLETED / CANCELLED), a kickoff time, and an optional final score (`Score` value object).

**Score** *(value object)*
The score of a match. Holds home and away goals plus a score type (NORMAL_TIME / FULL_TIME). Used both for final match scores and predicted scores in `Prediction`.

**Team** *(value object)*
A named team. Simple wrapper around a name string — used as home/away team on `Match` and as `predictedWinner` on `Prediction`.

**Prediction**
A user's prediction for a match within a group. Stores a predicted score, a predicted winner (team), and an integer result (points awarded after scoring). Unique per user + match + group combination.

---

## Files Required Per Entity

The pattern is established and consistent across User and Group. Each entity requires the following files:

### Domain layer

| File | Package | Purpose |
|------|---------|---------|
| `{Entity}.java` | `domain/entity/` | Pure Java class with Lombok (`@Getter`, `@Builder`, `@AllArgsConstructor`). No Spring/JPA annotations. Enums can be inner or top-level in the same package. |
| `{Entity}RepositoryPort.java` | `domain/port/` | Interface defining the persistence contract. Methods: `save`, `findById`, plus query methods the service actually needs. |
| `{Entity}Service.java` | `domain/service/` | Business logic. Receives ports via constructor injection. Registered as a `@Bean` in `DomainConfig`. |

> Value objects (`Score`, `Team`) are used by entities but do not get their own port or service — they have no independent lifecycle.

### Persistence adapter layer

| File | Package | Purpose |
|------|---------|---------|
| `{Entity}Entity.java` | `adapter/persistence/entity/` | JPA-annotated class. Extends `BaseEntity` for audit timestamps. `@Enumerated(EnumType.STRING)` for enums. No `@ManyToOne` — use UUID FK fields only. |
| `{Entity}JpaRepository.java` | `adapter/persistence/repository/` | Extends `JpaRepository<{Entity}Entity, UUID>`. Declares query methods Spring Data can derive or `@Query` methods where needed. |
| `{Entity}Mapper.java` | `adapter/persistence/mapper/` | Static utility class. Two methods: `toDomain({Entity}Entity)` and `toEntity({Entity})`. Handles value object decomposition if needed. |
| `{Entity}RepositoryAdapter.java` | `adapter/persistence/adapter/` | Annotated `@Repository`. Implements `{Entity}RepositoryPort`. Delegates to JPA repo, maps via mapper. |

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

The file is included in `db.changelog-master.yaml`. Conventions: UUID primary keys (generated in app, not DB), `varchar` for enum columns, no DB-level foreign key enforcement required but recommended for data integrity.

---

## Implementation Status

| Entity | Domain entity | Port | Service | Persistence adapter | REST controller | Migration |
|--------|:---:|:---:|:---:|:---:|:---:|:---:|
| User | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ `002` |
| Group | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ `003` |
| GroupMember | ✅ | ✅ | (in GroupService) | ✅ | (in GroupController) | ✅ `003` |
| GroupInvite | ✅ | — | — | — | — | — |
| Tournament | ✅ | — | — | — | — | — |
| Match | ✅ | — | — | — | — | — |
| Score | ✅ (value obj) | — | — | — | — | — |
| Team | ✅ (value obj) | — | — | — | — | — |
| Prediction | ✅ | — | — | — | — | — |
