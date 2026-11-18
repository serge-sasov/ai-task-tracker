# T-019: A-02 E2E use cases and dataset for the model column

| Field | Value |
|---|---|
| id | T-019 |
| title | A-02 E2E use cases and dataset: S-01 model column |
| workflow | feature |
| stage | A-02 |
| role | analyst |
| status | done |
| hold | false |
| depends_on | T-018 |
| inputs | `docs/tasks/T-018-requirements-model-column.md`, `docs/analysis/requirements-ai-task-tracker.md`, `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/analysis/e2e-test-data-ai-task-tracker.yaml`, `docs/tasks/T-017-task-list-model-planning.md` |
| artifacts | `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/analysis/e2e-test-data-ai-task-tracker.yaml` |
| e2e | required - this is the e2e use-case and dataset stage for R-014; UC-E2E-003 is extended with the S-01 model assertions |
| ux | required - R-014 is visible in S-01; U-01/U-02 consume the updated column, `+N`, tooltip/aria and no-usage state |
| chunks | `docs/chunks/T-019.json` (C1: use case; C2: YAML dataset) |
| budget | max_write 64 KB / session 96 KB; two chunks |
| base_commit | 33cd6b9 |
| usage | n/a |
| issues | - |
| commit | 2d0ed43 |

## Acceptance

- [x] UC-E2E-003 has R-014 in its source requirements and a step-11 assertion for the visible model column, `+N`, tooltip/aria full list and "Нет данных".
- [x] The coverage summary includes R-014 and the totals are updated to 31 requirements.
- [x] The YAML dataset includes R-014 in UC-E2E-003 and an S8 step with the per-task ordered model lists.
- [x] `python scripts/validate-e2e-data.py ... --use-cases-md ... --strict` passes with 0 errors.
- [x] T-018's R-014 wording and the T-017 contract remain unchanged.
- [x] Chunks C1 and C2 are `done` with produced hashes in `docs/chunks/T-019.json`.
- [x] `CP-2` (e2e use cases and dataset) and `CP-F` (final acceptance) are declared.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-2 | A-02 e2e use cases and dataset | `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/analysis/e2e-test-data-ai-task-tracker.yaml` | 9P/0F/0M | approved | docs/checkpoints/T-019-CP-2.md |
| CP-F | final acceptance | all task artifacts | 10P/0F/0M | approved | docs/checkpoints/T-019-CP-F.md |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.

## Chunk plan

| Chunk | Stage | Role | Title | Owns | State |
|---|---|---|---|---|---|
| C1 | A-02 | analyst | UC-E2E-003 model-column use case | `docs/analysis/e2e-use-cases-ai-task-tracker.md` | done |
| C2 | A-02 | analyst | YAML dataset S8 model assertions | `docs/analysis/e2e-test-data-ai-task-tracker.yaml` | done |

## Log

- 2026-11-18: T-019 started on base `33cd6b9`; T-018 is done. UC-E2E-003 and the YAML dataset extended for R-014; validator passed.
- 2026-11-18: CP-2 approved (A-02); checks 9 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage n/a; reviewer auto (P2)
- f243632: CP-2 snapshot
- 2026-11-18: CP-F approved (A-02); checks 10 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage n/a; reviewer auto (explicit -AutoApprove)
- 2d0ed43: CP-F snapshot
