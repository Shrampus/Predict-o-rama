# Frontend Rule Guide (Strict)

**Audience:** junior developers and AI coding tools.  
**Stack:** React, TypeScript, Tailwind CSS, React Router.  
**Tone:** These are project rules, not suggestions.

---

### 1. Project Structure

- **MUST** keep the **page-centered modular structure** under `src/pages/`.
- **MUST** place each page in its own folder: `src/pages/<PageName>/<PageName>.tsx`.
- **MUST** keep code that exists **only** for that page inside that page folder:
  - `components/` — UI pieces used mainly by this page
  - `hooks/` — state, effects, and orchestration for this page
- **MUST** name the main page file exactly like the folder: `HomePage/HomePage.tsx`.
- **MUST NOT** scatter one page’s pieces across unrelated folders.
- **MUST** move code to a shared location only when it is actually reused across multiple pages or is clearly cross-cutting.
- **MUST NOT** create shared abstractions “just in case”.

**Examples**

- ❗ Wrong — putting page-only UI at `src/components/HomeHero.tsx` when only `HomePage` uses it.
- ✅ Correct — `src/pages/HomePage/components/HomeHero.tsx`.

- ❗ Wrong — `src/pages/HomePage/HomePage.tsx` contains 400 lines of hooks and helpers inline.
- ✅ Correct — thin `HomePage.tsx`, with `src/pages/HomePage/hooks/useHomePage.ts` (or smaller hooks) and `components/` for UI blocks.

---

### 2. Naming Conventions

| Kind | Rule |
|------|------|
| Components, pages, types (when named like components) | **PascalCase** (`PredictionsCard`, `HomePage`) |
| Hooks | **`use` + PascalCase remainder** (`usePredictionsList`) |
| Variables, functions, methods | **lowerCamelCase** (`fetchPredictions`, `activeTab`) |
| Constants (including env reads you treat as constants) | **UPPER_SNAKE_CASE** (`MAX_RETRIES`, `API_BASE_URL`) |
| Route path strings in React Router | **kebab-case** (`/predictions`, `/match/:matchId`) |
| Files and folders for pages/components | Match the exported name: `HomePage.tsx`, folder `HomePage/` |

- **MUST** use **descriptive** names. If a name needs a comment to explain it, rename it.
- **MUST NOT** use cryptic abbreviations (`prd`, `tmp`, `btn1`) unless they are universally understood (`id`, `url`, `api` as part of a longer name is fine).
- **MUST** name booleans with prefixes like **`is`**, **`has`**, **`can`**, **`should`**: `isLoading`, `hasError`, `canSubmit`, `shouldShowBanner`.

**Examples**

- ❗ Wrong — `const d = data.filter(...)`
- ✅ Correct — `const activePredictions = predictions.filter(...)`

- ❗ Wrong — `const ok = !error && items.length > 0`
- ✅ Correct — `const canSubmit = !error && items.length > 0`

- ❗ Wrong — `<Route path="/Predictions" ... />`
- ✅ Correct — `<Route path="/predictions" ... />`

---

### 3. Components

- **ONLY** use **functional components** (no class components).
- **MUST** treat components as **UI rendering**: structure, layout, presenting props, emitting events via callbacks.
- **MUST NOT** call **APIs or `fetch`** inside components. **MUST NOT** import **service modules** in presentational components.
- **MUST NOT** place **heavy business rules** in components (large `if/else` trees, complex calculations). Move that to hooks or pure helper functions.
- **MUST** keep components **small and focused** (one clear job).
- **MUST** extract **repeated UI** into reusable components (in the page folder first, or shared only when truly multi-page).
- **MUST** keep the **page file readable** (composition + wiring props), not a dump for all logic.

**Examples**

- ❗ Wrong — inside `PredictionsCard.tsx`: `fetch('/api/...').then(...)` or `axios.get(...)`.
- ✅ Correct — `PredictionsCard` receives `prediction` and `onSelect` as props; a hook loads data and passes props.

- ❗ Wrong — a 200-line JSX block with inline sorting, filtering, and URL building in one component.
- ✅ Correct — parent or hook prepares `visibleItems`; child maps and renders rows.

---

### 4. Hooks

- **MUST** put **stateful logic**, **`useEffect`**, **subscriptions**, and **async flows that orchestrate state** in **custom hooks** (or coordinated small hooks), not in fat components.
- **MUST** put **page-specific** hooks under that page: `src/pages/<PageName>/hooks/`.
- **MUST** design hooks to be **reusable** when the same behavior is needed in more than one place; then place them only in a shared spot **after** that need is real (not “maybe later”).
- **MUST NOT** return **JSX** from hooks. Hooks return **data and functions**, not UI.
- **MUST** return **simple, explicit values** (objects with stable, clear keys) so components stay dumb and easy to read.

**Examples**

- ❗ Wrong — `function useThing() { return <div />; }`
- ✅ Correct — `function useThing() { return { isLoading, data, error, reload }; }`

- ❗ Wrong — `HomePage.tsx` with five `useState` and three `useEffect` inline.
- ✅ Correct — `useHomePage.ts` (or smaller `useHomeData`, `useHomeFilters`) consumed by `HomePage.tsx`.

---

### 5. State and Data Flow

**Standard pattern (junior default):**

