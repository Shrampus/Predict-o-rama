# Plan: Temporary Simple Login (Issue #13)

## Context & Goal

The app currently has no authentication. Every page that requires a user assumes a logged-in session, and workarounds like manually pasting UUIDs into form fields are in place. This makes UI development and multi-user testing extremely painful.

The long-term plan is Google OAuth, but that is not yet the priority. This plan implements a **temporary, dev-grade login system** using email + bcrypt password. It is intentionally minimal — no Spring Security, no JWT, no token refresh — just enough to:

- Allow real login with multiple named test users
- Provide a `currentUser` (with a real UUID) to every frontend page
- Remove all manual UUID input workarounds
- Support different roles and user scenarios (e.g. self-referential checks, group ownership)

This solution will coexist with Google OAuth when that is added; the `users` table already exists and the password column can simply remain unused for OAuth users.

---

## Decisions

### No Spring Security
Spring Security adds significant configuration complexity for a temporary dev feature. Instead, `HttpSession` is used directly — Spring Boot ships session support out of the box with no extra config. The session holds the authenticated `userId`; a `SessionService` retrieves the current user from the session. When real auth is added, this layer is replaced, not refactored.

### BCrypt over plaintext
Even for dev/test passwords, storing plaintext in a
migration file is bad practice. BCrypt hashing is used via the `spring-security-crypto` library. This is **not** a transitive dependency of the current stack — it must be added explicitly to `pom.xml` as a standalone dependency (without pulling in full Spring Security).

### HttpSession over JWT
JWT requires a token library, signing keys, and frontend token storage logic. `HttpSession` is a single cookie managed by the browser automatically. For a local dev tool this is the right tradeoff. The session cookie (`JSESSIONID`) is HttpOnly by default.

### Seed data in Liquibase
Test users are seeded via a dedicated Liquibase changelog rather than a separate script. This means any developer with a fresh DB gets the test users automatically — no manual setup step. Note: `created_at`/`updated_at` will use the Liquibase execution time, so timestamps shift on every fresh DB setup — acceptable for dev seed data.

### AuthContext in React (in-memory, session cookie)
The frontend stores the logged-in user in React context state. On mount, the context calls `GET /api/auth/me` — if the session cookie is still valid, it hydrates the user state silently. This means the page survives a refresh without re-login. No JWT, no localStorage — just the session cookie doing the work.

### Route guard at the router level
All routes except `/login` are wrapped in a single `<RequireAuth>` component. If `currentUser` is null and loading is complete, it redirects to `/login`. This is a single change point; individual pages have no auth awareness.

### PasswordVerifier port (hexagonal compliance)
`AuthService` lives in the domain layer, which must have no Spring dependencies. Rather than importing Spring's `PasswordEncoder` into the domain, we define a `PasswordVerifier` port interface in `domain/port/` and adapt `BCryptPasswordEncoder` behind it in the adapter layer. This keeps the domain clean for a small cost.

### Removing manual UUID fields
Once `currentUser.id` is available globally, the `ownerId` / `userId` inputs in `CreateGroupPage` and `JoinGroupPage` should be replaced with the value from context. These pages are currently unmerged WIP on a feature branch — this cleanup applies when those pages are present and merged.

---

## Scope

This plan covers:

**Backend**
- Maven dependency: add `spring-security-crypto`
- Liquibase migration: add `password_hash` column to `users` table
- Liquibase migration: seed 5 test users with bcrypt hashed passwords
- `PasswordVerifier` port + `BCryptPasswordVerifier` adapter
- `AuthService`: login (verify password) + getById
- `AuthController`: `POST /api/auth/login`, `GET /api/auth/me`, `POST /api/auth/logout`
- `SessionService`: thin wrapper to read/write `userId` on `HttpSession`
- Exception classes: `InvalidCredentialsException` (401), `UserNotFoundException` (404)
- CORS config update: `allowCredentials(true)` for session cookies
- Wire new beans in `DomainConfig`
- `AuthController` tests: happy path login, bad password, /me with/without session

**Frontend**
- `authApi.ts`: API client for login/me/logout
- `AuthContext.tsx`: React context — holds `currentUser`, `login()`, `logout()`, loading state
- `LoginPage/`: simple email + password form with test credentials displayed below
- `RequireAuth.tsx`: route guard component
- Update `AppRouter.tsx`: wrap existing routes with `<RequireAuth>`, add `/login` route
- Update `MainLayout`: show current username and a logout button

**Deferred (applies when group pages are merged)**
- Update `CreateGroupPage`: remove manual `ownerId` input, use `currentUser.id`
- Update `JoinGroupPage`: remove manual `userId` input, use `currentUser.id`

