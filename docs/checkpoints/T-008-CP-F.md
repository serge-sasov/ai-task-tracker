# T-008 CP-F: I-01 Implementation and unit/integration tests

| Field | Value |
|---|---|
| task | T-008 |
| workflow | feature |
| bug | - |
| checkpoint | CP-F |
| role | coder |
| stage | I-01 |
| artifacts | src/**, tests/**, backend/**, Dockerfile, docker-compose*.yml, Makefile |
| status | awaiting_review |
| requested_at | 2026-11-15T23:46:35 |
| reviewed_at | - |
| reviewer | user |
| decision | - |
| snapshot_commit | 5090c26 |
| generated_by | stage-done/1 |
| checks_file | docs/checkpoints/T-008-CP-F.checks.json |

## Review scope

- Stage `I-01` acceptance for T-008.
- Backend builds and all unit/integration tests pass locally (Makefile `test` target) — Java 21 + Spring Boot 3, PostgreSQL 16 + pgvector, Redis 7 per ADR-0001/contracts.
- REST endpoints API-001..API-004 implemented per the OpenAPI specs and contracts §4–5 (RU error envelope, pagination, timings R-023/R-024).
- Ingestion: 60 s poll, idempotent re-sync (R-025), Redis lock/watermark, defensive parsing (ADR consequences).
- Vue 3 SPA screens S-01..S-06 implemented per UI spec, RU copy (R-030).
- One-command local stack: `docker compose up` brings up backend+postgres+redis+frontend with ports on 127.0.0.1 (R-022, R-027); `Makefile` provides up/down/test/logs.
- No TODO/FIXME placeholders, no secrets in code/config (checks no-todo, no-secrets).

## Chunks

| Chunk | State | Depth | Role | Produced | Title |
|---|---|---|---|---|---|
| C4fix | done | d1 | coder | 8 | Fix API defects A/B/C and add context wiring test |
| C5a | done | d1 | coder | 13 | API-002 calendar and tokens |
| C5b | done | d1 | coder | 10 | API-003 search and Ollama client |
| C6a | done | d1 | coder | 1 | Vue shell: build config, API client, store, router |
| C6b | done | d1 | coder | 6 | Screens S-01..S-03 |
| C6c | done | d1 | coder | 8 | Screens S-04..S-06 |
| C6d | done | d1 | coder | 6 | Frontend build wiring and full verification |

Ledger: `docs/chunks/T-008.json`

## Checks

| ID | Check | Result | Detail |
|---|---|---|---|
| artifacts-exist | file-exists | PASS | 140 file(s) |
| artifact-hashes | hash-match | FAIL | docker-compose.yml (hash changed); Makefile (hash changed) |
| src-exist | file-exists | PASS | 116 file(s) |
| tests-exist | file-exists | PASS | 20 file(s) |
| no-todo | pattern-absent | PASS | 3 pattern(s) absent in 140 file(s) |
| no-secrets | pattern-absent | PASS | 3 pattern(s) absent in 140 file(s) |
| chunk-ownership-no-overlap | chunk-ledger | PASS | 7 chunk(s), rule ownership |
| chunk-coverage | chunk-ledger | PASS | 7 chunk(s), rule coverage |
| chunk-budget | chunk-ledger | PASS | 7 chunk(s) within 12/40 KB |

Summary: 8 PASS, 1 FAIL, 0 MANUAL, 0 N/A, 0 UNCHECKED.

## Usage

| Sessions | Input | Output | Reasoning | Cache read | Cache/Input | Minutes | Cost |
|---|---|---|---|---|---|---|---|
| 83 | 7552704 | 1760959 | 14205 | 72346734 | 9.6x | 3315 | 0.06 |

## Diff stat

```
.env.example                                       |   26 +
 .gitignore                                         |   10 +
 Dockerfile                                         |   31 +
 Makefile                                           |   18 +
 backend/pom.xml                                    |   76 +
 docker-compose.yml                                 |   84 +
 docs/activity.puml                                 |    2 +-
 docs/activity.svg                                  |    2 +-
 docs/architecture/replay/replay-api-004.yaml       |   66 +
 docs/architecture/replay/replay-scn-001.puml       |   43 +
 docs/architecture/replay/replay-scn-001.svg        |    1 +
 docs/bugs/BUG-001-api-004-spec-diverges.md         |   68 +
 docs/bugs/README.md                                |    1 +
 docs/checkpoints/T-008-CP-1.checks.json            |   56 +
 docs/checkpoints/T-008-CP-1.md                     |   85 +
 docs/chunks/T-006.json                             |  346 ++++
 docs/chunks/T-006/R1.brief.md                      |   42 +
 docs/chunks/T-007.json                             |  607 +++++++
 docs/chunks/T-007/R2.brief.md                      |   42 +
 docs/chunks/T-008.json                             | 1670 ++++++++++++++++++++
 docs/chunks/T-008/C4fix.brief.md                   |   53 +
 docs/chunks/T-008/C5a.brief.md                     |   43 +
 docs/chunks/T-008/C5b.brief.md                     |   68 +
 docs/chunks/T-008/C6a.brief.md                     |   45 +
 docs/chunks/T-008/C6b.brief.md                     |   77 +
 docs/chunks/T-008/C6c.brief.md                     |   78 +
 docs/chunks/T-008/C6d.brief.md                     |   44 +
 docs/chunks/T-009.json                             |  286 ++++
 docs/chunks/T-010.json                             |  132 ++
 docs/chunks/T-011.json                             |  172 ++
 docs/evidence/T-008.md                             |   14 +
 ...-203800-chunk-usage-attribution-and-coverage.md |   82 +
 ...s-exclude-regex-dead-code-harness-lib-ps1-46.md |   59 +
 docs/issues/README.md                              |    2 +
 docs/plan.md                                       |    2 +-
 docs/tasks/T-008-implementation-tests.md           |   61 +
 docs/tasks/T-009-e2e-automation.md                 |   46 +
 docs/tasks/T-010-e2e-plan.md                       |   45 +
 docs/tasks/T-011-qa-acceptance.md                  |   46 +
 harness/checks/bugfix.json                         |  367 ++++-
 harness/checks/feature.json                        | 1533 ++++++++++++++++--
 harness/process.md                                 |  120 +-
 harness/scripts/chunk-validate.py                  |  545 +++++++
 harness/scripts/chunk.ps1                          |  371 +++++
 harness/scripts/doctor.ps1                         |   21 +-
 harness/scripts/harness-lib.ps1                    |  122 ++
 harness/scripts/init.ps1                           |    2 +-
 harness/scripts/install.ps1                        |   20 +-
 harness/scripts/measure-tokens.ps1                 |   46 +
 harness/scripts/stage-done.ps1                     |   50 +-
 harness/scripts/uninstall.ps1                      |    2 +-
 harness/scripts/verify-artifacts.ps1               |  458 ++++++
 harness/templates/adr.md                           |   10 +-
 harness/templates/architecture-description.md      |   68 +-
 harness/templates/chunk-ledger.md                  |  162 ++
 harness/templates/plan-row.md                      |    3 +
 harness/templates/task-card-bugfix.md              |    4 +-
 harness/templates/task-card-feature.md             |    4 +-
 harness/version.json                               |   72 +-
 harness/workflows/feature.md                       |   25 +-
 .../ru/aitasktracker/AiTaskTrackerApplication.java |   14 +
 .../calendar/controller/CalendarController.java    |   33 +
 .../api/calendar/dto/CalendarDay.java              |   34 +
 .../api/calendar/dto/CalendarResponse.java         |   40 +
 .../api/calendar/dto/CalendarTask.java             |   37 +
 .../aitasktracker/api/calendar/dto/MonthCell.java  |   38 +
 .../api/calendar/service/CalendarService.java      |  191 +++
 .../api/controller/ApiErrorAdapter.java            |   35 +
 .../api/controller/HealthController.java           |   31 +
 .../api/controller/HistoryController.java          |   66 +
 .../api/controller/TaskController.java             |   49 +
 .../java/ru/aitasktracker/api/dto/ApiError.java    |   24 +
 .../ru/aitasktracker/api/dto/HealthResponse.java   |   63 +
 .../java/ru/aitasktracker/api/dto/SearchHit.java   |   36 +
 .../ru/aitasktracker/api/dto/SearchRequest.java    |   31 +
 .../ru/aitasktracker/api/dto/SearchResponse.java   |   33 +
 .../ru/aitasktracker/api/dto/SolutionItem.java     |   23 +
 .../java/ru/aitasktracker/api/dto/StepItem.java    |   29 +
 .../aitasktracker/api/dto/TaskDetailResponse.java  |  121 ++
 .../ru/aitasktracker/api/dto/TaskListItem.java     |   57 +
 .../ru/aitasktracker/api/dto/TaskListResponse.java |   26 +
 .../ru/aitasktracker/api/dto/TokenByModel.java     |   42 +
 .../java/ru/aitasktracker/api/dto/TokenTotals.java |   43 +
 .../aitasktracker/api/exception/ApiException.java  |   22 +
 .../exception/ConfirmationRequiredException.java   |   31 +
 .../api/exception/InternalErrorException.java      |   35 +
 .../api/exception/InvalidPeriodException.java      |   31 +
 .../ru/aitasktracker/api/exception/NotFound.java   |   34 +
 .../api/exception/ServiceUnavailableException.java |   32 +
 .../search/configuration/SearchConfiguration.java  |   37 +
 .../api/search/controller/SearchController.java    |   39 +
 .../api/search/exception/EmptyQueryException.java  |   35 +
 .../exception/SearchUnavailableException.java      |   38 +
 .../api/search/service/SearchService.java          |  165 ++
 .../aitasktracker/api/service/HealthService.java   |  139 ++
 .../aitasktracker/api/service/HistoryService.java  |   71 +
 .../aitasktracker/api/service/OllamaChecker.java   |   44 +
 .../api/service/TaskQueryService.java              |  192 +++
 .../api/tokens/controller/TokensController.java    |   36 +
 .../aitasktracker/api/tokens/dto/TokenByModel.java |   44 +
 .../api/tokens/dto/TokenStatsResponse.java         |   41 +
 .../aitasktracker/api/tokens/dto/TokenTotals.java  |   31 +
 .../api/tokens/service/TokenService.java           |  120 ++
 .../ru/aitasktracker/api/web/ApiConfiguration.java |   63 +
 .../ru/aitasktracker/api/web/ApiNoCacheFilter.java |   34 +
 .../config/IngestAutoConfiguration.java            |   52 +
 .../config/TrackerAutoConfiguration.java           |    9 +
 .../ru/aitasktracker/config/TrackerProperties.java |   60 +
 .../java/ru/aitasktracker/domain/TaskDuration.java |   40 +
 .../java/ru/aitasktracker/domain/TaskStatus.java   |   41 +
 .../ru/aitasktracker/domain/TaskStatusService.java |   71 +
 .../domain/entity/IngestStateEntity.java           |   47 +
 .../domain/entity/SolutionEntity.java              |  103 ++
 .../ru/aitasktracker/domain/entity/StepEntity.java |   92 ++
 .../ru/aitasktracker/domain/entity/TaskEntity.java |  149 ++
 .../domain/entity/TokenUsageEntity.java            |  128 ++
 .../domain/repository/IngestStateRepository.java   |   18 +
 .../domain/repository/SolutionRepository.java      |   28 +
 .../domain/repository/StepRepository.java          |   36 +
 .../domain/repository/TaskRepository.java          |   68 +
 .../domain/repository/TokenUsageRepository.java    |   53 +
 .../aitasktracker/ingestion/IngestScheduler.java   |   47 +
 .../ru/aitasktracker/ingestion/IngestService.java  |  402 +++++
 .../ingestion/domain/IngestJsonParser.java         |  311 ++++
 .../ingestion/domain/IngestedSession.java          |  194 +++
 .../ingestion/domain/SourceStatus.java             |   18 +
 .../ingestion/extractor/NoOpSolutionExtractor.java |   20 +
 .../ingestion/extractor/SolutionExtractor.java     |   39 +
 .../ingestion/lock/InMemoryTaskLock.java           |   41 +
 .../ingestion/lock/RedisTaskLock.java              |   72 +
 .../ru/aitasktracker/ingestion/lock/TaskLock.java  |   32 +
 .../java/ru/aitasktracker/search/OllamaClient.java |  194 +++
 .../ru/aitasktracker/search/OllamaException.java   |   20 +
 src/main/resources/application.yml                 |   38 +
 .../db/migration/V1__vector_extension.sql          |    2 +
 src/main/resources/db/migration/V2__schema.sql     |   51 +
 src/main/resources/db/migration/V3__indexes.sql    |   10 +
 src/web/.dockerignore                              |    6 +
 src/web/Dockerfile                                 |   24 +
 src/web/index.html                                 |   13 +
 src/web/nginx.conf                                 |   44 +
 src/web/package-lock.json                          | 1515 ++++++++++++++++++
 src/web/package.json                               |   24 +
 src/web/src/App.vue                                |   45 +
 src/web/src/api/client.ts                          |  131 ++
 src/web/src/api/types.ts                           |  155 ++
 src/web/src/assets/main.css                        |   84 +
 src/web/src/components/calendar/Heatmap.vue        |  147 ++
 src/web/src/components/calendar/Legend.vue         |   74 +
 src/web/src/components/calendar/WeekdayRow.vue     |   36 +
 src/web/src/components/dashboard/StatCard.vue      |   16 +
 src/web/src/components/dashboard/StatusBadge.vue   |   17 +
 src/web/src/components/format.ts                   |   44 +
 src/web/src/components/shared/Dialog.vue           |  132 ++
 src/web/src/global.d.ts                            |    3 +
 src/web/src/main.ts                                |   14 +
 src/web/src/registry/views.ts                      |   24 +
 src/web/src/router/index.ts                        |   79 +
 src/web/src/shims-vue.d.ts                         |    5 +
 src/web/src/stores/calendar.ts                     |   48 +
 src/web/src/stores/health.ts                       |   33 +
 src/web/src/stores/search.ts                       |   48 +
 src/web/src/stores/settings.ts                     |   39 +
 src/web/src/stores/tasks.ts                        |   53 +
 src/web/src/stores/tokens.ts                       |   36 +
 src/web/src/views/CalendarView.vue                 |  332 ++++
 src/web/src/views/DashboardView.vue                |  292 ++++
 src/web/src/views/HistoryView.vue                  |  208 +++
 src/web/src/views/SearchView.vue                   |  197 +++
 src/web/src/views/SettingsView.vue                 |  234 +++
 src/web/src/views/TaskView.vue                     |  264 ++++
 src/web/src/views/index-c6c.ts                     |    4 +
 src/web/src/views/index.ts                         |    4 +
 src/web/tsconfig.json                              |   23 +
 src/web/tsconfig.node.json                         |   12 +
 src/web/vite.config.ts                             |   30 +
 .../ru/aitasktracker/api/ApiContextWiringTest.java |  129 ++
 .../api/calendar/CalendarServiceTest.java          |  177 +++
 .../search/controller/SearchControllerTest.java    |   64 +
 .../api/search/service/SearchServiceTest.java      |  130 ++
 .../api/service/HealthServiceTest.java             |  139 ++
 .../api/service/HistoryServiceTest.java            |  101 ++
 .../api/service/OllamaCheckerTest.java             |   88 ++
 .../api/service/TaskQueryServiceTest.java          |  220 +++
 .../aitasktracker/api/tokens/TokenServiceTest.java |  160 ++
 .../config/TrackerPropertiesTest.java              |   33 +
 .../aitasktracker/domain/FlywayMigrationsTest.java |  157 ++
 .../ru/aitasktracker/domain/TaskDurationTest.java  |   69 +
 .../domain/TaskStatusServiceTest.java              |  144 ++
 .../ingestion/IngestJsonParserTest.java            |  173 ++
 .../ingestion/IngestSchedulerTest.java             |   70 +
 .../aitasktracker/ingestion/IngestServiceTest.java |  217 +++
 .../extractor/NoOpSolutionExtractorTest.java       |   26 +
 .../ingestion/lock/InMemoryTaskLockTest.java       |   45 +
 .../ingestion/lock/RedisTaskLockTest.java          |  101 ++
 .../ru/aitasktracker/search/OllamaClientTest.java  |  115 ++
 196 files changed, 20157 insertions(+), 222 deletions(-)
```

## User remarks

- (user fills; may be empty if artifacts were edited directly)

## User edits

- none

## Role response

- Machine checks: 1 FAIL, 0 UNCHECKED, 0 MANUAL.
- Failed checks: artifact-hashes

## Next action

- return to this checkpoint