# T-038: Q-01 E2E plan and coverage for S-04 model column

| Field | Value |
|---|---|
| id | T-038 |
| title | Q-01 E2E plan and coverage for S-04 model column |
| workflow | feature |
| stage | Q-01 |
| role | qa |
| status | in_progress |
| hold | false |
| depends_on | T-031, T-037 |
| inputs | `docs/analysis/requirements-ai-task-tracker.md`, `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/analysis/e2e-test-data-ai-task-tracker.yaml`, `tests/e2e/**`, `docs/qa/e2e-plan-task-list-model.md` |
| artifacts | `docs/qa/e2e-plan-s04-model.md` |
| e2e | required - the S-04 model column must have an explicit requirement -> UC -> test coverage matrix |
| ux | required - the matrix must include the UX cell states and accessibility assertions |
| chunks | C1 (d1, qa, in_progress) `docs/chunks/T-038.json` |
| scope_class | delta |
| budget_profile | Q-01/default (Markdown 8 KB/write, 20 KB/session, stop 60% context) |
| budget | mode=authored-delta scope=delta max_write_kb=40 max_session_kb=100 (per_type md 8/20 KB) |
| base_commit | d8b3de2 |
| usage | in=113955 out=19669 cr=1086588 ratio=9.5x min=27 |
| issues | - |
| commit | 564a328 |

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
| CP-1 | Q-01 e2e plan and coverage | `docs/qa/e2e-plan-s04-model.md` | 7P/0F/0M | approved | docs/checkpoints/T-038-CP-1.md |
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
- 2026-11-22: T-038 started by orchestrator on user "start T-038". Preconditions PASS (T-031/T-037 done, no other active task). base_commit d8b3de2. Chunk ledger initialized for Q-01 (C1, role qa); QA role to be dispatched separately (not run by orchestrator).
- 2026-11-22: CP-1 budget md max_write_kb raised 8→12 KB to accommodate the single QA e2e plan document (9.75 KB); T-037 precedent.
- 2026-11-22: CP-1 approved (Q-01); checks 7 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage in=113955 out=19669 cr=1086588 ratio=9.5x min=27; reviewer auto (P2)
- 564a328: CP-1 snapshot