**Not in scope**
- Registration UI (users are seeded only; `POST /api/users` remains for programmatic use)
- Role-based access control on backend endpoints
- Google OAuth integration
- Password reset / change
- Any production hardening (rate limiting, CSRF, etc.)

---

## Backend Implementation

### Step 1 — Maven dependency

Add to `backend/pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>
```

This pulls in only the crypto utilities (BCrypt, etc.) without Spring Security's web/filter infrastructure. Version is managed by the Spring Boot BOM.

### Step 2 — Liquibase: add password_hash column

File: `backend/src/main/resources/db/changelog/006-add-password-hash-to-users.yaml`

```yaml
databaseChangeLog:
  - changeSet:
      id: 006-add-password-hash-to-users
      author: dev
      changes:
        - addColumn:
            tableName: users
            columns:
              - column:
                  name: password_hash
                  type: varchar(255)
                  constraints:
                    nullable: true   # nullable so existing/OAuth users without a password are valid
```

Nullable because future Google OAuth users will have no password — only seeded dev users need it.

### Step 3 — Liquibase: seed test users

File: `backend/src/main/resources/db/changelog/007-seed-test-users.yaml`

Five users, covering all useful testing scenarios:

| Username | Email | Password | Role | Purpose |
|----------|-------|----------|------|---------|
| alice | alice@test.com | password123 | ADMIN | Admin user |
| bob | bob@test.com | password123 | USER | Group owner |
| carol | carol@test.com | password123 | USER | Group member |
| dave | dave@test.com | password123 | USER | Second group member (self-referential tests) |
| eve | eve@test.com | password123 | USER | User outside groups (access control tests) |

BCrypt hashes for `password123` are pre-computed and hardcoded in the migration. All UUIDs are fixed (hardcoded) so test data referencing them remains stable across DB resets.

```yaml
databaseChangeLog:
  - changeSet:
      id: 007-seed-test-users
      author: dev
      context: dev
      changes:
        - insert:
            tableName: users
            columns:
              - column: { name: id, value: '00000000-0000-0000-0000-000000000001' }
              - column: { name: username, value: alice }
              - column: { name: email, value: alice@test.com }
              - column: { name: system_role, value: ADMIN }
              - column: { name: password_hash, value: '$2a$10$...' }  # bcrypt of password123
              - column: { name: created_at, valueDate: 'now()' }
              - column: { name: updated_at, valueDate: 'now()' }
        # ... repeat for bob, carol, dave, eve
```

Include both `006` and `007` in `db.changelog-master.yaml`.

### Step 4 — PasswordVerifier port + adapter

The domain layer must stay free of Spring dependencies. `AuthService` needs to verify passwords, so we define a port.

Port — `backend/.../domain/port/PasswordVerifier.java`:
```java
public interface PasswordVerifier {
    boolean matches(String rawPassword, String encodedPassword);
}
```

Adapter — `backend/.../adapter/persistence/adapter/BCryptPasswordVerifier.java`:
```java
@Component
public class BCryptPasswordVerifier implements PasswordVerifier {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
```

### Step 5 — Exception classes

File: `backend/.../domain/exception/InvalidCredentialsException.java`
```java
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}
```

File: `backend/.../domain/exception/UserNotFoundException.java`
```java
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UUID id) {
        super("User not found: " + id);
    }
}
```

These are mapped to HTTP status codes in the controller layer via `@ExceptionHandler` or a `@RestControllerAdvice` class that returns 401 for `InvalidCredentialsException` and 404 for `UserNotFoundException`.

File: `backend/.../adapter/rest/AuthExceptionHandler.java`
```java
@RestControllerAdvice
public class AuthExceptionHandler {
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Void> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Void> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
```

### Step 6 — SessionService

File: `backend/.../adapter/rest/SessionService.java`

```java
@Component
public class SessionService {
    private static final String USER_ID_KEY = "userId";

    public void setUserId(HttpSession session, UUID userId) {
        session.setAttribute(USER_ID_KEY, userId);
    }

    public Optional<UUID> getUserId(HttpSession session) {
        return Optional.ofNullable((UUID) session.getAttribute(USER_ID_KEY));
    }

    public void invalidate(HttpSession session) {
        session.invalidate();
    }
}
```

This is infrastructure (depends on `jakarta.servlet.http.HttpSession`), so it lives in the adapter layer at `adapter/rest/`, not in `domain/service/`.

### Step 7 — AuthService

File: `backend/.../domain/service/AuthService.java`

