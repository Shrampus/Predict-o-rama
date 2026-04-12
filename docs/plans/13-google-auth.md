# Google Authentication

---

# Part 1: Study Guide

## Key Terms & Concepts

### Authentication vs. Authorization
Two words that are often confused:
- **Authentication** — proving who you are. "I am John, here is my proof."
- **Authorization** — deciding what you are allowed to do. "John is allowed to edit this group, but not delete it."

Authentication always comes first. You cannot authorize someone whose identity you don't know.

### OAuth2
**OAuth2** (Open Authorization 2.0) is an open standard protocol that lets a user grant a third-party application limited access to their account on another service — without giving that app their password.

In plain terms: instead of trusting your app with a password, the user proves their identity to Google (a service they already trust), and Google tells your app "yes, this person is who they say they are."

OAuth2 defines several "flows" (sequences of steps). For a Single Page Application + REST API, the relevant one is the **Authorization Code flow with PKCE**, though libraries like `@react-oauth/google` abstract all of this away.

### OpenID Connect (OIDC)
**OpenID Connect** is a thin identity layer built on top of OAuth2. OAuth2 only handles authorization ("can this app access this resource?"). OIDC adds the concept of identity — it defines how to get a verified user profile.

When you use "Sign in with Google", you are technically using OIDC, not raw OAuth2. Google's OIDC implementation returns an **ID Token** that contains who the user is.

### JWT (JSON Web Token)
A **JWT** is a compact, self-contained string for securely transmitting information. It looks like:

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyLTEyMyJ9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV
```

It has three parts separated by dots: `header.payload.signature`

- **Header** — metadata about the token (algorithm used)
- **Payload** — the actual data (called "claims"), e.g. userId, expiry time
- **Signature** — a cryptographic hash that proves the token hasn't been tampered with

Anyone can *read* a JWT (the payload is just base64-encoded). The signature is what makes it trustworthy — only someone with the secret key can produce a valid signature. So when your backend sees a JWT signed with its own secret, it knows it issued that token and the claims inside are trustworthy.

### ID Token
An **ID Token** is a JWT issued by an identity provider (like Google) that contains verified information about the user — their name, email, and a stable unique identifier. It is proof that the user authenticated with Google.

The key claim inside is `sub` ("subject") — an opaque string like `"109876543210987654321"` that uniquely and permanently identifies the Google account. This is what we store as `googleId`.

### Access Token vs. ID Token
These are often confused:
- **ID Token** — "who is this person?" Contains user identity claims. Used for authentication.
- **Access Token** — "what can this person access?" Used to call APIs (e.g. Google Drive API). Not relevant to our use case.

In our flow, we only care about the ID Token.

### HMAC-SHA256
The algorithm used to sign our own JWTs. **HMAC** (Hash-based Message Authentication Code) produces a signature by combining the token data with a secret key using the **SHA-256** hash function. Without the secret key, you cannot produce a valid signature — so the backend can trust any JWT whose signature verifies correctly.

### Stateless vs. Stateful sessions

- **Stateful** — the server stores session data (in memory or Redis). The client gets a session ID (usually a cookie). Every request, the server looks up that ID. Problem: all servers must share the session store.
- **Stateless** — the server stores nothing. The client holds the token (JWT), which contains all the information the server needs. Every request, the server validates the token's signature. Scales horizontally without coordination.

We use stateless JWTs because we have a REST API consumed by a SPA.

### Spring Security
The standard security framework for Spring Boot applications. It works as a chain of **filters** that intercept every HTTP request before it reaches your controllers. You configure which endpoints are public, which require authentication, and what authentication mechanism to use. We add a custom `JwtAuthFilter` into this chain.

### SecurityContext
Spring Security's per-request container for the current user's identity. Once our `JwtAuthFilter` validates a JWT and extracts the userId, it places that identity into the `SecurityContext`. Any controller can then call `SecurityContextHolder.getContext().getAuthentication()` to know who is making the request.

### CORS (Cross-Origin Resource Sharing)
A browser security mechanism that blocks JavaScript from making requests to a different domain than the page it came from. Your frontend at `localhost:5173` talking to your backend at `localhost:8080` is a cross-origin request. Spring Security has its own CORS handling that needs to be configured — it must cooperate with (not replace) the existing `CorsConfig.java`.

### Environment Variable
A value set outside the application code, in the operating system or container environment. Used to separate configuration (especially secrets) from code. Never hardcode secrets in source files — they end up in git history.

### GitHub Secrets
An encrypted store for sensitive values in a GitHub repository. Values added here are injected as environment variables during GitHub Actions runs (CI/CD). They are never exposed in logs or to other users. This is where `JWT_SECRET` and `GOOGLE_CLIENT_ID` live for production deployments.

---

## What problem are we solving?

Right now, your API has no concept of identity. Anyone can call `POST /api/groups` and create a group.
There is no way to know *who* is making a request, and no way to restrict access.

Adding authentication means the app can answer two questions:
- **Authentication** — "Who are you?" (identity)
- **Authorization** — "Are you allowed to do this?" (permissions)

This plan covers authentication. Authorization (role-based access control) is a separate concern built on top.

---

## The mental model: 3 separate problems

When people say "add login", they usually mean three distinct things:

```
1. IDENTITY VERIFICATION  — Prove who you are
2. SESSION MANAGEMENT     — Remember that you proved it
3. USER LIFECYCLE         — What happens in our database
```

Each is solved independently.

---

## Problem 1: Identity Verification — "Who are you?"

### Option A: Build it yourself (email + password)
- Store a hashed password in your database
- User sends email + password
- You hash the incoming password and compare it
- Problem: you are responsible for security — hashing, salting, breach response, reset flows

### Option B: Delegate to Google (OAuth2)
- "Hey Google, has this person proved to you that they own this Google account?"
- Google says yes/no and tells you their email and a stable ID
- You never see or store a password
- This is what we're building

### How OAuth2 "Sign in with Google" actually works

```
[User]      clicks "Sign in with Google"
   ↓
