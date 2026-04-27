# Plan: Configurable Rulesets per Group×Tournament (#79)

## Context

Predict-o-rama currently scores predictions using two hardcoded Spring beans (`CorrectWinnerRule` = 1pt, `ExactScoreRule` = 3pts). There is DB scaffolding from migration 009 (`rulesets`, `ruleset_rules` tables, `groups.ruleset_id`) but it stores only rule names (no point values), has a vestigial `name` column, and is scoped per-group instead of per-group×tournament.

This feature lets group admins configure which rules are active and how many points each awards — separately per tournament within a group. Non-admins can view the rules read-only. Changes trigger async score recalculation.

---

## Decisions

- **Scope**: per group×tournament (not per group)
- **Configurable**: toggle on/off + edit point values per rule
- **Disabled rule storage**: disabled rules are absent from the `rulePoints` map — absence means disabled
- **UI**: expandable inline section within the tournament row (consistent with `CreateGroupForm` pattern)
- **Non-admin view**: same expandable section, all inputs disabled (no Save button)
- **Default on tournament add**: all rules ON at default points (CORRECT_WINNER=1, EXACT_SCORE=3, CORRECT_GOAL_DIFFERENCE=2)
- **Lock**: no lock — admin can edit anytime
- **On save**: async recalculation — PUT returns immediately with the saved ruleset; recalculation runs in background via `@Async`; frontend refreshes leaderboard after a short delay
- **New rule**: CORRECT_GOAL_DIFFERENCE — awards points if predicted goal diff equals actual (exact match only)
- **DB migration**: drop `rulesets.name`, drop `groups.ruleset_id`, add `points` column to `ruleset_rules`, add composite PK to `ruleset_rules`, add `ruleset_id` to `group_tournaments` — no data migration (DB reset via `compose down -v`)

---

## Phase 1: Database Migration (`019`)

**File:** `backend/src/main/resources/db/changelog/019-configurable-rulesets.yaml`

Execute in this order:

1. Drop FK constraint `fk_groups_ruleset`, then drop column `ruleset_id` from `groups`
2. Drop the `name` column from `rulesets` (vestigial — rulesets are anonymous, identified by their group×tournament link)
3. Add `points INT NOT NULL DEFAULT 0` column to `ruleset_rules`
4. Add composite PK `(ruleset_id, rule_name)` to `ruleset_rules`
5. Add `ruleset_id UUID` (nullable FK → `rulesets(id)`) column to `group_tournaments`

> No data migration needed — DB is reset with `compose down -v` before deploying.

---

## Phase 2: Backend — Domain & Scoring

### 2a. `ScoringRule` interface
**File:** `backend/.../domain/service/scoring/ScoringRule.java`

Change `int evaluate(...)` → `boolean matches(Prediction, Score, Winner)`.
The scoring service now owns the points; rules only check the condition.

### 2b. Update existing rules
- `CorrectWinnerRule.java`: rename `evaluate` → `matches`, return `boolean`
- `ExactScoreRule.java`: rename `evaluate` → `matches`, return `boolean`

### 2c. Add new rule
**New file:** `backend/.../domain/service/scoring/CorrectGoalDifferenceRule.java`
- `matches()`: guard for null scores first (return false if either score is null), then return `true` if `(prediction.homeScore - prediction.awayScore) == (actualScore.home - actualScore.away)`
- `name()`: `"CORRECT_GOAL_DIFFERENCE"`
- Annotate `@Component`

### 2d. `Ruleset` domain entity
**File:** `backend/.../domain/entity/Ruleset.java`

- Change `Set<String> ruleNames` → `Map<String, Integer> rulePoints` (key = rule name, value = configured points; absence = disabled)
- Remove `name` field

### 2e. `RulesetEntity` persistence
**File:** `backend/.../adapter/persistence/entity/RulesetEntity.java`

- Remove `name` field and its `@Column` mapping
- Change `@ElementCollection Set<String> ruleNames` to:
  ```java
  @ElementCollection
  @CollectionTable(name = "ruleset_rules", joinColumns = @JoinColumn(name = "ruleset_id"))
  @MapKeyColumn(name = "rule_name")
  @Column(name = "points")
  private Map<String, Integer> rulePoints;
  ```

### 2f. Purge `name` from ruleset infrastructure