```java
public class AuthService {
    private final UserRepositoryPort userRepository;
    private final PasswordVerifier passwordVerifier;

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(InvalidCredentialsException::new);
        if (!passwordVerifier.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return user;
    }

    public User getById(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    }
}
```

Uses the `PasswordVerifier` port — no Spring imports in the domain layer. The error message is deliberately vague ("Invalid credentials") for both bad email and bad password.

### Step 8 — AuthController

File: `backend/.../adapter/rest/controller/AuthController.java`

Three endpoints:

**POST /api/auth/login**
```
Request:  { "email": "alice@test.com", "password": "password123" }
Response: { "id": "...", "username": "alice", "email": "alice@test.com", "systemRole": "ADMIN" }
```
Calls `AuthService.login()`. On success, stores `userId` in session via `SessionService`. Returns `UserResponse`.

**GET /api/auth/me**
```
Response: { "id": "...", "username": "alice", "email": "alice@test.com", "systemRole": "ADMIN" }
         or 401 if no session
```
Reads `userId` from session, looks up user via `AuthService.getById()`. Returns `UserResponse` or 401.

**POST /api/auth/logout**
```
Response: 204 No Content
```
Calls `SessionService.invalidate()`.

DTO — `backend/.../adapter/rest/dto/LoginRequest.java`:
```java
public record LoginRequest(String email, String password) {}
```

### Step 9 — CORS config update

Update `CorsConfig.java` to allow credentials — required for session cookies to be sent cross-origin (relevant when running frontend and backend on different ports without the Vite proxy, e.g. Docker with nginx):

```java
configuration.setAllowCredentials(true);
```

Note: `allowCredentials(true)` cannot be combined with `allowedOrigins("*")` — must use explicit origins. The current config already reads from `ALLOWED_ORIGIN` env var with a default, so this should work.

### Step 10 — Wire in DomainConfig

Add `AuthService` bean to `DomainConfig.java`:

```java
@Bean
public AuthService authService(UserRepositoryPort userRepository, PasswordVerifier passwordVerifier) {
    return new AuthService(userRepository, passwordVerifier);
}
```

`BCryptPasswordVerifier` is `@Component`-scanned automatically — no explicit bean needed.

### Step 11 — Update User domain entity and persistence layer

- Add `passwordHash` field (String, nullable) to `User.java` domain entity
- Add `passwordHash` column to `UserEntity.java` JPA entity
- Update `UserMapper.java` to map `passwordHash` in both `toDomain` and `toEntity`
- `UserRepositoryPort.findByEmail()` already exists — no change needed

### Step 12 — Tests

File: `backend/.../adapter/rest/controller/AuthControllerTest.java`

Minimum test cases:
- `POST /api/auth/login` with valid credentials returns 200 + user JSON
- `POST /api/auth/login` with wrong password returns 401
- `POST /api/auth/login` with nonexistent email returns 401
- `GET /api/auth/me` with valid session returns 200 + user JSON
- `GET /api/auth/me` without session returns 401
- `POST /api/auth/logout` invalidates session

---

## Frontend Implementation

### Step 1 — authApi.ts

File: `frontend/src/services/authApi.ts`

```typescript
const BASE = '/api/auth';

export interface CurrentUser {
  id: string;
  username: string;
  email: string;
  systemRole: 'ADMIN' | 'USER';
}

export const authApi = {
  login: (email: string, password: string): Promise<CurrentUser> =>
    fetch(`${BASE}/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    }).then(res => {
      if (!res.ok) throw new Error('Invalid credentials');
      return res.json();
    }),

  me: (): Promise<CurrentUser | null> =>
    fetch(`${BASE}/me`).then(res =>
      res.ok ? res.json() : null
    ),

  logout: (): Promise<void> =>
    fetch(`${BASE}/logout`, { method: 'POST' }).then(() => {}),
};
```

Note on `credentials`: In local dev, Vite proxies `/api` to `localhost:8080` making it same-origin — `credentials: 'include'` is not needed. If running cross-origin (Docker/nginx), add `credentials: 'include'` to all calls and ensure the CORS config has `allowCredentials(true)` (see backend step 9).

### Step 2 — AuthContext.tsx

File: `frontend/src/context/AuthContext.tsx`

```typescript
interface AuthContextValue {
  currentUser: CurrentUser | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
}
```

On mount, calls `authApi.me()` to restore session. `loading` is `true` until this resolves — prevents a flash-redirect to `/login` on page refresh when the session is valid.

Wrap in `main.tsx`: `<AuthProvider>` goes inside `<BrowserRouter>` (since login redirect uses `useNavigate`).

### Step 3 — RequireAuth.tsx

File: `frontend/src/components/auth/RequireAuth.tsx`

```typescript
export function RequireAuth({ children }: { children: ReactNode }) {
  const { currentUser, loading } = useAuth();
  if (loading) return null; // or a spinner
  if (!currentUser) return <Navigate to="/login" replace />;
  return <>{children}</>;
}
```

### Step 4 — LoginPage

File: `frontend/src/pages/LoginPage/LoginPage.tsx`

Simple form with email + password inputs. On submit calls `login()` from `useAuth()`, then navigates to `/`. Shows an error message on 401. No registration link — accounts are seeded only.

**Dev convenience:** Display the test credentials table below the form so developers don't need to look them up elsewhere.

### Step 5 — AppRouter.tsx

Add `/login` to `routePaths.ts` and update `AppRouter.tsx`:

```typescript
<Routes>
  <Route path={ROUTE_PATHS.login} element={<LoginPage />} />
  <Route element={<RequireAuth><MainLayout /></RequireAuth>}>
    <Route path={ROUTE_PATHS.home} element={<HomePage />} />
    <Route path={ROUTE_PATHS.predictions} element={<PredictionsPage />} />
    <Route path={ROUTE_PATHS.tournaments} element={<TournamentPage />} />
  </Route>