[Frontend]  opens a Google popup (via @react-oauth/google library)
   ↓
[Google]    asks user: "Allow Predict-o-rama to see your name and email?"
   ↓
[User]      clicks Allow
   ↓
[Google]    issues a signed ID Token (a JWT) back to the frontend
   ↓
[Frontend]  sends this token to your backend: POST /api/auth/google
   ↓
[Backend]   validates the token — "Is this really from Google? Is it expired?"
   ↓
[Backend]   now knows: { googleId, email, name } of the user
```

### What is an ID Token?

It is a JWT (JSON Web Token) — a base64-encoded string in three parts:

```
header.payload.signature
```

The payload contains claims like:
```json
{
  "sub": "109876543210987654321",   ← this is the googleId, stable forever
  "email": "john@gmail.com",
  "name": "John Doe",
  "exp": 1234567890,               ← expiry timestamp
  "iss": "https://accounts.google.com"
}
```

The signature proves Google signed it. Your backend verifies this signature against
Google's public keys. If valid, you trust the claims.

### What about the Google Client ID?

When you register your app in Google Cloud Console, Google gives you a **Client ID**.
This is like a name tag for your app — it tells Google "this OAuth request is coming from
Predict-o-rama, not some other app."

- The Client ID is **not a secret** — it ends up in your frontend JavaScript, visible to anyone
- The security comes from Google validating the entire OAuth flow, not from hiding the Client ID
- You do NOT need a Client Secret for this flow (that's only for server-side OAuth flows)

### Do users need to be added to Google Cloud?

No. Any Google account in the world can log into your app. Google Cloud Console is only
for you as a developer — to register your app and get a Client ID. Your users just click
"Sign in with Google" with their own accounts, exactly like any other website.

---

## Problem 2: Session Management — "Remember that you proved it"

### Why does this exist?

HTTP is stateless. Every request is independent. After the user logs in once,
the next request has no memory of that. You need a mechanism to carry identity across requests.

### Option A: Server-side sessions

```
Login → server stores session in memory/Redis → gives client a session ID (cookie)
Each request → client sends cookie → server looks up session → finds user
```

- Works well for traditional server-rendered apps
- Requires shared session storage (problem when scaling to multiple servers)
- Not ideal for REST APIs consumed by SPAs

### Option B: JWT tokens (what we're building)

```
Login → server issues a signed JWT → client stores it (localStorage)
Each request → client sends JWT in Authorization header → server validates signature
```

- Stateless — server doesn't store anything
- Scales horizontally with zero coordination
- The token IS the session

### Our JWT flow

```
[Backend]   validates Google's token → finds/creates User
[Backend]   issues its own JWT:
            {
              "sub": "user-uuid-here",
              "needsOnboarding": true/false,
              "exp": <7 days from now>
            }
            signed with JWT_SECRET (a random secret only your server knows)
