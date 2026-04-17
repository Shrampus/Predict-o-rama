---
name: FriendGroup Admin View
overview: Implement a school-MVP FriendGroup admin view quickly with the 5 required endpoints, straightforward admin checks, minimal persistence changes, and a simple `/groups/:groupId` page.
todos:
  - id: backend-admin-endpoints
    content: Add the 5 group admin endpoints with straightforward service logic and explicit status behavior
    status: pending
  - id: backend-group-tournament-link
    content: Add minimal `group_tournaments` table and repository access without extra abstraction
    status: pending
  - id: frontend-route-and-page
    content: Add `/groups/:groupId` route and implement a single readable `GroupDetailsPage` first
    status: pending
  - id: frontend-api-and-gating
    content: Extend `groupApi` and add simple admin-only actions with clear loading/empty/error states
    status: pending
  - id: manual-verification
    content: Validate all MVP flows with manual acceptance checklist only
    status: pending
isProject: false
---

# FriendGroup Admin View Plan

## Existing Relevant Structure

- Frontend currently has a single group area at [`frontend/src/pages/GroupPage/GroupsPage.tsx`](frontend/src/pages/GroupPage/GroupsPage.tsx), with group list/create/join behavior via hooks and API calls in [`frontend/src/services/groupApi.ts`](frontend/src/services/groupApi.ts).
- Routing is centralized in [`frontend/src/app/router/AppRouter.tsx`](frontend/src/app/router/AppRouter.tsx) and paths in [`frontend/src/app/routePaths.ts`](frontend/src/app/routePaths.ts), with auth gating through [`frontend/src/components/auth/RequireAuth.tsx`](frontend/src/components/auth/RequireAuth.tsx).
- Backend group APIs live in [`backend/src/main/java/com/predictorama/backend/adapter/rest/controller/GroupController.java`](backend/src/main/java/com/predictorama/backend/adapter/rest/controller/GroupController.java) and business logic in [`backend/src/main/java/com/predictorama/backend/domain/service/GroupService.java`](backend/src/main/java/com/predictorama/backend/domain/service/GroupService.java).
- DTO/mapper separation already exists (e.g. [`backend/src/main/java/com/predictorama/backend/adapter/rest/dto`](backend/src/main/java/com/predictorama/backend/adapter/rest/dto), [`backend/src/main/java/com/predictorama/backend/adapter/rest/mapper`](backend/src/main/java/com/predictorama/backend/adapter/rest/mapper)).
- There is no explicit group-to-tournament persistence relation yet (Liquibase changelogs under [`backend/src/main/resources/db/changelog`](backend/src/main/resources/db/changelog)).

## MVP Implementation Approach

### 1) Backend: implement only required endpoint surface

- Keep exactly this endpoint set:
  - `GET /api/groups/{groupId}` -> minimal group details + caller role only:
    - `{ id, name, description, currentUserRole }`
  - `GET /api/groups/{groupId}/members` -> current members.
  - `POST /api/groups/{groupId}/members` -> add member by email (admin-only).
  - `GET /api/groups/{groupId}/tournaments` -> linked tournaments.
  - `POST /api/groups/{groupId}/tournaments` -> link tournament to group (admin-only).
- Keep service checks literal and local (no new helper abstraction required for MVP):
  - ensure caller is at least a member for read endpoints
  - ensure caller is admin for mutation endpoints
- Keep DTOs explicit but lightweight for this view.
- Define add-member behavior explicitly:
  - admin submits email
  - backend resolves user by email
  - `404` when email does not map to a user
  - `409` when user is already in group
  - `403` when caller is not group admin
- Reuse existing exception handler style for clear `401`/`403`/`404`/`409` behavior.

### 2) Backend persistence: minimal relation only

- Add a minimal join table (`group_tournaments`) with:
  - `group_id`
  - `tournament_id`
  - unique `(group_id, tournament_id)` constraint
- Add Liquibase changelog to create table + foreign keys and include it in master changelog.
- For MVP, avoid creating extra domain layering unless strictly needed by current compilation/pattern constraints.
- Service logic validates tournament existence before linking and prevents duplicates.

### 3) Frontend: single page first, split only if needed

- Add route `'/groups/:groupId'` in [`frontend/src/app/routePaths.ts`](frontend/src/app/routePaths.ts) and wire in [`frontend/src/app/router/AppRouter.tsx`](frontend/src/app/router/AppRouter.tsx).
- Build a readable `GroupDetailsPage.tsx` first that includes:
  - group header
  - member list
  - add-member email form
  - tournament list
  - add-tournament simple input form
- Extract `MembersSection` / `TournamentsSection` only if file size or readability clearly requires it.
- From [`frontend/src/pages/GroupPage/components/MyGroupsList.tsx`](frontend/src/pages/GroupPage/components/MyGroupsList.tsx), add navigation entry to open group details.

### 4) Frontend API + admin gating (MVP)

- Extend [`frontend/src/services/groupApi.ts`](frontend/src/services/groupApi.ts) with explicit typed calls:
  - `getGroupDetails`
  - `getGroupMembers`
  - `addGroupMember`
  - `getGroupTournaments`
  - `addGroupTournament`
- In page container, compute `isAdmin` from group details response; show admin action forms only when true.
- Non-admin UX: still show members/tournaments read sections where permitted; hide or disable admin actions with clear message.
- Use existing simple UI patterns for loading/error/success text and explicit local state (no new framework abstractions).
- Keep add-tournament input simple for MVP:
  - submit `tournamentId` via basic input first
  - only reuse/fetch tournament options if an existing endpoint is already easy to wire

### 5) Security and behavior guarantees

- Enforce backend authorization regardless of frontend state (admin-only endpoints require admin role in group membership).
- Use explicit permission model:
  - members can read group details
  - members can read member list
  - members can read group tournaments
  - only admins can add members/tournaments
- Keep responses explicit and predictable for junior readability; no hidden side effects.

### 6) Validation (manual only for MVP)

- Do not add unit/controller/frontend tests in this pass.
- Validate using manual checklist only:
  - admin sees controls and can add member/tournament
  - non-admin cannot mutate (UI hidden + backend forbidden)
  - loading/empty/error states render clearly

## DTO Naming and API Surface (keep explicit, keep small)

- Keep DTO names explicit and view-oriented (avoid generic names):
  - request DTOs: `AddGroupMemberRequest`, `AddGroupTournamentRequest`
  - response DTOs: `GroupDetailsResponse`, `GroupMemberResponse`, `GroupTournamentResponse`
- Keep endpoint surface intentionally small:
  - `GET /api/groups/{groupId}`
  - `GET /api/groups/{groupId}/members`
  - `POST /api/groups/{groupId}/members`
  - `GET /api/groups/{groupId}/tournaments`
  - `POST /api/groups/{groupId}/tournaments`
- Tournament options endpoint is optional for this MVP (only if trivially available).

## Manual Acceptance Checklist

- Admin can open `/groups/:groupId`.
- Admin sees current member list.
- Admin can add an existing user by email.
- Duplicate member add shows a clear error.
- Admin sees linked tournament list.
- Admin can add a tournament from dropdown options.
- Non-admin can read allowed sections.
- Non-admin does not see admin actions.
- Backend rejects non-admin mutation attempts.

## Focused Notes (stay scoped)

- Tournament page currently uses a hardcoded group id path in existing flow; this MVP plan does not refactor broad tournament behavior.
- Priority is fast, readable, correct implementation over architectural completeness.