The `name` field no longer exists on `rulesets`. Remove all related code:

- **`RulesetMapper.java`**: remove `.name(...)` from both `toDomain` and `toEntity` mapping methods
- **`RulesetRepositoryPort.java`**: remove `findByName(String name)`, `findAll()`, `save(Ruleset)`, `findById(UUID)` — replace entire interface with only:
  ```java
  Optional<Ruleset> findByGroupIdAndTournamentId(UUID groupId, UUID tournamentId);
  Ruleset upsertForGroupTournament(UUID groupId, UUID tournamentId, Map<String, Integer> rulePoints);
  ```
- **`RulesetJpaRepository.java`**: remove `findByName` method (no longer needed)
- **`RulesetRepositoryAdapter.java`**: remove `save`, `findById`, `findByName`, `findAll` implementations; implement the two new port methods (see §2g)

### 2g. `GroupEntity` persistence
**File:** `backend/.../adapter/persistence/entity/GroupEntity.java`

Remove `rulesetId` field (column dropped in migration).

### 2h. `Group` domain entity
**File:** `backend/.../domain/entity/Group.java`

Remove `rulesetId` field.

### 2i. `GroupTournamentEntity`
**File:** `backend/.../adapter/persistence/entity/GroupTournamentEntity.java`

Add `rulesetId UUID` field:
```java
@Column(name = "ruleset_id")
private UUID rulesetId;
```

Also add a JPQL update method to `GroupTournamentJpaRepository`:
```java
@Modifying
@Query("UPDATE GroupTournamentEntity g SET g.rulesetId = :rulesetId WHERE g.groupId = :groupId AND g.tournamentId = :tournamentId")
void updateRulesetId(@Param("groupId") UUID groupId, @Param("tournamentId") UUID tournamentId, @Param("rulesetId") UUID rulesetId);
```

### 2j. Adapter: `RulesetRepositoryAdapter`
**File:** `backend/.../adapter/persistence/adapter/RulesetRepositoryAdapter.java`

Implement the two new port methods:

**`findByGroupIdAndTournamentId`**: join-fetch `GroupTournamentEntity` → `RulesetEntity` via `rulesetId`.

**`upsertForGroupTournament`**:
1. Load `GroupTournamentEntity` by `(groupId, tournamentId)`
2. If `rulesetId` is not null, load existing `RulesetEntity` and update its `rulePoints` map in place → save → return mapped domain object
3. If `rulesetId` is null, create a new `RulesetEntity` (new UUID, set `rulePoints`) → save it → call `groupTournamentJpaRepository.updateRulesetId(groupId, tournamentId, newRuleset.getId())` → return mapped domain object

Both steps happen within the same `@Transactional` call.

### 2k. Add `findFinishedByTournamentId` to match infrastructure

**`MatchRepositoryPort.java`**: add method:
```java
List<Match> findFinishedByTournamentId(UUID tournamentId);
```

**`MatchJpaRepository.java`**: add:
```java
List<MatchEntity> findByTournamentIdAndStatusIn(UUID tournamentId, Collection<MatchStatus> statuses);
```

**`MatchRepositoryAdapter.java`**: implement port method using the above, passing the finished/completed statuses.

### 2l. New `ScoreRecalculationService`
**New file:** `backend/.../domain/service/ScoreRecalculationService.java`

```java
@Service
public class ScoreRecalculationService {

    private final PredictionScoringService predictionScoringService;
    private final MatchRepositoryPort matchRepositoryPort;

    @Async
    public void recalculateForGroupTournament(UUID groupId, UUID tournamentId) {
        // groupId unused in scoring (distributePredictionScores re-scores all groups
        // for a match — idempotent for groups whose ruleset hasn't changed)
        matchRepositoryPort.findFinishedByTournamentId(tournamentId)
            .forEach(match -> {
                try {
                    predictionScoringService.distributePredictionScores(match.getId());
                } catch (Exception e) {
                    log.error("Recalculation failed for match {}", match.getId(), e);
                }
            });
    }
}
```

Ensure `@EnableAsync` is present on the main application class or a config class.

### 2m. `PredictionScoringService`
**File:** `backend/.../domain/service/PredictionScoringService.java`

Replace group-level ruleset lookup with group×tournament lookup, and update rule evaluation to use the new boolean `matches()`:

