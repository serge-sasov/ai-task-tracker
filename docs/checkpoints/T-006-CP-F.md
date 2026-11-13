# T-006 CP-F: R-01 Architecture and contracts (ADR)

| Field | Value |
|---|---|
| task | T-006 |
| workflow | feature |
| bug | - |
| checkpoint | CP-F |
| role | architect |
| stage | R-01 |
| artifacts | docs/architecture/adr-ai-task-tracker.md, docs/architecture/contracts-ai-task-tracker.md |
| status | awaiting_review |
| requested_at | 2026-11-13T12:39:24 |
| reviewed_at | - |
| reviewer | user |
| decision | - |
| snapshot_commit | - |
| generated_by | stage-done/1 |
| checks_file | docs/checkpoints/T-006-CP-F.checks.json |

## Review scope

- Stage `R-01` acceptance for T-006.
- ADR contains Context, Decision, Alternatives, Consequences and covers Q-001, Q-002, Q-003, Q-007, Q-008 decisions (opencode session files + polling; 1 task = 1 opencode session; duration = end - start; token usage from session usage fields; local Ollama AI search; LLM extraction of proposed/chosen solutions).
- Contracts define the PostgreSQL data model, ingestion file format and polling contract, REST API for tasks list/details/calendar/tokens/search/data deletion, AI search contract (local Ollama), error model, and health endpoint.
- Every requirement ID R-001..R-013 and R-020..R-030 is referenced in the ADR or contracts; screens S-01..S-06 and UC-E2E-001..008 are traceable.
- CP-1 and CP-F checkpoints recorded.

## Checks

| ID | Check | Result | Detail |
|---|---|---|---|
| artifacts-exist | file-exists | PASS | 2 file(s) |
| artifact-hashes | hash-match | PASS | 0 artifact(s) match ledger |
| adr-exist | file-exists | PASS | 1 file(s) |
| adr-sections | headings-required | PASS | 1 file(s) |
| contracts-exist | file-exists | PASS | 1 file(s) |
| architecture-requirement-trace | id-coverage | PASS | 24 id(s) covered by 2 file(s) |

Summary: 6 PASS, 0 FAIL, 0 MANUAL, 0 N/A, 0 UNCHECKED.

## Usage

| Sessions | Input | Output | Reasoning | Cache read | Cache/Input | Minutes | Cost |
|---|---|---|---|---|---|---|---|
| 5 | 335491 | 132909 | 0 | 3268191 | 9.7x | 155 | 0.00 |

## Diff stat

```
docs/activity.puml                                 |  49 ++-
 docs/activity.svg                                  |   2 +-
 docs/architecture/adr-ai-task-tracker.md           | 253 ++++++++++++++
 docs/architecture/contracts-ai-task-tracker.md     | 372 +++++++++++++++++++++
 docs/checkpoints/T-006-CP-1.checks.json            |  56 ++++
 docs/checkpoints/T-006-CP-1.md                     |  74 ++++
 docs/evidence/T-006.md                             |   8 +
 ...-contracts-file-exceeded-role-session-budget.md |  52 +++
 docs/issues/README.md                              |   1 +
 docs/plan.md                                       |   2 +-
 docs/tasks/T-006-architecture-contracts.md         |  50 +++
 11 files changed, 890 insertions(+), 29 deletions(-)
```

## User remarks

- (user fills; may be empty if artifacts were edited directly)

## User edits

- none

## Role response

- Machine checks: 0 FAIL, 0 UNCHECKED, 0 MANUAL.

## Next action

- return to this checkpoint