- **Components** → UI only  
- **Hooks** → state, side effects, wiring user actions  
- **Services** → HTTP/API calls, raw data access  

Rules:

- **MUST** prefer **local state** (`useState` in a hook) first.
- **MUST** lift state **only** when multiple children **truly** need the same changing value.
- **MUST** avoid **prop drilling** past two levels; extract a hook/provider **only when needed** (do **not** add global state libraries unless explicitly requested).
- **MUST** keep data flow **one direction and predictable**: load in hook/service → shape in hook → render in component.
- **MUST NOT** mix **fetching**, **UI**, and **heavy data transformation** in one place. Transform near where data is prepared (usually hook or a pure helper).

**Examples**

- ❗ Wrong — component calls service, sets state, formats dates for display, and branches navigation logic in one `useEffect`.
- ✅ Correct — service returns DTO; hook calls service, maps to view-model, exposes `items` and `onRetry`; component maps `items` to JSX.

---

### 6. Services / API

- **MUST** put all **HTTP/API** code in **service modules** (for example `src/services/predictionsApi.ts`). Create service files when endpoints appear; do not leave fetches in hooks without at least a dedicated function in a service file.
- **MUST NOT** let **components** call APIs or services directly.
- **MAY** let **hooks** call **services**.
- **MUST** use **`async`/`await`** for async code in services and hooks (not raw promise chains in new code).
- **MUST** handle errors **explicitly**: throw meaningful errors, or return a **discriminated** result type the hook can turn into UI state (`error` string).
- **MUST** keep services **thin**: URLs, verbs, payloads, parsing — **no** toasts, **no** navigation, **no** React imports.

**Examples**

- ❗ Wrong — `HomePage.tsx`: `const res = await fetch(...)` in an `onClick`.
- ✅ Correct — `predictionsApi.getLatest()` called from `useHomePage`, which sets `data` / `error`.

---

### 7. Styling Requirements (Tailwind CSS)

- **MUST** use **Tailwind utility classes** for styling (no ad-hoc inline styles unless there is a strong reason like dynamic values not expressible with utilities).
- **MUST** keep `className` strings **readable**: group logically (layout → spacing → typography → color → state variants). Break very long lines; use variables for repeated strings only when duplication is real.
- **MUST** extract repeated **visual patterns** into components instead of copy-pasting large Tailwind blocks.
- **MUST NOT** create “god components” with endless unreadable class strings; split layout vs. inner pieces.
- **MUST** keep styles **in the component file** unless extraction improves reuse or clarity.

**Examples**

- ❗ Wrong — same 25-class `<button>` pasted in six files.
- ✅ Correct — `<PrimaryButton>`, `<IconButton>`, or a small shared component in the page `components/` folder.

---

### 8. Clean Code Rules

- **MUST** follow **single responsibility** per function, hook, and component.
- **MUST** prefer **early returns** over **deep nesting**.
- **MUST** keep functions **short**; if a function does many steps, extract named helpers.
- **MUST** apply **DRY**, but **MUST NOT** invent abstractions for one-time code.
- **MUST** use **explicit TypeScript types** for public props, service responses you rely on, and non-trivial objects. Avoid `any`. Use `unknown` only with narrowing.
- **MUST NOT** scatter **magic strings/numbers**; use named constants when meaning is not obvious from context.
- **MUST** optimize for **readability first** for reviewers and juniors.

---

### 9. AI-Specific Rules (Non-Optional)

**AI tools MUST:**

- **Follow this structure exactly** (`src/pages/<Page>/<Page>.tsx`, plus `components/` and `hooks/` inside the page when applicable).
- **Preserve** the existing architecture and folder meanings.
- **Make minimal, surgical changes** that solve the stated task only.
- **Edit only files** that are **clearly relevant** to the task.
- **Reuse** existing patterns (imports, routing, layout, naming) before inventing new ones.
- **Produce production-ready** TypeScript/React code (complete implementations).
- **Avoid repo-wide formatting-only** changes or mass renames.
- **Avoid new dependencies** without **explicit approval**.

**AI tools MUST NOT:**

- **Invent a new architecture** (for example different top-level folder schemes).
- **Move logic randomly** between layers or folders.
- **Add state management libraries** (Redux, MobX, Zustand, etc.) unless the user **explicitly** requests one.
- **Put API calls inside components**.
- **Refactor unrelated files** “while I’m here.”
- **Add placeholder code** (`TODO`, `...`, fake functions) instead of working code.
- **Output pseudocode** when the task is an implementation task.

---

### 10. Non-Negotiables (Project Hard Rules)

- **Preserve** the page-based `src/pages/<PageName>/` structure.
- **No unrelated refactors** in the same change.
- **No unnecessary dependencies.**
- **MUST** write explicit types for props, service responses, hook return types when non-trivial, and shared/public interfaces.
- **MAY** rely on type inference for obvious local variables.
- **No API calls in components** — services + hooks only.
- **Keep diffs small**, readable, and easy to review.

---

**Summary:** One page = one folder under `src/pages/` with a thin `PageName.tsx`, local `components/` and `hooks/` when needed, all HTTP in `src/services/`, Tailwind for UI, strict naming, predictable data flow — and AI/human changes stay minimal, typed, and on-structure.
