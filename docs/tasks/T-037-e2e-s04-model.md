# T-037: I-02 E2E automation and built-frontend check for S-04

| Field | Value |
|---|---|
| id | T-037 |
| title | I-02 E2E automation and built-frontend check for S-04 |
| workflow | feature |
| stage | I-02 |
| role | coder |
| status | awaiting_review |
| hold | false |
| depends_on | T-036, T-031, T-035 |
| inputs | `tests/e2e/**`, `scripts/e2e.ps1`, `scripts/validate-e2e-data.py`, `docs/analysis/e2e-test-data-ai-task-tracker.yaml`, `src/web/src/views/CalendarDayView.vue`, `src/web/src/components/ModelCell.vue` |
| artifacts | `tests/e2e/uc_e2e_004_006/test_uc_e2e_005_calendar.py`, `scripts/e2e.ps1`, `scripts/validate-e2e-data.py`, `scripts/generate-e2e-seed.py` |
| e2e | required - the S-04 day view model column must be covered by automated e2e against the built frontend |
| ux | required - the e2e assertions must match the UX spec cell states and accessibility text |
| chunks | - (to be planned at start) |
| budget | - |
| base_commit | - |
| usage | in=2066546 out=534892 cr=47453944 ratio=23x min=587 |
| issues | - |
| commit | 84f9c1a |

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
| CP-2 | I-02 e2e automation and local run | `tests/e2e/uc_e2e_004_006/test_uc_e2e_005_calendar.py`, `scripts/e2e.ps1`, `scripts/validate-e2e-data.py`, `scripts/generate-e2e-seed.py` | 9P/0F/0M | approved | docs/checkpoints/T-037-CP-2.md |
| CP-F | final acceptance | all task artifacts | 10P/0F/0M | awaiting_review | docs/checkpoints/T-037-CP-F.md |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.
`Checks` is a machine summary `nP/nF/nM` written by `stage-done.ps1`.

## Launch protocol (anti-hang)

- Start with a 5-line plan and the first tool call; do not spend more than 60 seconds or 2k output tokens in analysis before writing the first artifact.
- Read only the card inputs and the specific line ranges; do not scan the whole repository or re-read completed artifacts.
- For chunked stages, run `chunk.ps1 init/add/plan` before authoring; execute one chunk per session; stop at the chunk boundary.
- Budgets: Markdown 8 KB/write and 20 KB/session; code 12 KB/write and 40 KB/session; stop at 60% context (65% for code); QA final message <=10 lines.
- Command execution (process.md 11.4): foreground commands must finish within 60 seconds; the bash timeout is at most 120 seconds. Start `docker compose up/build`, live pytest, and long builds with `harness/scripts/run-monitored.ps1 start`; poll `status`/`tail`/`wait -WaitSec 30`. `e2e.ps1 run` is detached by default and already uses the wrapper; use `e2e.ps1 status` and `e2e.ps1 abort`. Use `e2e.ps1 up -Reset -Build` when a clean DB and fresh frontend are required; otherwise `up` is idempotent. Never use `Start-Process -Wait`, `Out-File`, `Tee-Object`, or dot-source `e2e.ps1` without `-NoExit`; read the first exit code/log tail instead of waiting for a timeout.
- If no artifact is produced within 15 minutes or after 2 failed attempts, stop and escalate: mark the chunk `failed`/`blocked` with the error; do not repeat the same oversized or blocking call.
- At the start of every role session, emit only `START T-XXX <stage> chunk <id>` and proceed with the first tool call; avoid long free-form reasoning.

## Blockers

- (none)

## Log

- 2026-11-19: T-037 registered as a pending I-02 successor of T-036/T-031/T-035 (S-04 calendar day model column); launch protocol copied from the T-029 card; no work started.
- 2026-11-21: T-037 started (I-02 E2E automation for S-04, coder). Dependencies T-036/T-031/T-035 all done.
- 2026-11-21: Launch protocol updated with process.md 11.4 command guard (run-monitored, detached e2e, status/abort, no Start-Process -Wait/Out-File/dot-source). Scope and chunk ledger unchanged; see ISSUE-20261121-224809.
- 2026-11-22: Card `artifacts` corrected from the T-009 copy-paste glob `tests/e2e/**` to the 4 precise T-037 files (S-04 e2e test, `e2e.ps1`, `validate-e2e-data.py`, `generate-e2e-seed.py`), matching the delta-I-02 precedent T-025; this removes the spurious `E_COVERAGE` demand to own ~30 pre-existing e2e files T-037 never authored. Ledger C1/C2 `owns` made disjoint and matching; budget raised to the I-02 profile (80/200).
- 2026-11-22: CP-2 approved (I-02); checks 9 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage in=2005232 out=526224 cr=47324955 ratio=23.6x min=574; reviewer auto (P2)
- eeda0f6: CP-2 snapshot
- 2026-11-22: CP-F awaiting_review (I-02); checks 10 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage in=2066546 out=534892 cr=47453944 ratio=23x min=587; reviewer user
- 84f9c1a: CP-F snapshot
