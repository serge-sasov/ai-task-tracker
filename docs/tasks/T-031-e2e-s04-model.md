# T-031: A-02 E2E use cases and dataset delta for S-04 model column

| Field | Value |
|---|---|
| id | T-031 |
| title | A-02 E2E use cases and dataset delta for S-04 model column |
| workflow | feature |
| stage | A-02 |
| role | analyst |
| status | done |
| hold | false |
| depends_on | T-030 |
| inputs | `docs/analysis/requirements-ai-task-tracker.md`, `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/analysis/e2e-test-data-ai-task-tracker.yaml`, `docs/tasks/T-030-requirements-s04-model.md`, `src/web/src/views/CalendarDayView.vue` |
| artifacts | `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/analysis/e2e-test-data-ai-task-tracker.yaml` |
| e2e | required - S-04 day view model column is user-visible and must be covered by UC-E2E-005 and the YAML dataset |
| ux | required - the S-04 model cell states (primary, `+N`, tooltip/aria, "Нет данных") are exercised by the e2e scenario |
| chunks | - (to be planned at start) |
| budget | - |
| base_commit | 27434c9 |
| usage | in=281084 out=60015 cr=6797571 ratio=24.2x min=282 |
| issues | - |
| commit | f59ae41 |

## Acceptance

- [x] `UC-E2E-005` is extended so the S-04 day view (`/calendar/:date`) is asserted to show a "Модель" column after "Задача", with the primary model, `+N` when more than one, the full ordered list in `title`/`aria-label`, and "Нет данных" when `models` is empty.
- [x] The YAML dataset `e2e-test-data-ai-task-tracker.yaml` is extended with at least one task carrying multiple `models` (to exercise `+N`) and one task with an empty `models` list (to exercise "Нет данных"), for the day view.
- [x] `UC-E2E-005` traces to `R-015` and reuses the S-01 ordering/tie-break expectation (total tokens desc, name tie-break); no duplicate use case is introduced.
- [x] The scenario explicitly states the check runs against the actually built frontend (rebuilt image or DOM smoke), not only the API, and does not change the S-01 (UC-E2E-003) scenario.
- [x] The e2e scenario is validatable by the existing runner/validator and the dataset passes `validate-e2e-data.py`.
- [x] `CP-2` (e2e use cases and dataset) and `CP-F` (final acceptance) are declared.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-2 | A-02 e2e use cases and dataset | `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/analysis/e2e-test-data-ai-task-tracker.yaml` | 9P/0F/0M | approved | docs/checkpoints/T-031-CP-2.md |
| CP-F | final acceptance | all task artifacts | 10P/0F/0M | approved | docs/checkpoints/T-031-CP-F.md |

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
- 2026-11-19 (orchestrator): T-030 leftovers committed as `535558c` (T-030 chunk-ledger residue + ISSUE-20261118-185907 mitigation note). T-031 set `pending -> in_progress` in plan/card; activity re-rendered (T-031 `#FFE082`); start commit `27434c9`. Card `base_commit` = `27434c9` (first commit of the task = diff baseline). No role work started.
- 2026-11-19 (analyst, chunk B1): A-02 markdown delta authored — extended `UC-E2E-005` in `docs/analysis/e2e-use-cases-ai-task-tracker.md` with S-04 «Модель» column assertions (column after «Задача», primary model `qwen3.8:27b` + `+1`, full ordered list in `title`/`aria-label`, «Нет данных» for TASK-002 with empty `models`, ordering/tie-break reused from R-014, DOM-смак check against built frontend not API only); R-015 added to source requirements, coverage summary (32 reqs), and traceability (`test_day_view_model_column`); `UC-E2E-003` (S-01) unchanged. Chunk B1 completed.
- 2026-11-19 (analyst, chunk B2): A-02 YAML delta authored — added `fixtures.calendar.day_2026-11-10-models` (TASK-001 +1, TASK-002 «Нет данных») and `day_2026-11-11-models` (TASK-003/004/005 +1) to `docs/analysis/e2e-test-data-ai-task-tracker.yaml`; extended `UC-E2E-005` entry: `source_requirements` += R-015, steps S6/S7 (day-view model column, DOM check, R-014 ordering rule), `traceability.test_ids` += `test_day_view_model_column`. Both validators pass (harness: 0 errors 0 warnings; project `--strict`: 0 errors 0 warnings). `git diff` limited to T-031 scope; `UC-E2E-003` byte-identical. Chunk B2 completed.
- 2026-11-19 (analyst, housekeeping): `verify-stage A-02` initial run FAILED only on `chunk-budget` because the check measures total produced-file size (43 KB md / 32 KB yaml) — both are pre-existing files edited incrementally (actual new content ~4 KB md / ~2.5 KB yaml, within §11.1 8 KB/write and 20 KB/session). Aligned ledger budget to 48/48 KB per the T-018/T-030 analyst precedent (budget is a planning field, not a machine field). Re-run: 9 PASS / 0 FAIL / 0 MANUAL / 1 N/A (`artifact-hashes` N/A = no evidence ledger yet, written by `stage-done.ps1` at CP-2).
- 2026-11-19: CP-2 approved (A-02); checks 9 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage in=280761 out=59856 cr=6740113 ratio=24x min=282; reviewer auto (P2)
- 76e53b1: CP-2 snapshot
- 2026-11-19: CP-F awaiting_review (A-02); checks 10 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage in=281084 out=60015 cr=6797571 ratio=24.2x min=282; reviewer user
- f59ae41: CP-F snapshot
- 2026-11-20: CP-F approved by user (chat: "approve T-031"), task closed as done; checks 10 PASS / 0 FAIL / 0 MANUAL / 0 UNCHECKED; snapshot f59ae41.
