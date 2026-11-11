# T-001: Feature planning — «Сервис мониторинга активностей пользователя в opencode»
decision: approved

| Field | Value |
|---|---|
| id | T-001 |
| title | Feature planning: fix scope, e2e/UX applicability, create the task graph and register it on the board |
| workflow | feature |
| stage | P-00 |
| role | orchestrator |
| status | done |
| depends_on | - |
| inputs | `requirements/исходные требования.txt`, `harness/workflows/feature.md`, `harness/process.md` |
| artifacts | `docs/plan.md`, `docs/tasks/T-001-ai-task-tracker-feature-planning.md`, `docs/checkpoints/T-001-CP-1.md`, `docs/checkpoints/T-001-CP-F.md`, `docs/activity.puml`, `docs/activity.svg` |
| e2e | required — feature with user-facing UI and backend service; e2e scope confirmed at planning (A-02 / I-02 / Q-01 / Q-02 in graph) |
| ux | required — the feature has a user-facing web interface (calendar view, AI search, token dashboard); UX scope confirmed at planning (U-01 / U-02 in graph) |
| issues | - |
| commit | - |

## Acceptance

- [x] Scope is fixed: the service monitors opencode user activities — what was done in tasks, what solutions were proposed and chosen, how tasks ended; calendar view of tasks with duration; AI-based task search; token consumption per model.
- [x] e2e applicability decided with a reason (`e2e: required`, see field above); stages A-02, I-02, Q-01 in the graph.
- [x] UX applicability decided with a reason (`ux: required`, see field above); stages U-01, U-02 in the graph.
- [x] Task graph built from the feature workflow stage table: `A-01 -> A-02 -> U-01 -> U-02 -> R-01 -> R-02 -> I-01 -> I-02 -> Q-01 -> Q-02 -> D-01`.
- [x] All P-00 successor tasks registered as `pending` rows in `docs/plan.md` (T-002..T-012) with explicit dependencies.
- [x] No cards for successor tasks are created at this checkpoint — cards are created at the start of each task (planning board rows only).
- [x] `CP-1` (plan and task graph) and `CP-F` (final acceptance) declared in the checkpoints table.
- [x] Tech stack recorded as a planning constraint: backend — Docker, Java, Postgres, Redis; frontend — Vue.js (web).

## Checkpoints

| ID | Stage | Artifacts | Status | Review file |
|---|---|---|---|---|
| CP-1 | P-00 plan and task graph | `docs/plan.md`, this card, `docs/activity.puml/.svg` | approved | `docs/checkpoints/T-001-CP-1.md` |
| CP-F | final acceptance | all task artifacts | approved | `docs/checkpoints/T-001-CP-F.md` |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.

## Planning scope (fixed at CP-1)

### Product scope (from source requirements)

1. Task activity tracking in opencode: per task — what was done, which solutions were proposed, which one was chosen, how the task ended.
2. Calendar view of tasks with their duration.
3. Task search with AI assistance.
4. Token consumption accounting in the breakdown of used models.

### Non-functional / tech-stack constraints

- Backend: Docker, Java, Postgres, Redis.
- Frontend: Vue.js (web).
- Local-only operations; no external APIs for harness operations (role models are local Ollama).

### Task graph (board rows, all `pending` until P-00 closes)

| Board ID | Stage | Title | Role | Depends on |
|---|---|---|---|---|
| T-002 | A-01 | Requirements analysis | analyst | T-001 |
| T-003 | A-02 | E2E use cases and dataset | analyst | T-002 |
| T-004 | U-01 | UX flows and screens | ux-designer | T-002, T-003 |
| T-005 | U-02 | Wireframes, prototype, UI spec | ux-designer | T-004 |
| T-006 | R-01 | Architecture and contracts | architect | T-002, T-003, T-005 |
| T-007 | R-02 | Diagrams and handoff | architect | T-006 |
| T-008 | I-01 | Implementation + tests | coder | T-007, T-005 |
| T-009 | I-02 | E2E automation | coder | T-008, T-003, T-007 |
| T-010 | Q-01 | E2E plan and coverage | qa | T-003, T-009 |
| T-011 | Q-02 | QA and verdict | qa | T-010, T-009, T-008, T-007, T-005 |
| T-012 | D-01 | Documentation | doc-writer | T-007, T-008, T-005, T-011 |

Chain: `T-001 -> T-002 -> T-003 -> T-004 -> T-005 -> T-006 -> T-007 -> T-008 -> T-009 -> T-010 -> T-011 -> T-012` (with cross-links per the table).

## Blockers

- (none)

## Log

- 2026-11-11: P-00 started. Read `harness/process.md`, `harness/workflows/feature.md`, source requirements. Initialized git repository (`master`), created `.gitignore`, baseline commit `1227337e` (harness + requirements + docs skeleton). Registered T-001 as `in_progress` and T-002..T-012 as `pending` in `docs/plan.md`. Created this card with fixed scope, e2e/ux applicability (both `required` with reasons), and the task graph. Updated `docs/activity.puml`, rendered `docs/activity.svg`. Created checkpoint file `docs/checkpoints/T-001-CP-1.md`, status `awaiting_review`. Snapshot commit `7ed5367`.
- 2026-11-11: CP-1 approved by user (decision line added to card). Verified acceptance: board (T-002..T-012 all pending, only T-001 active), card (scope, e2e/ux required with reasons, CP-1+CP-F declared), git diff (only user's `decision: approved` line). Resumed as `in_progress`, then prepared CP-F final acceptance checkpoint (`docs/checkpoints/T-001-CP-F.md`), set T-001 to `awaiting_review`. Snapshot commit `aec95630`.
- 2026-11-11: CP-F approved by user (decision in `docs/checkpoints/T-001-CP-F.md`). Final acceptance verified (all card acceptance items satisfied; T-002..T-012 pending, no successor cards; tech stack recorded). T-001 -> `done`; board/card/diagram finalized (green #90EE90); T-002 (A-01) unblocked (pending). Commits: `1227337e` baseline, `7ed5367` CP-1 snapshot, `aec95630` CP-F snapshot, final `T-001: feature planning (P-00) accepted`.
