# T-037: I-02 E2E automation and built-frontend check for S-04

| Field | Value |
|---|---|
| id | T-037 |
| title | I-02 E2E automation and built-frontend check for S-04 |
| workflow | feature |
| stage | I-02 |
| role | coder |
| status | pending |
| hold | false |
| depends_on | T-036, T-031, T-035 |
| inputs | `tests/e2e/**`, `scripts/e2e.ps1`, `scripts/validate-e2e-data.py`, `docs/analysis/e2e-test-data-ai-task-tracker.yaml`, `src/web/src/views/CalendarDayView.vue`, `src/web/src/components/ModelCell.vue` |
| artifacts | `tests/e2e/**`, `scripts/e2e.ps1`, `scripts/validate-e2e-data.py` |
| e2e | required - the S-04 day view model column must be covered by automated e2e against the built frontend |
| ux | required - the e2e assertions must match the UX spec cell states and accessibility text |
| chunks | - (to be planned at start) |
| budget | - |
| base_commit | - |
| usage | n/a |
| issues | - |
| commit | - |

## Acceptance

- [ ] An e2e test for `UC-E2E-005` is added that asserts the S-04 day view (`/calendar/:date`) shows the "Модель" column with the primary model, `+N` when more than one, the full ordered list in `title`/`aria-label`, and "Нет данных" when `models` is empty.
- [ ] The e2e test verifies the actually built frontend (rebuilt image or DOM smoke), not only the API; the stale-frontend failure mode from T-027/T-028 is explicitly guarded (the test fails if the built artifact lacks the column).
- [ ] The e2e dataset and validator are updated so the multi-model and empty-model day-view tasks are present and validated (`validate-e2e-data.py` passes).
- [ ] The S-01 (UC-E2E-003) e2e test remains green; no existing e2e test is broken by the S-04 addition.
- [ ] The e2e runner (`scripts/e2e.ps1`) executes the new S-04 test in an isolated environment with cleanup and does not depend on production.
- [ ] `CP-2` (e2e automation and local run) and `CP-F` (final acceptance) are declared.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-2 | I-02 e2e automation and local run | `tests/e2e/**`, `scripts/e2e.ps1`, `scripts/validate-e2e-data.py` | - | pending | `docs/checkpoints/T-037-CP-2.md` |
| CP-F | final acceptance | all task artifacts | - | pending | `docs/checkpoints/T-037-CP-F.md` |

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

- 2026-11-19: T-037 registered as a pending I-02 successor of T-036/T-031/T-035 (S-04 calendar day model column); launch protocol copied from the T-029 card; no work started.
