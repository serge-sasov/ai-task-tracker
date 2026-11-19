# T-032: U-01 UX flows and screens for S-04 model column

| Field | Value |
|---|---|
| id | T-032 |
| title | U-01 UX flows and screens for S-04 model column |
| workflow | feature |
| stage | U-01 |
| role | ux-designer |
| status | awaiting_review |
| hold | false |
| depends_on | T-030, T-031 |
| inputs | `docs/ux/user-flows-ai-task-tracker.md`, `docs/ux/screens-ai-task-tracker.md`, `docs/analysis/requirements-ai-task-tracker.md`, `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/tasks/T-031-e2e-s04-model.md`, `src/web/src/views/CalendarDayView.vue`, `src/web/src/views/DashboardView.vue` |
| artifacts | `docs/ux/user-flows-ai-task-tracker.md`, `docs/ux/screens-ai-task-tracker.md` |
| e2e | required - the S-04 day-view flow must be traceable to UC-E2E-005 |
| ux | required - the S-04 day view gets a visible model column with primary model, `+N`, tooltip/aria and "Нет данных" states |
| chunks | - (to be planned at start) |
| budget | - |
| base_commit | c3a20d6 |
| usage | in=240623 out=35379 cr=2662219 ratio=11.1x min=93 |
| issues | - |
| commit | 252e062 |

## Acceptance

- [ ] `docs/ux/screens-ai-task-tracker.md` is extended with the S-04 day view screen inventory and the new "Модель" column, including the four states: primary model only, primary + `+N`, tooltip/`aria-label` full ordered list, and "Нет данных".
- [ ] `docs/ux/user-flows-ai-task-tracker.md` is extended with the S-04 day-view journey (open `/calendar/:date` -> view day table -> read the model column) and traces to UC-E2E-005 and `R-015`.
- [ ] The S-04 screen states (success / loading / empty / error) are documented, and the model-cell states reuse the S-01 (S-01 list view) terminology and labels ("Модель", "Нет данных").
- [ ] The screen inventory is consistent with the existing S-01 model-column UX (T-020) so the same cell rules apply in both views; no conflicting state is introduced.
- [ ] The UX doc explicitly states the day view is read-only and the model column does not add new interactions beyond the S-01 tooltip/aria behavior.
- [ ] `CP-1` (UX flows and screens) and `CP-F` (final acceptance) are declared.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-1 | U-01 UX flows and screens | `docs/ux/user-flows-ai-task-tracker.md`, `docs/ux/screens-ai-task-tracker.md` | 9P/0F/0M | approved | docs/checkpoints/T-032-CP-1.md |
| CP-F | final acceptance | all task artifacts | 10P/0F/0M | awaiting_review | docs/checkpoints/T-032-CP-F.md |

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

- 2026-11-19: T-032 registered as a pending U-01 successor of T-030/T-031 (S-04 calendar day model column); launch protocol copied from the T-029 card; no work started.
- 2026-11-19: T-032 started (U-01 UX flows and screens); board set in_progress; base_commit c3a20d6; no role work started.
- 2026-11-20: CP-1 approved (U-01); checks 9 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage in=155299 out=31633 cr=2581050 ratio=16.6x min=81; reviewer auto (P2)
- 252e062: CP-1 snapshot
- 2026-11-20: CP-F awaiting_review (U-01); checks 10 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage in=240623 out=35379 cr=2662219 ratio=11.1x min=93; reviewer user
