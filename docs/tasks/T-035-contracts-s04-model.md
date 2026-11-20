# T-035: R-02 Contracts and module handoff for S-04

| Field | Value |
|---|---|
| id | T-035 |
| title | R-02 Contracts and module handoff for S-04 |
| workflow | feature |
| stage | R-02 |
| role | architect |
| status | done |
| hold | false |
| depends_on | T-034 |
| inputs | `docs/architecture/architecture-ai-task-tracker.md`, `docs/architecture/contracts-ai-task-tracker.md`, `docs/analysis/requirements-ai-task-tracker.md`, `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `src/web/src/api/types.ts`, `src/web/src/views/CalendarDayView.vue`, `src/web/src/views/DashboardView.vue` |
| artifacts | `docs/architecture/architecture-ai-task-tracker.md`, `docs/architecture/contracts-ai-task-tracker.md`, `docs/architecture/openapi-api-*.yaml`, `docs/architecture/data-model-*.puml`, `docs/architecture/data-model-*.svg`, `docs/architecture/class-*.puml`, `docs/architecture/class-*.svg`, `docs/architecture/swimlane-svc-*.puml` |
| e2e | required - the S-04 contract must trace to UC-E2E-005 and the existing `GET /api/tasks` response shape |
| ux | required - the S-04 model cell contract must match the UX spec states and accessibility text |
| chunks | - (to be planned at start) |
| budget | - |
| base_commit | - |
| usage | in=307266 out=99054 cr=5519740 ratio=18x min=234 |
| issues | - |
| commit | 57e2789 |

## Acceptance

- [ ] `contracts-ai-task-tracker.md` is extended with the S-04 module boundary: the frontend shared model-cell component owns the `models: string[]` -> display mapping (primary, `+N`, full list, "Нет данных") and the day view reuses it; the API contract is unchanged (no new endpoint, no schema change).
- [ ] The architecture description is extended with the S-04 data/class model and `CLS-NNN` IDs for the shared model-cell element, and traces `R-015` to `API-NNN` (the existing `GET /api/tasks`), `SCN-NNN`, and the class.
- [ ] The OpenAPI spec `openapi-api-*.yaml` is confirmed to already expose `models` in the task list item response; if no change is required, the description states "No new REST API" and the existing spec is re-validated.
- [ ] The data model and class diagram (`.puml`/`.svg`) are updated for the shared model-cell element and re-rendered; the swimlane diagram for the relevant service is updated and re-rendered; all diagrams pass syntax and SVG checks.
- [ ] The module handoff to the coder names the shared component/composable, the files to touch (`CalendarDayView.vue`, `DashboardView.vue`, a new shared element, `types.ts`) and the test expectations, and states the S-01 behavior must remain green.
- [ ] Class-level responsibility zones are registered and no two classes of the same level own the same function; every artifact is linked from `architecture-*.md`.
- [ ] `CP-2` (contracts, data/class models, swimlanes, module handoff) and `CP-F` (final acceptance) are declared.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-2 | R-02 contracts and module handoff | `docs/architecture/architecture-ai-task-tracker.md`, `docs/architecture/contracts-ai-task-tracker.md`, `docs/architecture/openapi-api-*.yaml`, `docs/architecture/data-model-*.puml/.svg`, `docs/architecture/class-*.puml/.svg`, `docs/architecture/swimlane-svc-*.puml` | 19P/2F/1M | approved | docs/checkpoints/T-035-CP-2.md |
| CP-F | final acceptance | all task artifacts | 20P/2F/1M | approved | docs/checkpoints/T-035-CP-F.md |

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

- 2026-11-19: T-035 registered as a pending R-02 successor of T-034 (S-04 calendar day model column); launch protocol copied from the T-029 card; no work started.
- 2026-11-20: T-035 started in_progress by lead dispatch; R-02 architect role session will follow; no artifacts yet.
- 2026-11-20: CP-2 close-out. Fixed card `artifacts` row to include the `.svg` renders it mandates (added `docs/architecture/data-model-*.svg` and `docs/architecture/class-*.svg`), resolving the `chunk-coverage` `E_OWN_OUTSIDE_CARD` flagged in ISSUE-20261120-203629 (card omitted `.svg` while Acceptance line 29 requires them re-rendered). Re-ran verify-stage.
- 2026-11-20: CP-2 awaiting_review (R-02); checks 19 PASS/2 FAIL/1 MANUAL/0 UNCHECKED; usage in=220874 out=72934 cr=4835343 ratio=21.9x min=165; reviewer user
- 6f613e9: CP-2 snapshot
- 2026-11-20: CP-2 approved by user (in-session chat, 2026-11-20T22:09:32). The 2 FAILs (artifacts-exist swimlane conditional glob; chunk-budget ISSUE-20261120-113748) acknowledged and waived; MANUAL zones-overlap-review settled by user review (18 zones, no overlap). Task returned to in_progress to proceed to CP-F.
- 2026-11-20: CP-F awaiting_review (R-02); checks 20 PASS/2 FAIL/1 MANUAL/0 UNCHECKED; usage in=307266 out=99054 cr=5519740 ratio=18x min=234; reviewer user
- 57e2789: CP-F snapshot
- 2026-11-20: CP-F approved by user (in-session chat, "approve CP-F"). Task T-035 closed done.
