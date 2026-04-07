# #26 Frontend Group Service Views

## Overview

Frontend pages for creating and joining prediction groups, calling the backend `GroupController` endpoints (`POST /api/groups` and `POST /api/groups/join`).

---

## Backend Endpoints Used

| Endpoint | Method | Request Body | Response |
|---|---|---|---|
| `/api/groups` | POST | `{ ownerId, name, description }` | `GroupResponse` — includes `inviteCode` |
| `/api/groups/join` | POST | `{ userId, inviteCode }` | `GroupMemberResponse` — includes role, status |

---

## Frontend Architecture

Follows the [Frontend Rule Guide](../../frontend/rules/FRONTEND_RULE_GUIDE.md) strictly. Data flows one direction: **Service → Hook → Component**.

### File Structure

```
frontend/src/
├── services/
│   └── groupApi.ts                              # HTTP calls + request/response types
├── pages/
│   ├── CreateGroupPage/
│   │   ├── CreateGroupPage.tsx                  # Thin page — UI only
│   │   └── hooks/
│   │       └── useGroups.ts                # State, form logic, calls service
│   └── JoinGroupPage/
│       ├── JoinGroupPage.tsx                    # Thin page — UI only
│       └── hooks/
│           └── useJoinGroup.ts                  # State, form logic, calls service
├── app/
│   ├── routePaths.ts                            # Added createGroup, joinGroup paths
│   └── router/
│       └── AppRouter.tsx                        # Registered new routes
└── components/
    └── layout/
        └── MainLayout.tsx                       # Added nav links
```

### Layer Responsibilities

| Layer | File | Responsibility |
|---|---|---|
| **Service** | `groupApi.ts` | `fetch()` calls, request/response TypeScript interfaces, error handling. No React imports. |
| **Hook** | `useGroups.ts`, `useJoinGroup.ts` | `useState` for form data, loading, errors, result. Calls service on submit. Returns data + handlers to component. |
| **Component** | `CreateGroupPage.tsx`, `JoinGroupPage.tsx` | Renders form UI from hook data. No fetch, no service imports, no business logic. |

---

## Service Layer — `groupApi.ts`

Defines TypeScript interfaces matching the backend DTOs:

- `CreateGroupRequest` — `{ ownerId: string, name: string, description: string }`
- `GroupResponse` — `{ id, ownerId, inviteCode, name, description }`
- `JoinGroupRequest` — `{ userId: string, inviteCode: string }`
- `GroupMemberResponse` — `{ id, groupId, userId, memberRole, status }`

Two exported async functions:
- `createGroup(request)` → POST `/api/groups` → returns `GroupResponse`
- `joinGroup(request)` → POST `/api/groups/join` → returns `GroupMemberResponse`

Uses relative `/api/` paths — Vite proxy forwards to `http://localhost:8080` in dev.

---

## Routing

Routes added to `routePaths.ts`:
- `/groups/create` → `CreateGroupPage`
- `/groups/join` → `JoinGroupPage`

Both registered inside `<MainLayout>` in `AppRouter.tsx`, so they share the same header/nav.

Nav links added to `MainLayout.tsx` for "Create Group" and "Join Group".

---

## Page Behavior

### Create Group Page (`/groups/create`)

1. User fills in: Owner ID (UUID), Group Name, Description
2. On submit → hook calls `createGroup()` service
3. **On success** → Shows confirmation screen with the **invite code** (so user can share it)
4. **On error** → Shows error banner above the form

### Join Group Page (`/groups/join`)

1. User fills in: User ID (UUID), Invite Code (UUID)
2. On submit → hook calls `joinGroup()` service
3. **On success** → Shows confirmation with group ID, role, and status
4. **On error** → Shows error banner (e.g., "Invite code not found")

---

## Key Design Decisions

| Decision | Reason |
|---|---|
| Relative fetch URLs (`/api/groups`) | Vite proxy handles dev routing; no CORS issues; works in production |
| Show invite code on success instead of navigating away | The invite code is the only way to share the group — losing it would be a UX problem |
| Manual UUID input for ownerId/userId | No authentication exists yet — this is a dev-time placeholder |
| No shared form component abstraction | Only two forms with different fields — extracting a shared form would be premature |

---

## Rule Guide Compliance

| Rule | How It's Followed |
|---|---|
| 1 — Project Structure | Each page in own folder with `hooks/` subfolder |
| 2 — Naming | PascalCase components, `use` prefix hooks, `isLoading` boolean, kebab-case routes |
| 3 — Components | Pages are UI-only, no fetch or service imports |
| 4 — Hooks | All state and async logic in custom hooks |
| 5 — Data Flow | Service → Hook → Component, one direction |
| 6 — Services | All HTTP in `groupApi.ts`, async/await, typed, no React |
| 7 — Styling | Tailwind utility classes only |
| 8 — Clean Code | Explicit types, descriptive names, small focused functions |
| 9 — AI Rules | Minimal changes, reuses existing patterns, no new dependencies |

---

## Testing Locally

**Docker:**
```bash
cd local-env && docker compose up --build
# Open http://localhost/groups/create
```

**Manual:**
```bash
# Terminal 1 (needs local Postgres)
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Terminal 2
cd frontend && npm run dev
# Open http://localhost:5173/groups/create
```

Flow: Create group → copy invite code → go to Join Group → paste invite code → verify success.