```java
var rulePoints = rulesetRepositoryPort
    .findByGroupIdAndTournamentId(prediction.getGroupId(), match.getTournamentId())
    .map(Ruleset::getRulePoints)
    .orElse(RulesetService.DEFAULT_RULE_POINTS);   // fallback for pre-migration groups

int totalScore = scoringRules.stream()
    .filter(r -> rulePoints.containsKey(r.name()))
    .mapToInt(r -> r.matches(prediction, actualScore, actualWinner)
        ? rulePoints.get(r.name()) : 0)
    .sum();
```

Remove the old `DEFAULT_RULE_POINTS` constant from this class — it now lives in `RulesetService`.

### 2n. New `RulesetService`
**New file:** `backend/.../domain/service/RulesetService.java`

```java
@Service
public class RulesetService {

    public static final Map<String, Integer> DEFAULT_RULE_POINTS = Map.of(
        "CORRECT_WINNER", 1,
        "EXACT_SCORE", 3,
        "CORRECT_GOAL_DIFFERENCE", 2
    );

    // Called by GroupService — no auth check, internal use only
    public void initializeForGroupTournament(UUID groupId, UUID tournamentId) {
        rulesetRepositoryPort.upsertForGroupTournament(groupId, tournamentId, DEFAULT_RULE_POINTS);
    }

    // Requires active group membership
    public RulesetResponse getRuleset(UUID userId, UUID groupId, UUID tournamentId) {
        requireActiveMembership(userId, groupId);
        var ruleset = rulesetRepositoryPort
            .findByGroupIdAndTournamentId(groupId, tournamentId)
            .orElseGet(() -> buildDefaultRuleset());
        return toResponse(ruleset);
    }

    // Requires admin; triggers async recalculation; returns saved state
    public RulesetResponse updateRuleset(UUID adminUserId, UUID groupId, UUID tournamentId,
                                         List<RuleConfigDto> rules) {
        requireAdminMembership(adminUserId, groupId);
        // disabled rules are absent from the map (absent = disabled)
        var rulePoints = rules.stream()
            .filter(RuleConfigDto::isEnabled)
            .collect(Collectors.toMap(RuleConfigDto::getName, RuleConfigDto::getPoints));
        var saved = rulesetRepositoryPort.upsertForGroupTournament(groupId, tournamentId, rulePoints);
        scoreRecalculationService.recalculateForGroupTournament(groupId, tournamentId); // @Async — returns immediately
        return toResponse(saved);
    }
}
```

`requireActiveMembership` / `requireAdminMembership` follow the same pattern as `GroupService` (check membership via `groupMemberRepository`, throw `GroupAccessDeniedException` if not satisfied).

`toResponse` builds the `RulesetResponse` by iterating all known rule names (from injected `List<ScoringRule>`) and for each emitting `{ name, enabled: rulePoints.containsKey(name), points: rulePoints.getOrDefault(name, defaultPoints) }`.

### 2o. `GroupService.addTournamentToGroup()`
**File:** `backend/.../domain/service/GroupService.java`

After `groupTournamentRepository.save(groupId, tournamentId)`, add:
```java
rulesetService.initializeForGroupTournament(groupId, tournamentId);
```

`GroupService` injects `RulesetService`. No need to reference `DEFAULT_RULE_POINTS` directly.

### 2p. New `RulesetController`
**New file:** `backend/.../adapter/rest/controller/RulesetController.java`

```
GET  /api/groups/{groupId}/tournaments/{tournamentId}/ruleset  → RulesetResponse
PUT  /api/groups/{groupId}/tournaments/{tournamentId}/ruleset  → RulesetResponse
```

Authorization follows the same pattern as `GroupController`:
```java
private UUID currentUserId() {
    return UUID.fromString((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
}
```
Pass `currentUserId()` to `rulesetService.getRuleset(...)` / `rulesetService.updateRuleset(...)` — the service enforces membership/admin.

PUT body: `RulesetUpdateRequest`. PUT returns the saved `RulesetResponse` (not void) so the frontend reflects server-canonical state.

### 2q. DTOs
**New files** in `adapter/rest/dto/`:

- **`RuleConfigDto.java`**: `{ String name; boolean enabled; @Min(1) int points }` — shared for both request and response
- **`RulesetResponse.java`**: `{ List<RuleConfigDto> rules }`
- **`RulesetUpdateRequest.java`**: `{ @Valid List<RuleConfigDto> rules }`

