# T-031: A-02 E2E use cases and dataset delta for S-04 model column

| Field | Value |
|---|---|
| id | T-031 |
| title | A-02 E2E use cases and dataset delta for S-04 model column |
| workflow | feature |
| stage | A-02 |
| role | analyst |
| status | pending |
| hold | false |
| depends_on | T-030 |
| inputs | `docs/analysis/requirements-ai-task-tracker.md`, `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/analysis/e2e-test-data-ai-task-tracker.yaml`, `docs/tasks/T-030-requirements-s04-model.md`, `src/web/src/views/CalendarDayView.vue` |
| artifacts | `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/analysis/e2e-test-data-ai-task-tracker.yaml` |
| e2e | required - S-04 day view model column is user-visible and must be covered by UC-E2E-005 and the YAML dataset |
| ux | required - the S-04 model cell states (primary, `+N`, tooltip/aria, "Нет данных") are exercised by the e2e scenario |
| chunks | - (to be planned at start) |
| budget | - |
| base_commit | - |
| usage | n/a |
| issues | - |
| commit | - |

## Acceptance

- [ ] `UC-E2E-005` is extended so the S-04 day view (`/calendar/:date`) is asserted to show a "Модель" column after "Задача", with the primary model, `+N` when more than one, the full ordered list in `title`/`aria-label`, and "Нет данных" when `models` is empty.
- [ ] The YAML dataset `e2e-test-data-ai-task-tracker.yaml` is extended with at least one task carrying multiple `models` (to exercise `+N`) and one task with an empty `models` list (to exercise "Нет данных"), for the day view.
- [ ] `UC-E2E-005` traces to `R-015` and reuses the S-01 ordering/tie-break expectation (total tokens desc, name tie-break); no duplicate use case is introduced.
- [ ] The scenario explicitly states the check runs against the actually built frontend (rebuilt image or DOM smoke), not only the API, and does not change the S-01 (UC-E2E-003) scenario.
- [ ] The e2e scenario is validatable by the existing runner/validator and the dataset passes `validate-e2e-data.py`.
- [ ] `CP-2` (e2e use cases and dataset) and `CP-F` (final acceptance) are declared.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-2 | A-02 e2e use cases and dataset | `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/analysis/e2e-test-data-ai-task-tracker.yaml` | - | pending | `docs/checkpoints/T-031-CP-2.md` |
| CP-F | final acceptance | all task artifacts | - | pending | `docs/checkpoints/T-031-CP-F.md` |

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

- 2026-11-19: T-031 registered as a pending A-02 successor of T-030 (S-04 calendar day model column); launch protocol copied from the T-029 card; no work started.
