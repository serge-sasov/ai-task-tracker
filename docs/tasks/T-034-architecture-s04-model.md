# T-034: R-01 Solution design for S-04 day model column

| Field | Value |
|---|---|
| id | T-034 |
| title | R-01 Solution design for S-04 day model column |
| workflow | feature |
| stage | R-01 |
| role | architect |
| status | awaiting_review |
| hold | false |
| depends_on | T-030, T-031, T-033 |
| inputs | `docs/architecture/architecture-ai-task-tracker.md`, `docs/architecture/adr-ai-task-tracker.md`, `docs/analysis/requirements-ai-task-tracker.md`, `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/ux/ui-spec-ai-task-tracker.md`, `src/web/src/views/DashboardView.vue`, `src/web/src/views/CalendarDayView.vue` |
| artifacts | `docs/architecture/architecture-ai-task-tracker.md`, `docs/architecture/adr-ai-task-tracker.md`, `docs/architecture/components-ai-task-tracker.puml`, `docs/architecture/components-ai-task-tracker.svg`, `docs/architecture/sequence-scn-*.puml`, `docs/architecture/sequence-scn-*.svg` |
| e2e | required - the S-04 day-view scenario is traceable to UC-E2E-005 and must be covered by a sequence diagram |
| ux | required - the S-04 model cell (UX spec) is the input to the design and the reuse decision |
| chunks | - (to be planned at start) |
| budget | - |
| base_commit | 1010367 |
| usage | in=377315 out=104320 cr=8246608 ratio=21.9x min=249 |
| issues | - |
| commit | d9dd3f8 |

## Acceptance

- [ ] The ADR `adr-ai-task-tracker.md` is extended with a decision for the S-04 day-view model column: prefer one shared model-cell component/composable reused by both `DashboardView.vue` (S-01) and `CalendarDayView.vue` (S-04), with alternatives considered (copy-paste cell, separate component).
- [ ] The architecture description `architecture-ai-task-tracker.md` (R-01 core) is extended with the S-04 scenario (`SCN-NNN`) and the shared model-cell component/module IDs (`MOD-NNN`), and traces `R-015` to components, `SCN-NNN`, and the S-01/S-04 views.
- [ ] The component diagram `components-ai-task-tracker.puml` is updated to show the shared model-cell element and re-rendered to SVG; the diagram passes syntax and SVG checks.
- [ ] A sequence diagram `sequence-scn-*.puml` (and SVG) is added/updated for the S-04 day-view scenario showing `GET /api/tasks` -> `CalendarDayView` -> shared model cell, and traces to UC-E2E-005.
- [ ] The design confirms no backend/API/schema change is required (the `models` field is already returned by `GET /api/tasks`) and the S-01 behavior is preserved (reuse, not duplication).
- [ ] Responsibility zones are registered for the new/changed component and module (owns/does-not-own) and no two elements of the same level own the same function.
- [ ] `CP-1` (ADR, architecture core, component and per-scenario sequence diagrams) and `CP-F` (final acceptance) are declared.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-1 | R-01 solution design | `docs/architecture/architecture-ai-task-tracker.md`, `docs/architecture/adr-ai-task-tracker.md`, `docs/architecture/components-ai-task-tracker.puml/.svg`, `docs/architecture/sequence-scn-*.puml/.svg` | 22P/0F/1M | awaiting_review | docs/checkpoints/T-034-CP-1.md |
| CP-F | final acceptance | all task artifacts | 23P/0F/1M | awaiting_review | docs/checkpoints/T-034-CP-F.md |

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

- 2026-11-19: T-034 registered as a pending R-01 successor of T-030/T-031/T-033 (S-04 calendar day model column); launch protocol copied from the T-029 card; no work started.
- 2026-11-20: T-034 set in_progress; lead verified board invariant and deps (T-030/T-031/T-033 done); work delegated to harness-architect (R-01, CP-1 target).
- 2026-11-20: CP-1 awaiting_review (R-01); checks 22 PASS/0 FAIL/1 MANUAL/0 UNCHECKED; usage in=248949 out=74732 cr=7303374 ratio=29.3x min=194; reviewer user
- 4348cea: CP-1 snapshot
- 2026-11-20: CP-1 approved by user (chat); task returned to in_progress; proceeding to CP-F final acceptance.
- 2026-11-20: CP-F awaiting_review (R-01); checks 23 PASS/0 FAIL/1 MANUAL/0 UNCHECKED; usage in=377315 out=104320 cr=8246608 ratio=21.9x min=249; reviewer user
- d9dd3f8: CP-F snapshot