A single `RuleConfigDto` is used in both directions. Validation (`@Min(1)`) on `points` applies only when the rule is enabled; disabled rules are filtered out before persisting so zero/negative points in a disabled entry never reach the DB.

---

## Phase 3: Frontend

### 3a. Types & API service
**New file:** `frontend/src/services/rulesetApi.ts`

```typescript
export type RuleConfig = { name: string; enabled: boolean; points: number }
export type RulesetResponse = { rules: RuleConfig[] }
export type RulesetUpdateRequest = { rules: RuleConfig[] }

export async function fetchRuleset(groupId: string, tournamentId: string): Promise<RulesetResponse>
export async function saveRuleset(groupId: string, tournamentId: string, req: RulesetUpdateRequest): Promise<RulesetResponse>
// saveRuleset now returns RulesetResponse (PUT returns saved state, not void)
```

### 3b. Hook: `useTournamentRuleset`
**New file:** `frontend/src/pages/GroupDetailsPage/hooks/useTournamentRuleset.ts`

Follow the existing `useGroupDetails` pattern: plain `useState` + `useEffect` with an `isMounted` guard.

- Fetch is lazy — triggered by a `load()` call (called on first expand), not on mount. Track with `hasFetched` ref.
- Local edit state is a copy of the fetched rules; mutations update local state only until save.
- **Save flow**: call `saveRuleset` (PUT) → on success, replace local state with the returned `RulesetResponse` (server-canonical), show "Rules saved. Leaderboard updating…" message, then after a 3-second `setTimeout` call `onRulesSaved()` to trigger leaderboard refresh. The delay accounts for async recalculation completing.
- Returns: `{ rules, isLoading, isSaving, error, successMessage, load, handleToggle, handlePointsChange, handleSave }`

### 3c. Component: `TournamentRulesetSection`
**New file:** `frontend/src/pages/GroupDetailsPage/components/TournamentRulesetSection.tsx`

Props: `{ groupId, tournamentId, isAdmin, onRulesSaved }`

UI structure:
```
▼ Rules
  ☑ Correct winner          [ 1 ] pts
  ☑ Exact score             [ 3 ] pts
  ☑ Correct goal difference [ 2 ] pts
                            [Save rules]   ← admin only
```

- Checkbox: toggle `enabled` (disabled for non-admin)
- Number input: edit `points` (disabled for non-admin); `min="1"`
- Save button: visible only to admin
- Loading spinner while fetching/saving
- Inline success/error messages (green/red, matching existing pattern)
- Rule display names come from i18n (`t(`ruleset.rules.${rule.name}`)`)

### 3d. Update `GroupTournamentsSection`
**File:** `frontend/src/pages/GroupDetailsPage/components/GroupTournamentsSection.tsx`

- Add local `openRulesetTournamentId: string | null` state (local to this component — does not need to be lifted)
- Add "Rules" button next to "Open"/"Remove" for each tournament row (visible to all members, not just admins)
- Toggle: clicking "Rules" on the open tournament closes it; clicking on a different tournament opens it and closes the previous
- Render `<TournamentRulesetSection>` below the tournament row when `openRulesetTournamentId === tournament.id`

### 3e. Update `useGroupDetails`
**File:** `frontend/src/pages/GroupDetailsPage/hooks/useGroupDetails.ts`

Expose `refreshLeaderboards()` — a function that re-runs the leaderboard fetch (already fetched in the initial `useEffect`; extract into a named function and include in the return type). Pass it as `onRulesSaved` to `TournamentRulesetSection`.

### 3f. i18n keys
Add to `frontend/src/i18n/locales/en.json` (and `et.json`, `ru.json`):
```json
"ruleset": {
  "title": "Rules",
  "saveButton": "Save rules",
  "saveSuccess": "Rules saved. Leaderboard updating…",
  "rules": {
    "CORRECT_WINNER": "Correct winner",
    "EXACT_SCORE": "Exact score",
    "CORRECT_GOAL_DIFFERENCE": "Correct goal difference"
  },
  "points": "pts",
  "readOnly": "Rules are set by the group admin"
}
```

---

## Critical Files Summary

