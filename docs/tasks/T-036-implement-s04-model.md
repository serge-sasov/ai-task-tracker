# T-036: I-01 Implementation and unit tests for S-04 model column

| Field | Value |
|---|---|
| id | T-036 |
| title | I-01 Implementation and unit tests for S-04 model column |
| workflow | feature |
| stage | I-01 |
| role | coder |
| status | in_progress |
| hold | false |
| depends_on | T-035, T-033 |
| inputs | `src/web/src/views/CalendarDayView.vue`, `src/web/src/views/DashboardView.vue`, `src/web/src/api/types.ts`, `docs/architecture/contracts-ai-task-tracker.md`, `docs/ux/ui-spec-ai-task-tracker.md`, `tests/**` |
| artifacts | `src/web/src/views/CalendarDayView.vue`, `src/web/src/components/ModelCell.vue`, `src/web/src/api/types.ts`, `tests/**` |
| e2e | required - the implemented S-04 model cell must be covered by the UC-E2E-005 scenario (I-02) |
| ux | required - the implemented cell must match the UX spec states and accessibility text |
| chunks | - (to be planned at start) |
| budget | - |
| base_commit | - |
| usage | n/a |
| issues | - |
| commit | - |

## Acceptance

- [ ] `CalendarDayView.vue` (S-04 day view) renders a "Модель" column after "Задача" showing the primary model, `+N` when more than one, the full ordered list in `title`/`aria-label`, and "Нет данных" when `models` is empty.
- [ ] One shared model-cell component or composable (e.g. `src/web/src/components/ModelCell.vue`) is introduced and used by both `DashboardView.vue` (S-01) and `CalendarDayView.vue` (S-04), following the S-01 model-cell rules; no copy-paste duplication of the cell logic.
- [ ] The S-01 behavior and its existing tests remain green; if the shared element is introduced, a regression test covers the S-01 cell and the S-04 cell.
- [ ] The change is frontend-only: no backend/API/schema change; `types.ts` is only touched if a shared type is needed; the built frontend reflects the new column.
- [ ] Unit/component tests for the new/changed element pass and the project build succeeds; no `TODO`/`FIXME` is left in the new code.
- [ ] `CP-1` (implementation and tests) and `CP-F` (final acceptance) are declared.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-1 | I-01 implementation and tests | `src/web/src/views/CalendarDayView.vue`, `src/web/src/components/ModelCell.vue`, `src/web/src/api/types.ts`, `tests/**` | - | pending | `docs/checkpoints/T-036-CP-1.md` |
| CP-F | final acceptance | all task artifacts | - | pending | `docs/checkpoints/T-036-CP-F.md` |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.
`Checks` is a machine summary `nP/nF/nM` written by `stage-done.ps1`.

## Launch protocol (anti-hang)

- Start with a 5-line plan and the first tool call; do not spend more than 60 seconds or 2k output tokens in analysis before writing the first artifact.
- Read only the card inputs and the specific line ranges; do not scan the whole repository or re-read completed artifacts.
- For chunked stages, run `chunk.ps1 init/add/plan` before authoring; execute one chunk per session; stop at the chunk boundary.
- Budgets: Markdown 8 KB/write and 20 KB/session; code 12 KB/write and 40 KB/session; stop at 60% context (65% for code); QA final message <=10 lines.
- If no artifact is produced within 15 minutes or after 2 attempts, stop and escalate: mark the chunk `failed`/`blocked` with the error; do not repeat the same oversized call.
- At the start of every role session, emit only `START T-XXX <stage> chunk <id>` and proceed with the first tool call; avoid long free-form reasoning.

## Blockers

- (none)

## Log

- 2026-11-19: T-036 registered as a pending I-01 successor of T-035/T-033 (S-04 calendar day model column); launch protocol copied from the T-029 card; no work started.
- 2026-11-20: T-036 started: I-01 implementation for S-04 model column (coder). Board and card set in_progress by orchestrator.
