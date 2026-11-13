# T-006 CP-F: R-01 Architecture and contracts (ADR)

| Field | Value |
|---|---|
| task | T-006 |
| workflow | feature |
| bug | - |
| checkpoint | CP-F |
| role | architect |
| stage | R-01 |
| artifacts | docs/architecture/architecture-ai-task-tracker.md, docs/architecture/adr-ai-task-tracker.md, docs/architecture/contracts-ai-task-tracker.md, docs/architecture/openapi-api-*.yaml |
| status | awaiting_review |
| requested_at | 2026-11-13T17:10:41 |
| reviewed_at | - |
| reviewer | user |
| decision | approved |
| snapshot_commit | 411881b |
| generated_by | stage-done/1 |
| checks_file | docs/checkpoints/T-006-CP-F.checks.json |

## Review scope

- Stage `R-01` acceptance for T-006.
- ADR contains Context, Decision, Alternatives, Consequences and covers Q-001, Q-002, Q-003, Q-007, Q-008 decisions (opencode session files + polling; 1 task = 1 opencode session; duration = end - start; token usage from session usage fields; local Ollama AI search; LLM extraction of proposed/chosen solutions).
- Contracts define the PostgreSQL data model, ingestion file format and polling contract, REST API for tasks list/details/calendar/tokens/search/data deletion, AI search contract (local Ollama), error model, and health endpoint.
- Every requirement ID R-001..R-013 and R-020..R-030 is referenced in the ADR or contracts; screens S-01..S-06 and UC-E2E-001..008 are traceable.
- `docs/architecture/architecture-ai-task-tracker.md` is present with all 11 required sections (`## Scope and applicability`, `## Context and constraints`, `## Decisions`, `## Components`, `## Services`, `## Scenarios`, `## Data model`, `## Class model`, `## REST API`, `## Diagram index`, `## Traceability`), defines `SCN-NNN`/`SVC-NNN`/`API-NNN` ids, traces every `R-NNN` and `UC-E2E-NNN`, and carries explicit `No microservices` / `No REST API` statements for any conditional set it omits (silence is not a waiver).
- One valid OpenAPI 3.x spec per `API-NNN` (`docs/architecture/openapi-api-*.yaml`) passes `harness/scripts/validate-openapi.py`, each spec references its `API-NNN`, and the specs are linked into the architecture description (diagram index / REST API section) and vice versa.
- CP-1 and CP-F checkpoints recorded.

## Checks

| ID | Check | Result | Detail |
|---|---|---|---|
| artifacts-exist | file-exists | PASS | 7 file(s) |
| artifact-hashes | hash-match | PASS | 2 artifact(s) match ledger |
| adr-exist | file-exists | PASS | 1 file(s) |
| adr-sections | headings-required | PASS | 1 file(s) |
| contracts-exist | file-exists | PASS | 1 file(s) |
| architecture-exist | file-exists | PASS | 1 file(s) |
| architecture-sections | headings-required | PASS | 1 file(s) |
| architecture-requirement-trace | id-coverage | PASS | 24 id(s) covered by 3 file(s) |
| architecture-description-trace | id-coverage | PASS | 24 id(s) covered by 1 file(s) |
| scenario-ids | ids-unique | PASS | 8 unique id(s) |
| service-ids | ids-unique | PASS | 0 unique id(s) |
| api-ids | ids-unique | PASS | 4 unique id(s) |
| scenario-uc-coverage | id-coverage | PASS | 8 id(s) covered by 1 file(s) |
| openapi-count | count-match | PASS | 4 = 4 |
| openapi-ids | id-coverage | PASS | 4 id(s) covered by 4 file(s) |
| openapi-valid | python-validator | PASS | 4 file(s) |
| architecture-openapi-links | reference-coverage | PASS | 4 file(s) referenced by 1 target(s) |

Summary: 17 PASS, 0 FAIL, 0 MANUAL, 0 N/A, 0 UNCHECKED.

## Usage

| Sessions | Input | Output | Reasoning | Cache read | Cache/Input | Minutes | Cost |
|---|---|---|---|---|---|---|---|
| 6 | 452356 | 182070 | 0 | 4372564 | 9.7x | 211 | 0.00 |

## Diff stat

```
.gitignore                                         |   1 +
 docs/activity.puml                                 |  49 ++-
 docs/activity.svg                                  |   2 +-
 docs/architecture/adr-ai-task-tracker.md           | 253 ++++++++++++++
 docs/architecture/contracts-ai-task-tracker.md     | 372 +++++++++++++++++++++
 docs/checkpoints/T-006-CP-1.checks.json            |  56 ++++
 docs/checkpoints/T-006-CP-1.md                     |  74 ++++
 docs/checkpoints/T-006-CP-F.checks.json            |  56 ++++
 docs/checkpoints/T-006-CP-F.md                     |  80 +++++
 docs/evidence/T-006.md                             |  10 +
 ...-contracts-file-exceeded-role-session-budget.md |  52 +++
 ...one-record-step-fails-on-git-lf-crlf-warning.md |  52 +++
 docs/issues/README.md                              |   2 +
 docs/plan.md                                       |   4 +-
 docs/tasks/T-006-architecture-contracts.md         |  57 ++++
 docs/tasks/T-007-diagrams-handoff.md               |  47 +++
 harness/agents/harness-analyst.md                  | 149 +++++++++
 harness/agents/harness-architect.md                | 158 +++++++++
 harness/agents/harness-coder.md                    | 230 +++++++++++++
 harness/agents/harness-doc-writer.md               | 128 +++++++
 harness/agents/harness-orchestrator.md             | 184 ++++++++++
 harness/agents/harness-qa-vision.md                | 101 ++++++
 harness/agents/harness-qa.md                       | 139 ++++++++
 harness/agents/harness-ux-designer.md              | 166 +++++++++
 harness/agents/harness.md                          | 195 +++++++++++
 harness/checks/feature.json                        |  26 +-
 harness/process.md                                 |  22 +-
 harness/scripts/doctor.ps1                         |  13 +-
 harness/scripts/harness-lib.ps1                    |  11 +-
 harness/scripts/init.ps1                           |   3 +
 harness/scripts/install.ps1                        |   3 +
 harness/scripts/stage-done.ps1                     |   5 +-
 harness/scripts/validate-openapi.py                | 132 ++++++++
 harness/scripts/verify-artifacts.ps1               |  70 +++-
 harness/skills/harness-issues/SKILL.md             |  74 ++++
 harness/skills/harness/SKILL.md                    | 118 +++++++
 harness/templates/adr.md                           |  10 +-
 harness/templates/architecture-description.md      | 113 +++++++
 harness/templates/openapi.yaml                     |  28 ++
 harness/templates/task-card-feature.md             |   4 +
 harness/version.json                               |  72 ++--
 harness/workflows/feature.md                       |  29 +-
 42 files changed, 3259 insertions(+), 91 deletions(-)
```

## User remarks

- 2026-11-13: user approved CP-F after harness 2.6.0 re-run; 17/17 checks PASS; architecture description + 4 OpenAPI specs accepted.
- (user fills; may be empty if artifacts were edited directly)

## User edits

- none

## Role response

- Machine checks: 0 FAIL, 0 UNCHECKED, 0 MANUAL.

## Next action

- return to this checkpoint