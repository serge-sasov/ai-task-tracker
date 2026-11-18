# T-021: U-02 Wireframes, prototype and UI spec for the model column

| Field | Value |
|---|---|
| id | T-021 |
| title | U-02 Wireframes, prototype and UI spec: S-01 model column |
| workflow | feature |
| stage | U-02 |
| role | ux-designer |
| status | awaiting_review |
| hold | false |
| depends_on | T-020 |
| inputs | `docs/tasks/T-020-ux-model-column.md`, `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/ux/screens-ai-task-tracker.md`, `docs/ux/ui-spec-ai-task-tracker.md` |
| artifacts | `docs/ux/wireframes-ai-task-tracker.puml`, `docs/ux/wireframes-ai-task-tracker.svg`, `docs/ux/ui-spec-ai-task-tracker.md`, `docs/ux/prototype/ai-task-tracker/index.html`, `docs/ux/prototype/ai-task-tracker/uc-e2e-*.html`, `docs/ux/prototype/ai-task-tracker/assets/*` |
| e2e | required - UC-E2E-003 S8 asserts the model cell; the prototype mirrors the dataset |
| ux | required - this stage delivers the Salt wireframe, UI spec and offline prototype for R-014 |
| chunks | `docs/chunks/T-021.json` (C1 wireframes, C2 UI spec, C3 prototype) |
| budget | max_write 80 KB / session 200 KB; three chunks |
| base_commit | e2ee9b2 |
| usage | n/a |
| issues | - |
| commit | 55e1b32 |

## Acceptance

- [x] The S-01 Salt wireframe shows the model column immediately after "Название", with `qwen3.8:27b +1` and "Нет данных" for TASK-002.
- [x] `docs/ux/wireframes-ai-task-tracker.svg` is re-rendered from the updated `.puml`.
- [x] The UI spec defines the column order, primary model, `+N`, tooltip/aria full list, no-usage state and truncation.
- [x] The offline prototype `uc-e2e-003.html` shows the model column through `assets/app.js` with `+1` and "Нет данных"; no network or storage APIs are added.
- [x] All 8 UC-E2E prototype pages remain present, linked from the index and offline.
- [x] Chunks C1, C2 and C3 are `done` with produced hashes in `docs/chunks/T-021.json`.
- [x] `CP-2` (wireframes, prototype, UI spec) and `CP-F` (final acceptance) are declared.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-2 | U-02 wireframes, prototype, UI spec | wireframes `.puml/.svg`, UI spec, prototype pages/assets | 18P/0F/1M | awaiting_review | docs/checkpoints/T-021-CP-2.md |
| CP-F | final acceptance | all task artifacts | - | pending | `docs/checkpoints/T-021-CP-F.md` |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.

## Chunk plan

| Chunk | Stage | Role | Title | Owns | State |
|---|---|---|---|---|---|
| C1 | U-02 | ux-designer | Salt wireframes with the S-01 model column | `docs/ux/wireframes-ai-task-tracker.puml`, `docs/ux/wireframes-ai-task-tracker.svg` | done |
| C2 | U-02 | ux-designer | UI spec for the model cell | `docs/ux/ui-spec-ai-task-tracker.md` | done |
| C3 | U-02 | ux-designer | Offline prototype model column | `docs/ux/prototype/ai-task-tracker/index.html`, `docs/ux/prototype/ai-task-tracker/uc-e2e-*.html`, `docs/ux/prototype/ai-task-tracker/assets/*` | done |

## Log

- 2026-11-18: T-021 started on base `e2ee9b2`; T-020 is done. Wireframes, UI spec and prototype updated for R-014; SVG rendered; chunks recorded.
- 2026-11-18: CP-2 awaiting_review (U-02); checks 18 PASS/0 FAIL/1 MANUAL/0 UNCHECKED; usage n/a; reviewer user
- 55e1b32: CP-2 snapshot