</Routes>
```

### Step 6 — MainLayout

Add to the header: username display and a logout button that calls `logout()` from `useAuth()` and navigates to `/login`.

---

## File Checklist

### New files
- `backend/.../db/changelog/006-add-password-hash-to-users.yaml`
- `backend/.../db/changelog/007-seed-test-users.yaml`
- `backend/.../domain/port/PasswordVerifier.java`
- `backend/.../adapter/persistence/adapter/BCryptPasswordVerifier.java`
- `backend/.../domain/exception/InvalidCredentialsException.java`
- `backend/.../domain/exception/UserNotFoundException.java`
- `backend/.../adapter/rest/AuthExceptionHandler.java`
- `backend/.../adapter/rest/SessionService.java`
- `backend/.../domain/service/AuthService.java`
- `backend/.../adapter/rest/controller/AuthController.java`
- `backend/.../adapter/rest/dto/LoginRequest.java`
- `backend/.../adapter/rest/controller/AuthControllerTest.java`
- `frontend/src/services/authApi.ts`
- `frontend/src/context/AuthContext.tsx`
- `frontend/src/components/auth/RequireAuth.tsx`
- `frontend/src/pages/LoginPage/LoginPage.tsx`

### Modified files
- `backend/pom.xml` — add `spring-security-crypto` dependency
- `backend/.../db/changelog/db.changelog-master.yaml` — include 006, 007
- `backend/.../domain/entity/User.java` — add `passwordHash` field
- `backend/.../adapter/persistence/entity/UserEntity.java` — add `passwordHash` column
- `backend/.../adapter/persistence/mapper/UserMapper.java` — map passwordHash
- `backend/.../config/DomainConfig.java` — wire AuthService bean
- `backend/.../config/CorsConfig.java` — add allowCredentials(true)
- `frontend/src/main.tsx` — wrap with `<AuthProvider>`
- `frontend/src/app/routePaths.ts` — add `login` path
- `frontend/src/app/router/AppRouter.tsx` — add login route + RequireAuth wrapper
- `frontend/src/components/layout/MainLayout.tsx` — add user display + logout

### Deferred (when group pages are merged)
- `frontend/src/pages/CreateGroupPage/CreateGroupPage.tsx` — remove ownerId input
- `frontend/src/pages/JoinGroupPage/JoinGroupPage.tsx` — remove userId input

---

## Test Credentials

| Email | Password | Role | UUID |
|-------|----------|------|------|
| alice@test.com | password123 | ADMIN | 00000000-0000-0000-0000-000000000001 |
| bob@test.com | password123 | USER | 00000000-0000-0000-0000-000000000002 |
| carol@test.com | password123 | USER | 00000000-0000-0000-0000-000000000003 |
| dave@test.com | password123 | USER | 00000000-0000-0000-0000-000000000004 |
| eve@test.com | password123 | USER | 00000000-0000-0000-0000-000000000005 |

---

## Migration Path to Google OAuth

When Google OAuth is added:
1. Add `google_id` column to users table
2. Add a new login flow that upserts a user by `google_id` and calls `SessionService.setUserId()`
3. `AuthController`, `AuthService`, `SessionService` remain unchanged
4. Seeded dev users continue to work via the password login endpoint
5. Eventually the password login endpoint can be disabled in production via a feature flag or Spring profile

The session-based approach means the frontend has zero changes when the auth backend changes — `GET /api/auth/me` always returns the same shape.
