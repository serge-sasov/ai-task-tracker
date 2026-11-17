# T-009 CP-F: I-02 E2E automation and runner

| Field | Value |
|---|---|
| task | T-009 |
| workflow | feature |
| bug | - |
| checkpoint | CP-F |
| role | coder |
| stage | I-02 |
| artifacts | tests/e2e/**, scripts/e2e.ps1, scripts/validate-e2e-data.py |
| status | approved |
| requested_at | 2026-11-17T15:44:01 |
| reviewed_at | 2026-11-17T15:44:01 |
| reviewer | auto (explicit -AutoApprove) |
| decision | approved |
| snapshot_commit | - |
| generated_by | stage-done/1 |
| checks_file | docs/checkpoints/T-009-CP-F.checks.json |

## Review scope

- Stage `I-02` acceptance for T-009.
- Tests are generated from the YAML dataset and run against an isolated local/staging stack only.
- The runner emits machine-readable results per `UC-E2E-NNN` and cleans up test data.
- No chunk reads more than its own UC batch and the runner API.

## Chunks

| Chunk | State | Depth | Role | Produced | Title |
|---|---|---|---|---|---|
| C1 | done | d1 | coder | 3 | Runner skeleton and result schema |
| C2 | done | d1 | coder | 1 | Dataset validator |
| C3 | done | d1 | coder | 6 | Use cases UC-E2E-001..003 |
| C4 | done | d1 | coder | 4 | Use cases UC-E2E-004..006 |
| C5 | done | d1 | coder | 4 | Use cases UC-E2E-007..008 |
| C6 | done | d1 | coder | 3 | Fixtures, isolation, cleanup |
| C7 | done | d1 | coder | 9 | Full run and result capture |

Ledger: `docs/chunks/T-009.json`

## Checks

| ID | Check | Result | Detail |
|---|---|---|---|
| artifacts-exist | file-exists | PASS | 30 file(s) |
| artifact-hashes | hash-match | PASS | 30 artifact(s) match ledger |
| e2e-tests-exist | file-exists | PASS | 26 file(s) |
| e2e-runner-exist | file-exists | PASS | 1 file(s) |
| e2e-validator-exist | file-exists | PASS | 1 file(s) |
| e2e-cleanup | pattern-present | PASS | 1 pattern(s) in 26 file(s) |
| e2e-no-production | pattern-absent | PASS | 2 pattern(s) absent in 26 file(s) |
| chunk-ownership-no-overlap | chunk-ledger | PASS | 7 chunk(s), rule ownership |
| chunk-coverage | chunk-ledger | PASS | 7 chunk(s), rule coverage |
| chunk-budget | chunk-ledger | PASS | 7 chunk(s) within 24/80 KB |

Summary: 10 PASS, 0 FAIL, 0 MANUAL, 0 N/A, 0 UNCHECKED.

## Usage

| Sessions | Input | Output | Reasoning | Cache read | Cache/Input | Minutes | Cost |
|---|---|---|---|---|---|---|---|
| 29 | 4539299 | 1137324 | 197017 | 110482991 | 24.3x | 2657 | 1.21 |

## Diff stat

```
docs/activity.puml                            |   4 +-
 docs/activity.svg                             |   2 +-
 docs/checkpoints/T-009-CP-F.md                |   2 +-
 docs/checkpoints/T-014-CP-1.checks.json       |  68 +++++++++++
 docs/checkpoints/T-014-CP-1.md                | 157 ++++++++++++++++++++++++
 docs/checkpoints/T-015-CP-F.checks.json       |  74 +++++++++++
 docs/checkpoints/T-015-CP-F.md                | 169 ++++++++++++++++++++++++++
 docs/evidence/T-014.md                        |   1 +
 docs/evidence/T-015.md                        |  82 +++++++++++++
 docs/plan.md                                  |   6 +-
 docs/tasks/T-009-e2e-automation.md            |   3 +-
 docs/tasks/T-014-verify-bug-002-dockerfile.md |   8 +-
 docs/tasks/T-015-fix-bug-003-local-stack.md   |   8 +-
 13 files changed, 570 insertions(+), 14 deletions(-)
```

## User remarks

- (user fills; may be empty if artifacts were edited directly)

## User edits

- none

## Role response

- Machine checks: 0 FAIL, 0 UNCHECKED, 0 MANUAL.

## Next action

- accept task