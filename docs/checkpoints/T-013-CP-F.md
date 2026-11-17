# T-013 CP-F: Fix BUG-002 - Dockerfile source-path mismatch (B-01/B-02)

| Field | Value |
|---|---|
| task | T-013 |
| workflow | bugfix |
| bug | BUG-002 |
| checkpoint | CP-F |
| role | coder |
| stage | B-02 |
| artifacts | Dockerfile, tests/regression/** |
| status | awaiting_review |
| requested_at | 2026-11-17T15:39:44 |
| reviewed_at | - |
| reviewer | user |
| decision | - |
| snapshot_commit | e2c518e |
| generated_by | stage-done/1 |
| checks_file | docs/checkpoints/T-013-CP-F.checks.json |

## Review scope

- Stage `B-02` acceptance for T-013.
- The regression test fails on the base revision (90c22f7).
- The root cause is documented in this card.
- The minimal fix passes the regression test.
- Relevant existing tests pass (collection-safe; no live run required at CP-1).
- No unrelated behavior or files changed.
- QA verifies fail-before/pass-after independently (T-014).

## Chunks

| Chunk | State | Depth | Role | Produced | Title |
|---|---|---|---|---|---|
| C1 | done | d1 | coder | 2 | B-01 reproduction/root cause + B-02 minimal fix and regression |

Ledger: `docs/chunks/T-013.json`

## Checks

| ID | Check | Result | Detail |
|---|---|---|---|
| artifacts-exist | file-exists | PASS | 3 file(s) |
| artifact-hashes | hash-match | PASS | 2 artifact(s) match ledger |
| fix-src-exist | file-exists | PASS | 117 file(s) |
| regression-tests-exist | file-exists | PASS | 50 file(s) |
| fix-no-todo | pattern-absent | PASS | 2 pattern(s) absent in 3 file(s) |
| fix-no-secrets | pattern-absent | PASS | 3 pattern(s) absent in 3 file(s) |
| chunk-ownership-no-overlap | chunk-ledger | PASS | 1 chunk(s), rule ownership |
| chunk-coverage | chunk-ledger | PASS | 1 chunk(s), rule coverage |
| chunk-budget | chunk-ledger | PASS | 1 chunk(s) within 12/40 KB |

Summary: 9 PASS, 0 FAIL, 0 MANUAL, 0 N/A, 0 UNCHECKED.

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
 docs/evidence/T-013.md                             |  10 ++
 docs/evidence/T-015.md                             | 170 +++++++++++++++++++++
 docs/evidence/T-016.md                             |   8 +
 docs/plan.md                                       |  11 +-
 docs/qa/review-T-016.md                            |  72 +++++++++
 docs/tasks/T-009-e2e-automation.md                 |   2 +
 .../T-013-fix-bug-002-dockerfile-source-paths.md   |  67 ++++++++
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
 42 files changed, 2237 insertions(+), 102 deletions(-)
```

## User remarks

- (user fills; may be empty if artifacts were edited directly)

## User edits

- none

## Role response

- Machine checks: 0 FAIL, 0 UNCHECKED, 0 MANUAL.

## Next action

- return to this checkpoint