| File | Change |
|------|--------|
| `backend/.../db/changelog/019-configurable-rulesets.yaml` | **NEW** — migration |
| `backend/.../domain/service/scoring/ScoringRule.java` | `evaluate` → `matches` returning boolean |
| `backend/.../domain/service/scoring/ExactScoreRule.java` | update for new interface |
| `backend/.../domain/service/scoring/CorrectWinnerRule.java` | update for new interface |
| `backend/.../domain/service/scoring/CorrectGoalDifferenceRule.java` | **NEW** |
| `backend/.../domain/entity/Ruleset.java` | `Map<String, Integer> rulePoints`; remove `name` |
| `backend/.../domain/entity/Group.java` | remove `rulesetId` |
| `backend/.../adapter/persistence/entity/RulesetEntity.java` | `Map<String,Integer>` collection; remove `name` |
| `backend/.../adapter/persistence/entity/GroupEntity.java` | remove `rulesetId` |
| `backend/.../adapter/persistence/entity/GroupTournamentEntity.java` | add `rulesetId` |
| `backend/.../adapter/persistence/mapper/RulesetMapper.java` | remove `name` mapping |
| `backend/.../adapter/persistence/repository/RulesetJpaRepository.java` | remove `findByName` |
| `backend/.../adapter/persistence/repository/GroupTournamentJpaRepository.java` | add `updateRulesetId` JPQL query |
| `backend/.../adapter/persistence/repository/MatchJpaRepository.java` | add `findByTournamentIdAndStatusIn` |
| `backend/.../domain/port/persistence/RulesetRepositoryPort.java` | replace with 2 new methods only |
| `backend/.../domain/port/persistence/MatchRepositoryPort.java` | add `findFinishedByTournamentId` |
| `backend/.../adapter/persistence/adapter/RulesetRepositoryAdapter.java` | replace with new implementations |
| `backend/.../adapter/persistence/adapter/MatchRepositoryAdapter.java` | implement new port method |
| `backend/.../domain/service/PredictionScoringService.java` | group×tournament lookup; boolean `matches`; remove `DEFAULT_RULE_POINTS` |
| `backend/.../domain/service/GroupService.java` | call `rulesetService.initializeForGroupTournament` on tournament add |
| `backend/.../domain/service/RulesetService.java` | **NEW** — owns `DEFAULT_RULE_POINTS` |
| `backend/.../domain/service/ScoreRecalculationService.java` | **NEW** — `@Async` recalculation |
| `backend/.../adapter/rest/controller/RulesetController.java` | **NEW** |
| `backend/.../adapter/rest/dto/RuleConfigDto.java` | **NEW** — shared request/response DTO |
| `backend/.../adapter/rest/dto/RulesetResponse.java` | **NEW** |
| `backend/.../adapter/rest/dto/RulesetUpdateRequest.java` | **NEW** |
| `frontend/src/services/rulesetApi.ts` | **NEW** |
| `frontend/src/pages/GroupDetailsPage/hooks/useTournamentRuleset.ts` | **NEW** |
| `frontend/src/pages/GroupDetailsPage/components/TournamentRulesetSection.tsx` | **NEW** |
| `frontend/src/pages/GroupDetailsPage/components/GroupTournamentsSection.tsx` | add Rules button + expandable |
| `frontend/src/pages/GroupDetailsPage/hooks/useGroupDetails.ts` | expose `refreshLeaderboards()` |
| `frontend/src/i18n/locales/{en,et,ru}.json` | add `ruleset.*` keys |

---

## Verification

1. Start: `cd local-env && docker compose up --build`
2. Log in as group admin → navigate to `/groups/{groupId}`
3. Add a tournament → verify "Rules" button appears on that tournament row
4. Click "Rules" → expandable shows CORRECT_WINNER(1), EXACT_SCORE(3), CORRECT_GOAL_DIFFERENCE(2)
5. Change EXACT_SCORE points to 5, disable CORRECT_GOAL_DIFFERENCE, click "Save rules" → success message appears immediately; leaderboard refreshes ~3s later
6. Log in as non-admin member → "Rules" button visible, all inputs disabled, no Save button
7. Trigger scoring for a finished match → verify updated points are used (EXACT_SCORE=5, CORRECT_GOAL_DIFFERENCE not awarded)
8. Run: `cd backend && ./mvnw test`