[Frontend]  stores JWT in localStorage
[Frontend]  sends on every request: Authorization: Bearer <jwt>
[Backend]   on each protected request:
              - reads the header
              - verifies the signature with JWT_SECRET
              - extracts userId from "sub" claim
              - knows who is making the request
```

### Why issue our own JWT instead of using Google's?

Google's ID token expires in 1 hour and is meant for one-time identity verification.
Your JWT expires when you decide (7 days) and contains exactly what you need.
It is validated locally with your secret — no network call to Google on every request.

### What goes in GitHub Secrets?

| Variable | Secret? | Reason |
|---|---|---|
| `GOOGLE_CLIENT_ID` | No (but use env var) | Ends up in frontend JS — visible to anyone. Use env var so dev/prod differ. |
| `JWT_SECRET` | **Yes** | Anyone with this can forge login tokens. Never commit. |

A leaked `JWT_SECRET` means an attacker can create a valid JWT for any userId and impersonate
any user. Treat it like a root password.

---

## Problem 3: User Lifecycle — "What happens in the database"

### First login (new user)

```
Google token arrives → extract googleId + email
findByGoogleId(googleId) → not found
Create new User { googleId, email, username: null }
Return JWT with needsOnboarding: true
Frontend redirects to onboarding screen
User picks a username → POST /api/auth/complete-profile
Username saved, return new JWT with needsOnboarding: false
```

### Returning login (existing user)

```
Google token arrives → extract googleId
findByGoogleId(googleId) → found
Return JWT with needsOnboarding: false
Frontend redirects to home
```

### Why googleId and not email?

Users can change their Google email. The `sub` claim (which we call googleId) is an
opaque numeric string that Google guarantees will **never change** for a given account.
Always use googleId as the stable identifier.

---

## Key questions to answer before building

These are the design decisions that shape the entire implementation:

| Question | Why it matters | Decision |
|---|---|---|
| Google only, or multiple providers? | Affects data model — do we need a `provider` field? | Google only for now |
| What to do with `username`? | Currently required; Google doesn't give us one | Onboarding screen after first login |
| Which endpoints are protected? | Shapes security config | Most /api/*, some are public |
| How long should sessions last? | JWT expiry | 7 days |

---

## Components overview

### Google Cloud Console (one-time setup)
Where you register your app with Google. You get a Client ID.
Users never interact with this — it's purely developer infrastructure.

### @react-oauth/google (frontend library)
Renders the "Sign in with Google" button. Handles the popup and the OAuth dance with Google,
and gives you back the ID token. You don't implement any of that flow yourself.

### Google ID Token validator (backend)
Takes a Google ID token string and verifies it is genuine, not expired, and issued for
your app's Client ID. We use the `google-api-client` Java library for this.

### JwtService (backend)
Issues and validates your app's own JWTs using HMAC-SHA256 signing with a secret key.
This is a stateless replacement for sessions.

### JwtAuthFilter (backend)
A Spring Security filter that runs on every request. Reads the Authorization header,
validates the JWT, and sets the user identity in Spring's SecurityContext.
After this filter runs, any controller can call `SecurityContextHolder` to get the current user.

### SecurityConfig (backend)
Defines which endpoints are public and which require authentication.
Also configures Spring Security to be stateless (no server-side sessions).

### AuthService (backend domain)
The business logic: validate Google token → find or create user → return result.
Lives in the domain layer, following the existing hexagonal architecture.

### AuthContext (frontend)
A React context that holds the JWT and current user. Provides `login()` and `logout()`.
Components anywhere in the tree can read auth state without prop drilling.

### ProtectedRoute (frontend)
A wrapper component for routes that require login. If the user is not authenticated,
redirects to `/login`. Otherwise renders the child component.

### apiClient (frontend)
A wrapper around fetch. Automatically injects the `Authorization: Bearer <jwt>` header
on every outgoing request. Handles 401 responses by logging the user out.

---

## Where each piece lives in the hexagonal architecture

```
domain/
  entity/
    User.java                      ← add googleId field, make username nullable
  port/
    UserRepositoryPort.java        ← add findByGoogleId()
    GoogleTokenValidatorPort.java  ← NEW: interface for Google token validation
  service/
    AuthService.java               ← NEW: login + onboarding business logic
    AuthResult.java                ← NEW: record returned by loginWithGoogle()

