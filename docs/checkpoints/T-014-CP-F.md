# T-014 CP-F: Verify BUG-002 - Dockerfile source-path fix (B-03)

| Field | Value |
|---|---|
| task | T-014 |
| workflow | bugfix |
| bug | BUG-002 |
| checkpoint | CP-F |
| role | qa |
| stage | B-03 |
| artifacts | docs/qa/review-T-014.md |
| status | awaiting_review |
| requested_at | 2026-11-17T15:39:47 |
| reviewed_at | - |
| reviewer | user |
| decision | - |
| snapshot_commit | 01a6f14 |
| generated_by | stage-done/1 |
| checks_file | docs/checkpoints/T-014-CP-F.checks.json |

## Review scope

- Stage `B-03` acceptance for T-014.
- Independent fail-before evidence: base revision (90c22f7) backend build fails.
- Independent pass-after evidence: after T-013 fix, `docker compose up --build` succeeds and the backend jar is present.
- Regression test `tests/regression/test_dockerfile_source_paths.py` passes.
- No collateral changes (only `Dockerfile` and the regression test touched).
- Verdict recorded in `docs/qa/review-T-014.md`.

## Checks

| ID | Check | Result | Detail |
|---|---|---|---|
| artifacts-exist | file-exists | PASS | 1 file(s) |
| artifact-hashes | hash-match | NOT_APPLICABLE | no ledger docs/evidence/T-014.md |
| qa-review-exist | file-exists | PASS | 2 file(s) |
| qa-verdict | pattern-present | PASS | 1 pattern(s) in 1 file(s) |
| qa-fail-before-pass-after | pattern-present | PASS | 2 pattern(s) in 1 file(s) |
| chunk-ownership-no-overlap | chunk-ledger | NOT_APPLICABLE | no ledger docs/chunks/T-014.json (legacy task) |
| chunk-coverage | chunk-ledger | NOT_APPLICABLE | no ledger docs/chunks/T-014.json (legacy task) |
| chunk-budget | chunk-ledger | NOT_APPLICABLE | no ledger docs/chunks/T-014.json (legacy task) |

Summary: 4 PASS, 0 FAIL, 0 MANUAL, 4 N/A, 0 UNCHECKED.

## Usage

- token usage not available (opencode CLI or database unavailable)

## Diff stat

```
Dockerfile                                         |  10 +-
 backend/pom.xml                                    |   4 +
 docker-compose.yml                                 |   4 +-
 docs/activity.puml                                 |   6 +-
 docs/activity.svg                                  |   2 +-
 docs/bugs/BUG-002-dockerfile-source-paths.md       | 119 +++++++++++++++
 docs/bugs/README.md                                |   1 +
 docs/checkpoints/T-013-CP-1.checks.json            |  62 ++++++++
 docs/checkpoints/T-013-CP-1.md                     |  85 +++++++++++
 docs/checkpoints/T-013-CP-2.checks.json            |  74 +++++++++
 docs/checkpoints/T-013-CP-2.md                     |  94 ++++++++++++
 docs/checkpoints/T-013-CP-F.checks.json            |  74 +++++++++
 docs/checkpoints/T-013-CP-F.md                     | 121 +++++++++++++++
 docs/checkpoints/T-015-CP-1.checks.json            |  74 +++++++++
 docs/checkpoints/T-015-CP-1.md                     |  96 ++++++++++++
 docs/checkpoints/T-015-CP-2.checks.json            |  74 +++++++++
 docs/checkpoints/T-015-CP-2.md                     | 112 ++++++++++++++
 docs/checkpoints/T-016-CP-1.checks.json            |  68 +++++++++
 docs/checkpoints/T-016-CP-1.md                     | 104 +++++++++++++
 docs/checkpoints/T-016-CP-F.checks.json            |  68 +++++++++
 docs/checkpoints/T-016-CP-F.md                     | 109 +++++++++++++
 docs/chunks/T-009.json                             | 148 +++++++++++++-----
 docs/chunks/T-013.json                             |  67 ++++++++
 docs/chunks/T-015.json                             | 109 +++++++++++++
 docs/evidence/T-013.md                             |  13 ++
 docs/evidence/T-015.md                             | 170 +++++++++++++++++++++
 docs/evidence/T-016.md                             |   8 +
 docs/plan.md                                       |  11 +-
 docs/qa/review-T-016.md                            |  72 +++++++++
 docs/tasks/T-009-e2e-automation.md                 |   2 +
 .../T-013-fix-bug-002-dockerfile-source-paths.md   |  69 +++++++++
 docs/tasks/T-014-verify-bug-002-dockerfile.md      |  58 +++++++
 docs/tasks/T-015-fix-bug-003-local-stack.md        |  71 +++++++++
 docs/tasks/T-016-verify-bug-003-local-stack.md     |  66 ++++++++
 .../api/search/exception/EmptyQueryException.java  |   2 +-
 .../api/tokens/service/TokenService.java           |  43 ++++--
 .../config/IngestAutoConfiguration.java            |   9 ++
 .../aitasktracker/domain/TaskStatusConverter.java  |  25 +++
 .../ru/aitasktracker/domain/entity/TaskEntity.java |   9 +-
 .../domain/repository/TaskRepository.java          |  68 +++++----
 .../aitasktracker/ingestion/IngestScheduler.java   |   4 +-
 src/main/resources/application.yml                 |   8 +-
 tests/regression/test_dockerfile_source_paths.py   |  99 ++++++++++++
 tests/regression/test_stack_health_prereqs.py      |  47 ++++++
 44 files changed, 2437 insertions(+), 102 deletions(-)
```

## User remarks

- (user fills; may be empty if artifacts were edited directly)

## User edits

- none

## Role response

- Machine checks: 0 FAIL, 0 UNCHECKED, 0 MANUAL.

## Next action

- return to this checkpoint