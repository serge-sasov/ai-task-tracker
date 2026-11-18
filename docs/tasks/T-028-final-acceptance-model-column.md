# T-028: F-01 Final acceptance and closure: S-01 model column

| Field | Value |
|---|---|
| id | T-028 |
| title | F-01 Final acceptance and closure: S-01 model column |
| workflow | feature |
| stage | F-01 |
| role | orchestrator + lead |
| status | awaiting_review |
| hold | false |
| depends_on | T-027 |
| inputs | `docs/tasks/T-017-task-list-model-planning.md`, `docs/tasks/T-018-requirements-model-column.md`, `docs/tasks/T-019-e2e-model-column.md`, `docs/tasks/T-020-ux-model-column.md`, `docs/tasks/T-021-prototype-model-column.md`, `docs/tasks/T-022-architecture-model-column.md`, `docs/tasks/T-023-contracts-model-column.md`, `docs/tasks/T-024-implement-model-column.md`, `docs/tasks/T-025-e2e-model-column.md`, `docs/tasks/T-026-e2e-plan-model-column.md`, `docs/tasks/T-027-qa-model-column.md`, `docs/checkpoints/T-027-CP-2.md`, `docs/checkpoints/T-027-CP-F.md`, `docs/qa/review-T-027.md`, `docs/qa/e2e-report-task-list-model.md`, `docs/bugs/BUG-007-pre-existing-backend-test-failures.md`, `docs/bugs/BUG-008-uc-e2e-004-ai-search-failures.md`, `docs/issues/ISSUE-20261118-153622-e2e-runner-cannot-start-the-local-stack-and-invokes-pytest-w.md`, `docs/issues/ISSUE-20261118-185907-harness-qa-subagent-sse-read-timeout-during-t-026.md` |
| artifacts | `docs/plan.md`, `docs/tasks/T-028-final-acceptance-model-column.md`, `docs/activity.puml`, `docs/activity.svg` |
| e2e | required - the S-01 model column is a new user-visible behavior backed by the `GET /api/tasks` `models` field; the feature graph already includes I-02/Q-01/Q-02 e2e, and F-01 verifies that the e2e evidence and verdict are consistent and final |
| ux | required - the S-01 model column is a user-facing UI change (column, `+N`, tooltip/aria, «Нет данных»); the feature graph already includes U-01/U-02, and F-01 verifies the UX/prototype/UI-spec evidence is consistent and final |
| chunks | `docs/chunks/T-028.json` (single source of truth for per-chunk state, `owns`, and usage); C1: final acceptance/closure; the plan is written with `harness/scripts/chunk.ps1` before authoring |
| budget | `max_write_kb=16` / `max_session_kb=32` (ledger `budget` object holds the effective values); C1 is a board/card/activity reconciliation, well under budget |
| base_commit | b4cbe04 |
| usage | n/a |
| issues | - |
| commit | - |

## Acceptance

- [ ] All feature tasks for the S-01 model column (T-018 A-01, T-019 A-02, T-020 U-01, T-021 U-02, T-022 R-01, T-023 R-02, T-024 I-01, T-025 I-02, T-026 Q-01, T-027 Q-02) are `done`, and T-027 is the last working-role task.
- [ ] Board (`docs/plan.md`), cards, checkpoints, and the activity diagram are mutually consistent: every T-018..T-027 row is `done`, each has an approved `CP-F`, and the activity nodes are green `#90EE90`.
- [ ] R-014 (S-01 model column) traceability is complete and consistent across requirements, e2e use cases, UX flows/screens/UI-spec/prototype, architecture/contracts/OpenAPI, implementation, tests, and QA evidence.
- [ ] D-01 is explicitly waived for this feature (recorded at T-017 P-00) and the waiver reason is preserved.
- [ ] BUG-007 (pre-existing backend unit-test failures) and BUG-008 (UC-E2E-004 AI-search e2e failures) are tracked as separate `open` follow-ups in `docs/bugs/` and are NOT absorbed by, nor closed as part of, this feature.
- [ ] `CP-F` is declared and its `final-traceability-manual` check is presented to the user for the final semantic acceptance decision.
- [ ] No second active task exists; T-028 is the only `in_progress`/`awaiting_review` row on the board.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-F | final acceptance and closure | `docs/plan.md`, `docs/tasks/T-028-final-acceptance-model-column.md`, `docs/activity.puml`, `docs/activity.svg` | 3P/1F/1M | awaiting_review | docs/checkpoints/T-028-CP-F.md |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.
`Checks` is a machine summary `nP/nF/nM` written by `stage-done.ps1`. F-01 has no intermediate working checkpoint; its only checkpoint is `CP-F`, and `final-traceability-manual` is a manual check that always waits for the user.

## Blockers

- (none)

## Log

- 2026-11-18: T-028 started on base `b4cbe04`; T-027 (Q-02) is `done`. C1 (final acceptance/closure) created `pending` in `docs/chunks/T-028.json` (owns = the four F-01 card artifacts, within the orchestrator write area; budget 16/32). `hold: false`, but CP-F always waits for the user because of `final-traceability-manual`. BUG-007 and BUG-008 remain `open` follow-ups, not absorbed.
- 2026-11-18: CP-F awaiting_review (F-01); checks 3 PASS/1 FAIL/1 MANUAL/0 UNCHECKED; usage n/a; reviewer user
