# T-026 CP-F: Q-01 E2E plan and coverage: S-01 model column

| Field | Value |
|---|---|
| task | T-026 |
| workflow | feature |
| bug | - |
| checkpoint | CP-F |
| role | qa |
| stage | Q-01 |
| artifacts | docs/qa/e2e-plan-task-list-model.md |
| status | approved |
| requested_at | 2026-11-18T19:08:41 |
| reviewed_at | 2026-11-18T19:08:41 |
| reviewer | auto (explicit -AutoApprove) |
| decision | approved |
| snapshot_commit | 85c202e |
| generated_by | stage-done/1 |
| checks_file | docs/checkpoints/T-026-CP-F.checks.json |

## Review scope

- Stage `Q-01` acceptance for T-026.
- The plan defines a requirement -> UC -> test matrix that covers ALL `UC-E2E-001`..`UC-E2E-008` (the `e2e-plan-uc-coverage` registry check requires every documented UC id to be present). (matrix rows span UC-E2E-001..008).
- The matrix includes a focused UC-E2E-003 S8 / R-014 block covering the S-01 model column: per-task ordered `models` list (`qwen3.8:27b` then `llama3.1:8b`, total desc then name asc), empty state for TASK-002 (no usage -> «Нет данных»), `+N`, tooltip/aria full list, and API `GET /api/tasks` field. (§3 S8-API-1..8 / S8-UI-1..5).
- Test data validation is described: how `scripts/validate-e2e-data.py --strict` is run against `docs/analysis/e2e-test-data-ai-task-tracker.yaml` and the pass criteria (0 errors). (§4; validator: 8 use cases, 25 requirement links, 0 errors, 0 warnings).
- The execution approach covers both a targeted UC run (e.g. `scripts/e2e.ps1 run -UC UC-E2E-003`) and the full suite, with the offline/skipped behavior noted. (§5).
- An environment caveat is recorded for ISSUE-20261118-153622 (Docker Compose plugin missing + runner pytest backslash path) and the workaround (direct forward-slash pytest invocation). (§5 Environment caveat).
- Entry/exit criteria and evidence expectations are stated (which `tests/e2e/results/*.json` and log lines constitute pass evidence; FAIL/UNCHECKED must be 0). (§6).
- Chunk C1 is `done` with a produced hash in `docs/chunks/T-026.json`. (C1 `done`, sha256 in ledger).
- `CP-1` (e2e plan and coverage) and `CP-F` (final acceptance) are declared.

## Chunks

| Chunk | State | Depth | Role | Produced | Title |
|---|---|---|---|---|---|
| C1 | done | d1 | qa | 1 | E2E plan + requirement->UC->test matrix (all UC-E2E-001..008, S-01/R-014 focus) |

Ledger: `docs/chunks/T-026.json`

## Checks

| ID | Check | Result | Detail |
|---|---|---|---|
| artifacts-exist | file-exists | PASS | 1 file(s) |
| artifact-hashes | hash-match | PASS | 1 artifact(s) match ledger |
| e2e-plan-exist | file-exists | PASS | 1 file(s) |
| e2e-plan-matrix | pattern-present | PASS | 2 pattern(s) in 1 file(s) |
| e2e-plan-uc-coverage | id-coverage | PASS | 8 id(s) covered by 1 file(s) |
| chunk-ownership-no-overlap | chunk-ledger | PASS | 1 chunk(s), rule ownership |
| chunk-coverage | chunk-ledger | PASS | 1 chunk(s), rule coverage |
| chunk-budget | chunk-ledger | PASS | 1 chunk(s) within 32/64 KB |

Summary: 8 PASS, 0 FAIL, 0 MANUAL, 0 N/A, 0 UNCHECKED.

## Usage

| Sessions | Input | Output | Reasoning | Cache read | Cache/Input | Minutes | Cost |
|---|---|---|---|---|---|---|---|
| 1 | 60511 | 9996 | 0 | 1620588 | 26.8x | 103 | 0.00 |

## Diff stat

```
docs/activity.puml                                 |   2 +-
 docs/activity.svg                                  |   2 +-
 docs/checkpoints/T-026-CP-1.checks.json            |  68 ++++++++++
 docs/checkpoints/T-026-CP-1.md                     |  89 +++++++++++++
 docs/chunks/T-026.json                             |  64 +++++++++
 docs/chunks/T-026/C1.brief.md                      |  46 +++++++
 docs/evidence/T-026.md                             |   7 +
 ...ss-qa-subagent-sse-read-timeout-during-t-026.md |  57 ++++++++
 docs/issues/README.md                              |   1 +
 docs/plan.md                                       |   2 +-
 docs/qa/e2e-plan-task-list-model.md                | 145 +++++++++++++++++++++
 docs/tasks/T-026-e2e-plan-model-column.md          |  55 ++++++++
 12 files changed, 535 insertions(+), 3 deletions(-)
```

## User remarks

- (user fills; may be empty if artifacts were edited directly)

## User edits

- none

## Role response

- Machine checks: 0 FAIL, 0 UNCHECKED, 0 MANUAL.

## Next action

- accept task