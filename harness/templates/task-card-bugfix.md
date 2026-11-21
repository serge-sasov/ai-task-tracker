# T-XXX: Fix BUG-XXX - short title

| Field | Value |
|---|---|
| id | T-XXX |
| title | Full task title |
| workflow | bugfix |
| stage | B-01 / B-02 / B-03 / B-04 / B-05 |
| role | coder / qa / analyst / architect / ux-designer / doc-writer / orchestrator |
| status | pending |
| hold | false (true forces human review at every checkpoint) |
| manual_carryover | false (true carries CP-2 manual items to CP-F only when artifacts are byte-identical; CP-F is still user-reviewed) |
| depends_on | T-YYY, T-ZZZ |
| bug | BUG-XXX |
| severity | P0 / P1 / P2 / P3 |
| profile | - / hotfix (P0/P1 only) |
| scope_class | delta / cross-cutting |
| budget_profile | `default` / `R-01/default` / ... (from `harness/budgets.json`) |
| inputs | bug card, affected code/tests, requirements/UC |
| artifacts | regression test, code/tests, `docs/qa/review-T-XXX.md` |
| e2e | required / not applicable (reason) |
| ux | required / not applicable (reason) |
| chunks | ledger path and granularity: `docs/chunks/T-XXX.json` (single source of truth for per-chunk state, `owns`, and usage); use it for large reproduction, fix, or test work |
| budget | `budget_profile` selects per-type + authored-delta limits from `harness/budgets.json`; the ledger `budget` object holds the effective values |
| base_commit | commit hash the first checkpoint diff is measured from |
| usage | measured token usage (filled by stage-done) |
| issues | ISSUE-... (links to docs/issues/ if problems occurred) |
| commit | latest commit hash (snapshot or closure) |

## Bug summary

- Observed:
- Expected:
- Reproduction:
- Root cause: (filled by coder at CP-1)

## Acceptance

- [ ] The regression test fails on the base revision.
- [ ] The root cause is documented.
- [ ] The minimal fix passes the regression test.
- [ ] Relevant existing tests pass.
- [ ] No unrelated behavior or files changed.
- [ ] QA verifies fail-before/pass-after independently.

## Launch protocol (anti-hang)

- Start with a 5-line plan and the first tool call; do not spend more than 60 seconds or 2k output tokens in analysis before writing the first artifact.
- Foreground commands must finish within 60 seconds; the bash timeout is at most 120 seconds.
- Start `docker compose up/build`, live pytest, and long builds with `harness/scripts/run-monitored.ps1 start`; poll `status`/`tail`/`wait -WaitSec 30`. The project e2e runner `run` is detached by default and already uses the wrapper; use its `status`/`abort`.
- Never use `Start-Process -Wait`, `Out-File`, `Tee-Object`, or dot-source a dispatching script without `-NoExit`; react to the first exit code/log tail, not to a timeout.
- If no artifact is produced within 15 minutes or after 2 failed attempts, stop and mark the chunk `failed`/`blocked` with the error; do not repeat the same blocking command.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-1 | reproduction/root cause | failing test + card log | - | pending | `docs/checkpoints/T-XXX-CP-1.md` |
| CP-2 | fix/regression | code + tests | - | pending | `docs/checkpoints/T-XXX-CP-2.md` |
| CP-F | final acceptance | all task artifacts | - | pending | `docs/checkpoints/T-XXX-CP-F.md` |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`,
`rejected`.
`Checks` is a machine summary `nP/nF/nM` written by `stage-done.ps1`.

## Blockers

- (filled on status=failed|blocked: cause, output, what is needed to unblock)

## Log

- YYYY-MM-DD: reproduction evidence, root cause, fix, tests, checkpoint commits, user decisions.
