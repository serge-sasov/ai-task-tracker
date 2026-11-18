# T-023: R-02 Contracts and OpenAPI for models

| Field | Value |
|---|---|
| id | T-023 |
| title | R-02 Interfaces and data model: task-list models contract |
| workflow | feature |
| stage | R-02 |
| role | architect |
| status | in_progress |
| hold | false |
| depends_on | T-022 |
| inputs | `docs/tasks/T-022-architecture-model-column.md`, `docs/analysis/requirements-ai-task-tracker.md`, `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/ux/ui-spec-ai-task-tracker.md`, `docs/architecture/architecture-ai-task-tracker.md`, `docs/architecture/adr-ai-task-tracker.md` |
| artifacts | `docs/architecture/architecture-ai-task-tracker.md`, `docs/architecture/contracts-ai-task-tracker.md`, `docs/architecture/openapi-api-001-ai-task-tracker.yaml`, `docs/architecture/data-model-ai-task-tracker.puml`, `docs/architecture/data-model-ai-task-tracker.svg`, `docs/architecture/class-ai-task-tracker.puml`, `docs/architecture/class-ai-task-tracker.svg` |
| e2e | required - the contract is asserted by UC-E2E-003 S8 and the SCN-003 sequence |
| ux | required - the S-01 model cell consumes the `models` field defined here |
| chunks | `docs/chunks/T-023.json` (C1 contracts/OpenAPI, C2 architecture/class model, C3 data model) |
| budget | max_write 100 KB / session 200 KB; three chunks |
| base_commit | 6501cac |
| usage | n/a |
| issues | - |
| commit | e99aacc |

## Acceptance

- [x] Contracts §4.2 define `models` ordering, empty-array semantics and the «Нет данных» UI state; R-014 is traced.
- [x] OpenAPI API-001 adds optional `models: string[]` without changing required fields; the spec validates.
- [x] The architecture description traces R-014 and defines class-level responsibility zones for the model aggregation/rendering.
- [x] The class diagram includes `TaskListItem.models` and the ordering note; the data model remains unchanged (derived field, no migration).
- [x] All four OpenAPI specs validate; API IDs and links stay consistent.
- [x] Chunks C1, C2 and C3 are `done` with produced hashes in `docs/chunks/T-023.json`.
- [x] `CP-2` (interfaces and data model) and `CP-F` (final acceptance) are declared.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-2 | R-02 contracts, OpenAPI, data/class model | contracts, OpenAPI, architecture, data/class diagrams | 21P/0F/1M | approved | docs/checkpoints/T-023-CP-2.md |
| CP-F | final acceptance | all task artifacts | - | pending | `docs/checkpoints/T-023-CP-F.md` |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.

## Chunk plan

| Chunk | Stage | Role | Title | Owns | State |
|---|---|---|---|---|---|
| C1 | R-02 | architect | Contracts and OpenAPI for `models` | `docs/architecture/contracts-ai-task-tracker.md`, `docs/architecture/openapi-api-001-ai-task-tracker.yaml` | done |
| C2 | R-02 | architect | Architecture trace and class model | `docs/architecture/architecture-ai-task-tracker.md`, `docs/architecture/class-ai-task-tracker.puml`, `docs/architecture/class-ai-task-tracker.svg` | done |
| C3 | R-02 | architect | Data model confirmation (derived models, no migration) | `docs/architecture/data-model-ai-task-tracker.puml`, `docs/architecture/data-model-ai-task-tracker.svg` | done |

## Log

- 2026-11-18: T-023 started on base `6501cac`; T-022 is done. Contracts, OpenAPI, architecture and class model updated for R-014; class SVG rendered; chunks recorded.
- 2026-11-18: CP-2 awaiting_review (R-02); checks 21 PASS/0 FAIL/1 MANUAL/0 UNCHECKED; usage n/a; reviewer user
- e99aacc: CP-2 snapshot
