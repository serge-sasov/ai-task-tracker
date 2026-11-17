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
| status | awaiting_review |
| requested_at | 2026-11-17T15:41:17 |
| reviewed_at | - |
| reviewer | user |
| decision | - |
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
| 29 | 4533741 | 1135839 | 194612 | 108249337 | 23.9x | 2654 | 1.19 |

## Diff stat

```
docs/bugs/BUG-002-dockerfile-source-paths.md       |   5 +-
 docs/bugs/BUG-003-local-stack-not-healthy.md       | 141 +++++++++++++++++++++
 docs/bugs/README.md                                |   3 +-
 docs/checkpoints/T-009-CP-2.md                     |   2 +-
 docs/chunks/T-009/C1.brief.md                      |  45 +++++++
 .../ISSUE-20261117-131059-C1-conftest-destroyed.md |  57 +++++++++
 docs/plan.md                                       |   2 +-
 docs/tasks/T-009-e2e-automation.md                 |   3 +-
 8 files changed, 252 insertions(+), 6 deletions(-)
```

## User remarks

- (user fills; may be empty if artifacts were edited directly)

## User edits

- none

## Role response

- Machine checks: 0 FAIL, 0 UNCHECKED, 0 MANUAL.

## Next action

- return to this checkpoint