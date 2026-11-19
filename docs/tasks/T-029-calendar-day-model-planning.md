# T-029: P-00 Feature planning - S-04 calendar day model display

| Field | Value |
|---|---|
| id | T-029 |
| title | P-00 Feature planning: S-04 calendar day model column |
| workflow | feature |
| stage | P-00 |
| role | orchestrator |
| status | in_progress |
| hold | false |
| depends_on | T-028 |
| inputs | `docs/plan.md`, `harness/process.md`, `harness/workflows/feature.md`, `docs/tasks/T-028-final-acceptance-model-column.md`, `docs/tasks/T-018-requirements-model-column.md`, `docs/analysis/requirements-ai-task-tracker.md`, `docs/ux/screens-ai-task-tracker.md`, `docs/ux/ui-spec-ai-task-tracker.md`, `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/analysis/e2e-test-data-ai-task-tracker.yaml`, `docs/architecture/contracts-ai-task-tracker.md`, `src/web/src/views/CalendarDayView.vue`, `src/web/src/views/DashboardView.vue` |
| artifacts | `docs/plan.md`, `docs/tasks/T-029-calendar-day-model-planning.md`, `docs/activity.puml`, `docs/activity.svg` |
| e2e | required - S-04 day view gets a user-visible model column; UC-E2E-005 and the YAML dataset need a model-display delta; I-02/Q-01/Q-02 are in the graph |
| ux | required - new visible S-04 column with the primary model, `+N`, tooltip/aria full list and "Нет данных"; U-01/U-02 are in the graph |
| chunks | - (P-00 planning produces board, card and checkpoints; no ledger) |
| budget | - |
| base_commit | dfb9c15 |
| usage | in=291770 out=44854 cr=1320477 ratio=4.5x min=189 |
| issues | ISSUE-20261118-185907, ISSUE-20261118-153622 |
| commit | 07dcead |

## Acceptance

- [ ] Scope fixed: S-04 day view (`/calendar/:date`) shows a visible "Модель" column immediately after "Задача"; it shows the primary model plus `+N`, the full ordered list in `title` and `aria-label`, and "Нет данных" when `models` is empty.
- [ ] Reuse: no backend/API/schema change is expected; `GET /api/tasks` already returns `models` per task and `CalendarDayView.vue` already calls it. Confirm against contracts §4.2/§4.5 before planning.
- [ ] Consistency: the S-04 cell follows the S-01 model-cell rules and does not change S-01 behavior; if a shared component or composable is introduced, the S-01 regression is covered.
- [ ] e2e applicability recorded (`required`) with a concrete UC-E2E-005 delta and YAML dataset update; evidence must verify the actually built frontend (rebuilt image or DOM smoke), not only the offline prototype.
- [ ] UX applicability recorded (`required`) with an S-04 screens/UI-spec/prototype delta; the UX deliverable includes the model-cell states and accessibility text.
- [ ] Task graph is created with explicit dependencies and per-stage applicability; every waiver has a reason; no silent skip of required stages.
- [ ] Launch protocol (anti-hang) is included in this card and copied into every successor card created at CP-1.
- [ ] Existing pending baseline tasks T-010, T-011 and T-012 are untouched.
- [ ] `CP-1` (plan and task graph) and `CP-F` (final acceptance) are declared.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-1 | P-00 plan and task graph | `docs/plan.md`, this card, `docs/activity.puml/.svg` | 5P/0F/0M | approved | docs/checkpoints/T-029-CP-1.md |
| CP-F | final acceptance | all task artifacts | - | pending | `docs/checkpoints/T-029-CP-F.md` |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.

## Proposed scope (to confirm at CP-1)

- Page: `src/web/src/views/CalendarDayView.vue` for `/calendar/:date` (S-04 day view).
- Table columns: `Задача | Модель | Начало | Окончание | Продолжительность | Статус`.
- Cell: primary model (first `models` item); `+N` when more than one; `title` and `aria-label` carry the full ordered list; "Нет данных" when `models` is empty.
- Data: `tasksApi.list` already returns `TaskListItem.models`; no backend change expected. Confirm with a live `GET /api/tasks?from=...&to=...` and the T-024/T-027 evidence.
- Reuse: prefer one shared model-cell helper/component or a small composable over copy-paste; keep S-01 behavior and tests intact.
- E2E: extend UC-E2E-005 and the YAML dataset; add a live check of the built S-04 page. Because the previous feature passed API-only tests while the stale frontend image lacked the S-01 column (T-027/T-028), this task must include a built-artifact or browser check and a frontend rebuild in the local run.
- UX: cover S-04 success/loading/empty/error states and the accessible model text; update the prototype page for UC-E2E-005.
- No DB migration; no new API endpoint.

## Launch protocol (anti-hang)

- Start with a 5-line plan and the first tool call; do not spend more than 60 seconds or 2k output tokens in analysis before writing the first artifact.
- Read only the card inputs and the specific line ranges; do not scan the whole repository or re-read completed artifacts.
- For chunked stages, run `chunk.ps1 init/add/plan` before authoring; execute one chunk per session; stop at the chunk boundary.
- Budgets: Markdown 8 KB/write and 20 KB/session; code 12 KB/write and 40 KB/session; stop at 60% context (65% for code); QA final message <=10 lines.
- If no artifact is produced within 15 minutes or after 2 attempts, stop and escalate: mark the chunk `failed`/`blocked` with the error; do not repeat the same oversized call.
- Reference `ISSUE-20261118-185907` (SSE timeout during T-026) and process §11; on a transport timeout, resume the same session if an artifact exists; otherwise split the chunk.
- At the start of every role session, emit only `START T-XXX <stage> chunk <id>` and proceed with the first tool call; avoid long free-form reasoning.

## Blockers

- (none)

## Log

- 2026-11-19: T-029 registered as a pending feature planning task for the S-04 calendar day model display; launch protocol added from the T-026/T-027 timeout lessons; no work started.
- 2026-11-19: T-029 started (P-00 planning); previous orchestrator session cancelled with no artifacts.
- 2026-11-19: CP-1 prepared: graph T-030..T-040 registered, 11 cards created, D-01 in scope (delta over T-012 baseline).
- 2026-11-19: CP-1 approved (P-00); checks 5 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage in=291770 out=44854 cr=1320477 ratio=4.5x min=189; reviewer auto (P2)
- 07dcead: CP-1 snapshot
