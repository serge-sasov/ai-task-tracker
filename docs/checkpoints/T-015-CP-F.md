# T-015 CP-F: Fix BUG-003 - local stack functional health (Flyway/PG16, wiring, JSON shape, tokens)

| Field | Value |
|---|---|
| task | T-015 |
| workflow | bugfix |
| bug | BUG-003 |
| checkpoint | CP-F |
| role | coder |
| stage | B-02 |
| artifacts | backend/pom.xml, src/main/**, docker-compose.yml, tests/regression/** |
| status | awaiting_review |
| requested_at | 2026-11-17T15:41:38 |
| reviewed_at | - |
| reviewer | user |
| decision | - |
| snapshot_commit | b37e5fe |
| generated_by | stage-done/1 |
| checks_file | docs/checkpoints/T-015-CP-F.checks.json |

## Review scope

- Stage `B-02` acceptance for T-015.
- The regression test fails on the base revision (2 of 2 failed).
- The root cause is documented (BUG-003).
- The minimal fix passes the regression test (4 passed with BUG-002 test).
- Relevant existing tests pass (`mvn -f backend/pom.xml -DskipTests package`).
- No unrelated behavior changed.
- QA verifies fail-before/pass-after independently (T-016).

## Chunks

| Chunk | State | Depth | Role | Produced | Title |
|---|---|---|---|---|---|
| C1 | done | d1 | coder | 11 | B-01 reproduction/root cause + B-02 minimal fix and regression |

Ledger: `docs/chunks/T-015.json`

## Checks

| ID | Check | Result | Detail |
|---|---|---|---|
| artifacts-exist | file-exists | PASS | 82 file(s) |
| artifact-hashes | hash-match | PASS | 82 artifact(s) match ledger |
| fix-src-exist | file-exists | PASS | 117 file(s) |
| regression-tests-exist | file-exists | PASS | 50 file(s) |
| fix-no-todo | pattern-absent | PASS | 2 pattern(s) absent in 82 file(s) |
| fix-no-secrets | pattern-absent | PASS | 3 pattern(s) absent in 82 file(s) |
| chunk-ownership-no-overlap | chunk-ledger | PASS | 1 chunk(s), rule ownership |
| chunk-coverage | chunk-ledger | PASS | 1 chunk(s), rule coverage |
| chunk-budget | chunk-ledger | PASS | 1 chunk(s) within 24/40 KB |

Summary: 9 PASS, 0 FAIL, 0 MANUAL, 0 N/A, 0 UNCHECKED.

## Usage

- token usage not available (opencode CLI or database unavailable)

## Diff stat

```
Dockerfile                                         |  10 +-
 backend/pom.xml                                    |   4 +
 docker-compose.yml                                 |   4 +-
 docs/activity.puml                                 |   8 +-
 docs/activity.svg                                  |   2 +-
 docs/bugs/BUG-002-dockerfile-source-paths.md       | 120 +++++
 docs/bugs/BUG-003-local-stack-not-healthy.md       | 141 ++++++
 docs/bugs/README.md                                |   2 +
 docs/checkpoints/T-009-CP-1.checks.json            |  80 +++
 docs/checkpoints/T-009-CP-1.md                     |  85 ++++
 docs/checkpoints/T-009-CP-2.checks.json            |  80 +++
 docs/checkpoints/T-009-CP-2.md                     |  88 ++++
 docs/checkpoints/T-009-CP-F.checks.json            |  80 +++
 docs/checkpoints/T-009-CP-F.md                     |  93 ++++
 docs/checkpoints/T-013-CP-1.checks.json            |  62 +++
 docs/checkpoints/T-013-CP-1.md                     |  85 ++++
 docs/checkpoints/T-013-CP-2.checks.json            |  74 +++
 docs/checkpoints/T-013-CP-2.md                     |  94 ++++
 docs/checkpoints/T-013-CP-F.checks.json            |  74 +++
 docs/checkpoints/T-013-CP-F.md                     | 121 +++++
 docs/checkpoints/T-014-CP-1.checks.json            |  68 +++
 docs/checkpoints/T-014-CP-1.md                     | 157 ++++++
 docs/checkpoints/T-014-CP-F.checks.json            |  68 +++
 docs/checkpoints/T-014-CP-F.md                     | 113 +++++
 docs/checkpoints/T-015-CP-1.checks.json            |  74 +++
 docs/checkpoints/T-015-CP-1.md                     |  96 ++++
 docs/checkpoints/T-015-CP-2.checks.json            |  74 +++
 docs/checkpoints/T-015-CP-2.md                     | 112 +++++
 docs/checkpoints/T-016-CP-1.checks.json            |  68 +++
 docs/checkpoints/T-016-CP-1.md                     | 104 ++++
 docs/checkpoints/T-016-CP-F.checks.json            |  68 +++
 docs/checkpoints/T-016-CP-F.md                     | 109 ++++
 docs/chunks/T-009.json                             | 205 ++++++--
 docs/chunks/T-009/C1.brief.md                      |  45 ++
 docs/chunks/T-013.json                             |  67 +++
 docs/chunks/T-015.json                             | 109 ++++
 docs/evidence/T-009.md                             |  96 ++++
 docs/evidence/T-013.md                             |  13 +
 docs/evidence/T-014.md                             |   8 +
 docs/evidence/T-015.md                             | 170 +++++++
 docs/evidence/T-016.md                             |   8 +
 .../ISSUE-20261117-131059-C1-conftest-destroyed.md |  57 +++
 docs/plan.md                                       |  13 +-
 docs/qa/review-T-014.md                            |  77 +++
 docs/qa/review-T-016.md                            |  72 +++
 docs/tasks/T-009-e2e-automation.md                 |  23 +-
 .../T-013-fix-bug-002-dockerfile-source-paths.md   |  69 +++
 docs/tasks/T-014-verify-bug-002-dockerfile.md      |  62 +++
 docs/tasks/T-015-fix-bug-003-local-stack.md        |  71 +++
 docs/tasks/T-016-verify-bug-003-local-stack.md     |  66 +++
 scripts/e2e.ps1                                    | 224 +++++++++
 scripts/validate-e2e-data.py                       | 166 ++++++
 .../api/search/exception/EmptyQueryException.java  |   2 +-
 .../api/tokens/service/TokenService.java           |  43 +-
 .../config/IngestAutoConfiguration.java            |   9 +
 .../aitasktracker/domain/TaskStatusConverter.java  |  25 +
 .../ru/aitasktracker/domain/entity/TaskEntity.java |   9 +-
 .../domain/repository/TaskRepository.java          |  68 +--
 .../aitasktracker/ingestion/IngestScheduler.java   |   4 +-
 src/main/resources/application.yml                 |   8 +-
 tests/e2e/README.md                                | 128 +++++
 tests/e2e/conftest.py                              | 359 +++++++++++++
 tests/e2e/fixtures/__init__.py                     |  78 +++
 tests/e2e/fixtures/cleanup.py                      | 158 ++++++
 tests/e2e/fixtures/seed.py                         | 138 +++++
 tests/e2e/results/UC-E2E-001.json                  |  41 ++
 tests/e2e/results/UC-E2E-002.json                  |  70 +++
 tests/e2e/results/UC-E2E-003.json                  | 140 ++++++
 tests/e2e/results/UC-E2E-004.json                  |  58 +++
 tests/e2e/results/UC-E2E-005.json                  |  57 +++
 tests/e2e/results/UC-E2E-006.json                  |  57 +++
 tests/e2e/results/UC-E2E-007.json                  |  56 +++
 tests/e2e/results/UC-E2E-008.json                  |  62 +++
 tests/e2e/results/e2e-results.json                 | 556 +++++++++++++++++++++
 tests/e2e/uc_e2e_001_003/__init__.py               |   6 +
 tests/e2e/uc_e2e_001_003/conftest.py               |  51 ++
 tests/e2e/uc_e2e_001_003/shared.py                 | 160 ++++++
 .../uc_e2e_001_003/test_uc_e2e_001_stack_start.py  |  74 +++
 tests/e2e/uc_e2e_001_003/test_uc_e2e_002_ingest.py |  70 +++
 .../test_uc_e2e_003_task_list_detail.py            | 122 +++++
 tests/e2e/uc_e2e_004_006/__init__.py               |  17 +
 .../uc_e2e_004_006/test_uc_e2e_004_ai_search.py    | 141 ++++++
 .../e2e/uc_e2e_004_006/test_uc_e2e_005_calendar.py | 176 +++++++
 .../uc_e2e_004_006/test_uc_e2e_006_token_stats.py  | 157 ++++++
 tests/e2e/uc_e2e_007_008/__init__.py               |  27 +
 tests/e2e/uc_e2e_007_008/helpers.py                | 212 ++++++++
 .../test_uc_e2e_007_restart_idempotency.py         | 207 ++++++++
 .../test_uc_e2e_008_manual_deletion.py             | 231 +++++++++
 tests/regression/test_dockerfile_source_paths.py   |  99 ++++
 tests/regression/test_stack_health_prereqs.py      |  47 ++
 90 files changed, 7844 insertions(+), 118 deletions(-)
```

## User remarks

- (user fills; may be empty if artifacts were edited directly)

## User edits

- none

## Role response

- Machine checks: 0 FAIL, 0 UNCHECKED, 0 MANUAL.

## Next action

- return to this checkpoint