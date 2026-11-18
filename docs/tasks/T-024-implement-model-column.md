# T-024: I-01 Implementation and unit tests: S-01 model column

| Field | Value |
|---|---|
| id | T-024 |
| title | I-01 Implementation and unit tests: S-01 model column |
| workflow | feature |
| stage | I-01 |
| role | coder |
| status | in_progress |
| hold | false |
| depends_on | T-023, T-021 |
| inputs | `docs/tasks/T-023-contracts-model-column.md`, `docs/tasks/T-021-prototype-model-column.md`, `docs/architecture/contracts-ai-task-tracker.md` (R-014, §4.2), `docs/analysis/requirements-ai-task-tracker.md` (R-014), `docs/analysis/e2e-use-cases-ai-task-tracker.md` (UC-E2E-003), `docs/architecture/architecture-ai-task-tracker.md` (class model) |
| artifacts | `src/main/java/ru/aitasktracker/api/dto/TaskListItem.java`, `src/main/java/ru/aitasktracker/api/service/TaskQueryService.java`, `tests/java/ru/aitasktracker/api/service/TaskQueryServiceTest.java`, `src/web/src/api/types.ts`, `src/web/src/views/DashboardView.vue` |
| e2e | required - I-02 will automate UC-E2E-003 S8 after I-01 |
| ux | required - S-01 model cell per T-021 (column after «Название», primary + `+N`, tooltip/aria full list, «Нет данных») |
| chunks | `docs/chunks/T-024.json` (C1 backend DTO/service/tests, C2 frontend types/view) |
| budget | max_write 32 KB / session 64 KB; two chunks |
| base_commit | d4dde64 |
| usage | n/a |
| issues | - |
| commit | - |

## Acceptance

- [x] `TaskListItem.models` is ordered by total tokens (input+output+cache) descending, then model name ascending (contracts §4.2, R-014).
- [x] `models` is derived from the same `token_usage` rows used for `tokens_total`; no extra query per task (reuse the existing rows).
- [x] An empty `models` array means no usage → the S-01 cell shows «Нет данных».
- [x] Frontend column is placed after «Название», shows the primary model + `+N` (overflow count), `title`/`aria` carry the full model list, and «Нет данных» when the list is empty (T-021 UI spec).
- [x] Unit tests cover ordering, ties (same total → name asc), and the empty (no-usage) case.
- [x] Targeted `TaskQueryServiceTest` 15/15 passes and `npm run build` in `src/web` passes; full backend suite has 5 pre-existing failures reproduced at base `d4dde64` and tracked in BUG-007.
- [x] Chunks C1 and C2 are `done` with produced hashes in `docs/chunks/T-024.json`.
- [x] No unrelated changes.
- [x] `CP-1` (implementation and unit tests) and `CP-F` (final acceptance) are declared.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-1 | I-01 DTO, service, tests, frontend column | TaskListItem, TaskQueryService, TaskQueryServiceTest, types.ts, DashboardView.vue | - | pending | `docs/checkpoints/T-024-CP-1.md` |
| CP-F | final acceptance | all task artifacts | - | pending | `docs/checkpoints/T-024-CP-F.md` |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.

## Chunk plan

| Chunk | Stage | Role | Title | Owns | State |
|---|---|---|---|---|---|
| C1 | I-01 | coder | Backend: ordered `models` field and TaskQueryService tests | `src/main/java/ru/aitasktracker/api/dto/TaskListItem.java`, `src/main/java/ru/aitasktracker/api/service/TaskQueryService.java`, `tests/java/ru/aitasktracker/api/service/TaskQueryServiceTest.java` | pending |
| C2 | I-01 | coder | Frontend: S-01 model column (primary + `+N`, tooltip/aria, empty state) | `src/web/src/api/types.ts`, `src/web/src/views/DashboardView.vue` | pending |

## Log

- 2026-11-18: T-024 started on base `d4dde64`; T-023 and T-021 are done. C1 (backend) and C2 (frontend) planned as `pending` in `docs/chunks/T-024.json`; coder will claim/complete them.
- 2026-11-18: Coder completed C1 and C2 (both `done` in `docs/chunks/T-024.json`). Targeted `TaskQueryServiceTest` 15/15 pass; `npm run build` (src/web) pass. Full backend suite shows 5 pre-existing failures (SearchServiceTest ×3, TokenServiceTest ×2) reproduced at base `d4dde64`, out of scope for R-014, recorded as BUG-007.
