# T-022: R-01 Solution design for model aggregation and S-01 display

| Field | Value |
|---|---|
| id | T-022 |
| title | R-01 Solution design: model aggregation and S-01 display |
| workflow | feature |
| stage | R-01 |
| role | architect |
| status | awaiting_review |
| hold | false |
| depends_on | T-018, T-019, T-021 |
| inputs | `docs/tasks/T-018-requirements-model-column.md`, `docs/tasks/T-019-e2e-model-column.md`, `docs/tasks/T-021-prototype-model-column.md`, `docs/analysis/requirements-ai-task-tracker.md`, `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/ux/screens-ai-task-tracker.md`, `docs/ux/ui-spec-ai-task-tracker.md` |
| artifacts | `docs/architecture/architecture-ai-task-tracker.md`, `docs/architecture/adr-ai-task-tracker.md`, `docs/architecture/components-ai-task-tracker.puml`, `docs/architecture/components-ai-task-tracker.svg`, `docs/architecture/sequence-scn-003-ai-task-tracker.puml`, `docs/architecture/sequence-scn-003-ai-task-tracker.svg` |
| e2e | required - R-014 is covered by UC-E2E-003 S8 and the SCN-003 sequence |
| ux | required - the S-01 model cell and its states are fixed by T-020/T-021 |
| chunks | `docs/chunks/T-022.json` (C1 ADR+architecture, C2 components, C3 SCN-003) |
| budget | max_write 80 KB / session 160 KB; three chunks |
| base_commit | 562f67e |
| usage | in=48057 out=18854 cr=272822 ratio=5.7x min=34 |
| issues | - |
| commit | a4899c2 |

## Acceptance

- [x] ADR-0001 records R-014 in the token-accounting decision and in the `GET /api/tasks` contract; alternatives and consequences stay valid.
- [x] The architecture description traces R-014, cites the shared `token_usage` rows, and defines the `models` ordering and no-usage behavior.
- [x] Component and module responsibility zones include the model cell and `TaskListItem.models` aggregation without overlap.
- [x] SCN-003 covers the model column, `+N`, full tooltip/aria list and "Нет данных"; its sequence diagram shows `models` in the list response.
- [x] The component and SCN-003 diagrams are re-rendered and linked from the architecture description.
- [x] Chunks C1, C2 and C3 are `done` with produced hashes in `docs/chunks/T-022.json`.
- [x] `CP-1` (solution design) and `CP-F` (final acceptance) are declared.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-1 | R-01 ADR, architecture, component and SCN-003 | ADR, architecture, components, sequence | 22P/0F/1M | approved | docs/checkpoints/T-022-CP-1.md |
| CP-F | final acceptance | all task artifacts | 23P/0F/1M | awaiting_review | docs/checkpoints/T-022-CP-F.md |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.

## Chunk plan

| Chunk | Stage | Role | Title | Owns | State |
|---|---|---|---|---|---|
| C1 | R-01 | architect | ADR and architecture description for R-014 | `docs/architecture/architecture-ai-task-tracker.md`, `docs/architecture/adr-ai-task-tracker.md` | done |
| C2 | R-01 | architect | Component diagram with the task model aggregation | `docs/architecture/components-ai-task-tracker.puml`, `docs/architecture/components-ai-task-tracker.svg` | done |
| C3 | R-01 | architect | SCN-003 sequence with the models response | `docs/architecture/sequence-scn-003-ai-task-tracker.puml`, `docs/architecture/sequence-scn-003-ai-task-tracker.svg` | done |

## Log

- 2026-11-18: T-022 started on base `562f67e`; T-018/T-019/T-021 are done. ADR, architecture, component and SCN-003 updated for R-014; diagrams rendered; chunks recorded.
- 2026-11-18: CP-1 awaiting_review (R-01); checks 22 PASS/0 FAIL/1 MANUAL/0 UNCHECKED; usage n/a; reviewer user
- b93fec1: CP-1 snapshot
- 2026-11-18: CP-F awaiting_review (R-01); checks 23 PASS/0 FAIL/1 MANUAL/0 UNCHECKED; usage in=48057 out=18854 cr=272822 ratio=5.7x min=34; reviewer user
- a4899c2: CP-F snapshot
