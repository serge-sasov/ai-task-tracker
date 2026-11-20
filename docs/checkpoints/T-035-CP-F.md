# T-035 CP-F: R-02 Contracts and module handoff for S-04

| Field | Value |
|---|---|
| task | T-035 |
| workflow | feature |
| bug | - |
| checkpoint | CP-F |
| role | architect |
| stage | R-02 |
| artifacts | docs/architecture/architecture-ai-task-tracker.md, docs/architecture/contracts-ai-task-tracker.md, docs/architecture/openapi-api-*.yaml, docs/architecture/data-model-*.puml, docs/architecture/data-model-*.svg, docs/architecture/class-*.puml, docs/architecture/class-*.svg, docs/architecture/swimlane-svc-*.puml |
| status | awaiting_review |
| requested_at | 2026-11-20T22:13:50 |
| reviewed_at | - |
| reviewer | user |
| decision | - |
| snapshot_commit | - |
| generated_by | stage-done/1 |
| checks_file | docs/checkpoints/T-035-CP-F.checks.json |

## Review scope

- Stage `R-02` acceptance for T-035.
- `contracts-ai-task-tracker.md` is extended with the S-04 module boundary: the frontend shared model-cell component owns the `models: string[]` -> display mapping (primary, `+N`, full list, "Нет данных") and the day view reuses it; the API contract is unchanged (no new endpoint, no schema change).
- The architecture description is extended with the S-04 data/class model and `CLS-NNN` IDs for the shared model-cell element, and traces `R-015` to `API-NNN` (the existing `GET /api/tasks`), `SCN-NNN`, and the class.
- The OpenAPI spec `openapi-api-*.yaml` is confirmed to already expose `models` in the task list item response; if no change is required, the description states "No new REST API" and the existing spec is re-validated.
- The data model and class diagram (`.puml`/`.svg`) are updated for the shared model-cell element and re-rendered; the swimlane diagram for the relevant service is updated and re-rendered; all diagrams pass syntax and SVG checks.
- The module handoff to the coder names the shared component/composable, the files to touch (`CalendarDayView.vue`, `DashboardView.vue`, a new shared element, `types.ts`) and the test expectations, and states the S-01 behavior must remain green.
- Class-level responsibility zones are registered and no two classes of the same level own the same function; every artifact is linked from `architecture-*.md`.
- `CP-2` (contracts, data/class models, swimlanes, module handoff) and `CP-F` (final acceptance) are declared.

## Chunks

| Chunk | State | Depth | Role | Produced | Title |
|---|---|---|---|---|---|
| C1 | done | d1 | architect | 1 | contracts: S-04 module boundary + module handoff |
| C2 | done | d1 | architect | 4 | class diagram: ModelCell CLS-005 + re-render; data model confirmed |
| C3 | done | d1 | architect | 1 | architecture description R-02 extension + OpenAPI re-validation |

Ledger: `docs/chunks/T-035.json`

## Checks

| ID | Check | Result | Detail |
|---|---|---|---|
| artifacts-exist | file-exists | FAIL | docs/architecture/swimlane-svc-*.puml (unresolved from card) |
| artifact-hashes | hash-match | PASS | 10 artifact(s) match ledger |
| contracts-exist | file-exists | PASS | 1 file(s) |
| contracts-requirement-trace | id-coverage | PASS | 27 id(s) covered by 1 file(s) |
| architecture-sections | headings-required | PASS | 1 file(s) |
| architecture-requirement-trace | id-coverage | PASS | 27 id(s) covered by 3 file(s) |
| architecture-description-trace | id-coverage | PASS | 27 id(s) covered by 1 file(s) |
| api-ids | ids-unique | PASS | 4 unique id(s) |
| openapi-count | count-match | PASS | 4 = 4 |
| openapi-ids | id-coverage | PASS | 4 id(s) covered by 4 file(s) |
| openapi-valid | python-validator | PASS | 4 file(s) |
| architecture-openapi-links | reference-coverage | PASS | 4 file(s) referenced by 1 target(s) |
| data-model-exist | file-exists | PASS | 1 file(s) |
| class-diagram-exist | file-exists | PASS | 1 file(s) |
| responsibility-zones | zones-unique | PASS | 1 file(s), 18 zone(s) |
| class-ids | ids-unique | PASS | 5 unique id(s) |
| diagram-syntax | puml-syntax | PASS | 2 diagram(s) |
| diagram-svg | svg-fresh | PASS | 2 SVG(s) |
| swimlane-count | count-match | NOT_APPLICABLE | no source ids |
| swimlane-ids | id-coverage | NOT_APPLICABLE | source=1, target=0 |
| architecture-links | reference-coverage | PASS | 17 file(s) referenced by 1 target(s) |
| zones-overlap-review | manual | MANUAL | component, module, and class responsibility zones: no two elements of the same level own the same functions |
| chunk-ownership-no-overlap | chunk-ledger | PASS | 3 chunk(s), rule ownership |
| chunk-coverage | chunk-ledger | PASS | 3 chunk(s), rule coverage |
| chunk-budget | chunk-ledger | FAIL | C1: docs/architecture/contracts-ai-task-tracker.md 28,8 KB > 12 KB write; C2: docs/architecture/class-ai-task-tracker.svg 90,6 KB > 12 KB write; C2: docs/architecture/data-model-ai-task-tracker.svg 19,5 KB > 12 KB write; C2: 117,9 KB > 40 KB session; C3: docs/architecture/architecture-ai-task-tracker.md 13,5 KB > 12 KB write |

Summary: 20 PASS, 2 FAIL, 1 MANUAL, 2 N/A, 0 UNCHECKED.

## Manual review

- zones-overlap-review: component, module, and class responsibility zones: no two elements of the same level own the same functions

## Usage

| Sessions | Input | Output | Reasoning | Cache read | Cache/Input | Minutes | Cost |
|---|---|---|---|---|---|---|---|
| 5 | 307266 | 99054 | 0 | 5519740 | 18x | 234 | 0.00 |

## Diff stat

```
docs/checkpoints/T-035-CP-2.md                     |  2 +-
 ...wn_outside_card-card-artifacts-omit-svg-rend.md | 93 ++++++++++++++++++++++
 docs/issues/README.md                              |  1 +
 docs/plan.md                                       |  2 +-
 docs/tasks/T-035-contracts-s04-model.md            |  3 +-
 5 files changed, 98 insertions(+), 3 deletions(-)
```

## User remarks

- (user fills; may be empty if artifacts were edited directly)

## User edits

- none

## Role response

- Machine checks: 2 FAIL, 0 UNCHECKED, 1 MANUAL.
- Failed checks: artifacts-exist, chunk-budget

## Next action

- return to this checkpoint