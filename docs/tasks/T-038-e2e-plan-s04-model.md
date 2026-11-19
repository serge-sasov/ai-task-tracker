# T-038: Q-01 E2E plan and coverage for S-04 model column

| Field | Value |
|---|---|
| id | T-038 |
| title | Q-01 E2E plan and coverage for S-04 model column |
| workflow | feature |
| stage | Q-01 |
| role | qa |
| status | pending |
| hold | false |
| depends_on | T-031, T-037 |
| inputs | `docs/analysis/requirements-ai-task-tracker.md`, `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/analysis/e2e-test-data-ai-task-tracker.yaml`, `tests/e2e/**`, `docs/qa/e2e-plan-task-list-model.md` |
| artifacts | `docs/qa/e2e-plan-s04-model.md` |
| e2e | required - the S-04 model column must have an explicit requirement -> UC -> test coverage matrix |
| ux | required - the matrix must include the UX cell states and accessibility assertions |
| chunks | - (to be planned at start) |
| budget | - |
| base_commit | - |
| usage | n/a |
| issues | - |
| commit | - |

## Acceptance

- [ ] `docs/qa/e2e-plan-s04-model.md` contains a requirement -> use case -> test matrix covering `R-015`, `UC-E2E-005`, and the S-04 e2e test(s), with the expected result for each cell state (primary, `+N`, full list, "Нет данных").
- [ ] The plan states the test data is validated by `validate-e2e-data.py` and includes the multi-model and empty-model day-view tasks.
- [ ] The plan states the execution approach: the check runs against the actually built frontend (rebuilt image or DOM smoke), and the stale-frontend failure mode from T-027/T-028 is a covered risk.
- [ ] The plan traces every `R-015` acceptance item to at least one test and lists any gap as an open item with a reason (no silent skip).
- [ ] The plan reuses the S-01 (T-026) plan structure and does not duplicate the S-01 coverage; the S-04 scope is the delta.
- [ ] `CP-1` (e2e plan and coverage) and `CP-F` (final acceptance) are declared.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-1 | Q-01 e2e plan and coverage | `docs/qa/e2e-plan-s04-model.md` | - | pending | `docs/checkpoints/T-038-CP-1.md` |
| CP-F | final acceptance | all task artifacts | - | pending | `docs/checkpoints/T-038-CP-F.md` |

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

- 2026-11-19: T-038 registered as a pending Q-01 successor of T-031/T-037 (S-04 calendar day model column); launch protocol copied from the T-029 card; no work started.