adapter/
  google/
    GoogleTokenValidatorAdapter.java  ← NEW: implements GoogleTokenValidatorPort
  persistence/
    entity/UserEntity.java            ← add google_id column, nullable username
    repository/UserJpaRepository.java ← add findByGoogleId()
    adapter/UserRepositoryAdapter.java ← implement findByGoogleId()
    mapper/UserMapper.java            ← handle nullable username
  rest/
    controller/AuthController.java    ← NEW: /api/auth/google + /api/auth/complete-profile
    dto/GoogleLoginRequest.java       ← NEW
    dto/CompleteProfileRequest.java   ← NEW
    dto/AuthResponse.java             ← NEW

config/
  SecurityConfig.java   ← NEW: which routes are public/protected
  JwtAuthFilter.java    ← NEW: reads Authorization header, sets SecurityContext
  JwtService.java       ← NEW: issue and validate JWTs
  DomainConfig.java     ← add AuthService bean
```

---

# Part 2: Implementation Plan

## Overview

This plan adds "Sign in with Google" to Predict-o-rama. Currently there is no authentication — all endpoints are open and the app has no concept of identity. After this change, users log in via Google OAuth2, the backend issues its own JWT for sessions, and protected endpoints require a valid token.

**Design decisions:**
- Google only (no email+password for now)
- Username: user picks one after first Google login (onboarding screen)
- Most `/api/*` endpoints require authentication; `GET /api/tournaments/**` and `GET /api/matches/**` are public
- Sessions are stateless JWTs — no server-side session storage

---

## How It Works

### The authentication flow

```
1. User clicks "Sign in with Google"
2. Google popup → user consents → Google issues a signed ID token to the frontend
3. Frontend sends: POST /api/auth/google  { idToken: "..." }
4. Backend validates token against Google's public keys
5. Backend finds or creates a User record (by googleId)
6. Backend issues its own JWT
7a. New user (no username yet) → { status: "NEEDS_ONBOARDING", token }
7b. Returning user           → { status: "OK", token }
8. All subsequent requests carry: Authorization: Bearer <jwt>
```

### Why issue our own JWT instead of reusing Google's token?

Google's ID token expires in 1 hour and is meant for one-time identity verification. Your backend issues its own JWT (7-day expiry) containing only what it needs (`userId`, `needsOnboarding`). This JWT is validated locally with a secret — no network call to Google on every request.

### The onboarding flow

The `users` table requires a `username`. Google doesn't provide one, so new users are created with `username = null` and must choose one before using the app.

```
First login  → User created (googleId, email, username: null)
             → JWT with needsOnboarding: true
             → Frontend shows /onboarding screen
             → User picks username → POST /api/auth/complete-profile
             → New JWT with needsOnboarding: false → redirect to home

Return login → findByGoogleId → found, has username
             → JWT with needsOnboarding: false → redirect to home
```

---

## Google Cloud Console Setup (manual, one-time)

1. Go to https://console.cloud.google.com/ (use a shared team account, not a personal one)
2. Create a project
3. APIs & Services → Credentials → Create Credentials → OAuth 2.0 Client IDs
4. Application type: **Web application**
5. Authorized JavaScript origins:
    - `http://localhost:5173`
    - `http://localhost`
    - production domain when ready
6. Copy the **Client ID** → store as `GOOGLE_CLIENT_ID`
7. Add `GOOGLE_CLIENT_ID` and a freshly generated `JWT_SECRET` to GitHub Secrets

> The Client ID is **not a secret** — it ends up in frontend JavaScript. The `JWT_SECRET` is sensitive; anyone with it can forge login tokens.

---

## Database Changes

**New migration:** `backend/src/main/resources/db/changelog/005-add-google-auth.yaml`

```yaml
databaseChangeLog:
  - changeSet:
      id: 005-add-google-id-to-users
      author: team
      changes:
        - addColumn:
            tableName: users
            columns:
              - column:
                  name: google_id
                  type: VARCHAR(255)
                  constraints:
                    nullable: true
                    unique: true
        - dropNotNullConstraint:
            tableName: users
            columnName: username
            columnDataType: VARCHAR(50)
```

Include in `db.changelog-master.yaml`.

---

## Backend Changes

### New dependencies (`pom.xml`)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
<dependency>
    <groupId>com.google.api-client</groupId>
    <artifactId>google-api-client</artifactId>
    <version>2.6.0</version>
</dependency>
```

### Domain layer

**`domain/entity/User.java`**
- Add `String googleId` (nullable)
- Make `String username` nullable

**`domain/port/GoogleTokenValidatorPort.java`** — new interface
```java
public interface GoogleTokenValidatorPort {
    GoogleUserInfo validate(String idToken); // throws if invalid
}
public record GoogleUserInfo(String googleId, String email, String name) {}
```

**`domain/port/UserRepositoryPort.java`**
- Add `Optional<User> findByGoogleId(String googleId)`

**`domain/service/AuthService.java`** — new service
- `loginWithGoogle(String idToken)` → `AuthResult`
    - Validate via `GoogleTokenValidatorPort`
    - `findByGoogleId` → create user if not found (username null)
    - Return `AuthResult(user, needsOnboarding: username == null)`
- `completeProfile(UUID userId, String username)` → `User`
    - Check username not taken
    - Save and return updated user

**`domain/service/AuthResult.java`** — new record
```java
public record AuthResult(User user, boolean needsOnboarding) {}
```

**`config/DomainConfig.java`** — register `AuthService` as a bean

### Adapter layer

**`adapter/google/GoogleTokenValidatorAdapter.java`** — new
- Implements `GoogleTokenValidatorPort`
- Uses `GoogleIdTokenVerifier` from `google-api-client`
- Reads `GOOGLE_CLIENT_ID` from environment

**`adapter/persistence/entity/UserEntity.java`**
- Add `@Column(name = "google_id", unique = true) String googleId` (nullable)
- Make `username` nullable

**`adapter/persistence/repository/UserJpaRepository.java`**
- Add `Optional<UserEntity> findByGoogleId(String googleId)`

**`adapter/persistence/adapter/UserRepositoryAdapter.java`**
- Implement `findByGoogleId`

**`adapter/persistence/mapper/UserMapper.java`**
- Handle nullable username in both directions

**`adapter/rest/controller/AuthController.java`** — new
- `POST /api/auth/google` → validate Google token, return JWT
- `POST /api/auth/complete-profile` → set username, return new JWT

**New DTOs:**
- `GoogleLoginRequest` — `{ String idToken }`
- `CompleteProfileRequest` — `{ String username }`
- `AuthResponse` — `{ String token, String status, UserResponse user }`

### Security infrastructure

**`config/JwtService.java`** — new
- `generateToken(UUID userId, boolean needsOnboarding)` → signed JWT string (HMAC-SHA256)
- `validateToken(String token)` → claims map, throws if invalid/expired
- Reads `JWT_SECRET` and `JWT_EXPIRY_DAYS` (default 7) from environment

**`config/JwtAuthFilter.java`** — new (`OncePerRequestFilter`)
- Reads `Authorization: Bearer <token>` header
- Validates with `JwtService`, sets `SecurityContextHolder`
- Missing/invalid token: continues unauthenticated (Spring Security handles the 401)

**`config/SecurityConfig.java`** — new
```
Public:  GET /health, GET /, POST /api/auth/google,
         GET /api/tournaments/**, GET /api/matches/**
Protected: all other /api/**
Session policy: STATELESS
```

### Environment variables

Add to `application.properties`:
```properties
google.client-id=${GOOGLE_CLIENT_ID}
jwt.secret=${JWT_SECRET}
jwt.expiry-days=${JWT_EXPIRY_DAYS:7}
```

Add to `local-env/docker-compose.yml` backend service:
```yaml
GOOGLE_CLIENT_ID: ${GOOGLE_CLIENT_ID}
JWT_SECRET: ${JWT_SECRET}
```

---

## Frontend Changes

### New dependency

```bash
npm install @react-oauth/google
```

### `frontend/src/main.tsx`

Wrap app with `<GoogleOAuthProvider clientId={import.meta.env.VITE_GOOGLE_CLIENT_ID}>`.

Add `VITE_GOOGLE_CLIENT_ID` to `.env.local` (not a secret — public value).

### New files

**`frontend/src/context/AuthProvider.tsx`**
- Stores JWT in `localStorage`, decodes to read `needsOnboarding` and `userId`
- Exposes: `isAuthenticated`, `needsOnboarding`, `userId`, `login(token)`, `logout()`

**`frontend/src/lib/apiClient.ts`**
- Fetch wrapper that injects `Authorization: Bearer <jwt>` on every request
- Handles 401 → logout + redirect to `/login`

**`frontend/src/pages/LoginPage/LoginPage.tsx`**
- Renders `<GoogleLogin>` button
- On success: `POST /api/auth/google` → store token → redirect to `/onboarding` or `/`

**`frontend/src/pages/OnboardingPage/OnboardingPage.tsx`**
- Username input form
- On submit: `POST /api/auth/complete-profile` → store new token → redirect to `/`

**`frontend/src/app/router/ProtectedRoute.tsx`**
- Not authenticated → redirect to `/login`
- Authenticated but `needsOnboarding` and not on `/onboarding` → redirect to `/onboarding`
- Otherwise → render children

### `frontend/src/app/router/AppRouter.tsx`

```
/login       → LoginPage        (public)
/onboarding  → OnboardingPage   (requires token)
/            → HomePage         (protected)
/predictions → PredictionsPage  (protected)
```

---

## Tests

Following the existing pattern (mock all ports, test service logic only).

**`AuthServiceTest.java`**
- New user → `needsOnboarding: true`, user created in repository
- Returning user with username → `needsOnboarding: false`
- Returning user without username → `needsOnboarding: true`
- Invalid Google token → exception thrown
- `completeProfile` with taken username → exception thrown
- `completeProfile` with available username → user saved with username

**`AuthControllerTest.java`**
- `POST /api/auth/google` valid body → 200 with token
- `POST /api/auth/google` missing idToken → 400
- `POST /api/auth/complete-profile` without JWT → 401

---

## Implementation Order

1. Google Cloud Console — get Client ID before wiring anything
2. DB migration (`005-add-google-auth.yaml`)
3. Add dependencies (`pom.xml`)
4. Domain layer — `User.java`, ports, `AuthService`, `AuthResult`
5. Google token validator adapter
6. Persistence changes — entity, repository, adapter, mapper
7. `JwtService`
8. `JwtAuthFilter` + `SecurityConfig`
9. `AuthController` + DTOs
10. Backend tests
11. Frontend — `AuthProvider`, `apiClient`, `LoginPage`, `OnboardingPage`, `ProtectedRoute`, router

---

## Verification

- [ ] `./mvnw test` passes (existing + new auth tests)
- [ ] `cd local-env && docker compose up --build` starts cleanly
- [ ] `http://localhost` redirects to `/login`
- [ ] Google popup appears, consent works
- [ ] First login → `/onboarding` → username picked → home
- [ ] Refresh page → still logged in
- [ ] `GET /api/tournaments` without token → 200
- [ ] `GET /api/groups` without token → 401
- [ ] `GET /api/groups` with valid token → 200
- [ ] Logout → token cleared, redirected to `/login`
- [ ] Second login with same Google account → home directly (no onboarding)
