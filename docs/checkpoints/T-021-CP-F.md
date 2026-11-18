# T-021 CP-F: U-02 Wireframes, prototype and UI spec: S-01 model column

| Field | Value |
|---|---|
| task | T-021 |
| workflow | feature |
| bug | - |
| checkpoint | CP-F |
| role | ux-designer |
| stage | U-02 |
| artifacts | docs/ux/wireframes-ai-task-tracker.puml, docs/ux/wireframes-ai-task-tracker.svg, docs/ux/ui-spec-ai-task-tracker.md, docs/ux/prototype/ai-task-tracker/index.html, docs/ux/prototype/ai-task-tracker/uc-e2e-*.html, docs/ux/prototype/ai-task-tracker/assets/* |
| status | awaiting_review |
| requested_at | 2026-11-18T10:12:17 |
| reviewed_at | - |
| reviewer | user |
| decision | - |
| snapshot_commit | e5590d5 |
| generated_by | stage-done/1 |
| checks_file | docs/checkpoints/T-021-CP-F.checks.json |

## Review scope

- Stage `U-02` acceptance for T-021.
- The S-01 Salt wireframe shows the model column immediately after "Название", with `qwen3.8:27b +1` and "Нет данных" for TASK-002.
- `docs/ux/wireframes-ai-task-tracker.svg` is re-rendered from the updated `.puml`.
- The UI spec defines the column order, primary model, `+N`, tooltip/aria full list, no-usage state and truncation.
- The offline prototype `uc-e2e-003.html` shows the model column through `assets/app.js` with `+1` and "Нет данных"; no network or storage APIs are added.
- All 8 UC-E2E prototype pages remain present, linked from the index and offline.
- Chunks C1, C2 and C3 are `done` with produced hashes in `docs/chunks/T-021.json`.
- `CP-2` (wireframes, prototype, UI spec) and `CP-F` (final acceptance) are declared.

## Chunks

| Chunk | State | Depth | Role | Produced | Title |
|---|---|---|---|---|---|
| C1 | done | d1 | ux-designer | 2 | Salt wireframes with the S-01 model column |
| C2 | done | d1 | ux-designer | 1 | UI spec for the model cell |
| C3 | done | d1 | ux-designer | 11 | Offline prototype model column |

Ledger: `docs/chunks/T-021.json`

## Checks

| ID | Check | Result | Detail |
|---|---|---|---|
| artifacts-exist | file-exists | PASS | 14 file(s) |
| artifact-hashes | hash-match | PASS | 14 artifact(s) match ledger |
| wireframes-exist | file-exists | PASS | 1 file(s) |
| wireframe-syntax | puml-syntax | PASS | 1 diagram(s) |
| wireframe-svg | svg-fresh | PASS | 1 SVG(s) |
| wireframe-coverage | id-coverage | PASS | 6 id(s) covered by 1 file(s) |
| ui-spec-exist | file-exists | PASS | 1 file(s) |
| ui-spec-uc-coverage | id-coverage | PASS | 8 id(s) covered by 1 file(s) |
| proto-index | file-exists | PASS | 1 file(s) |
| proto-page-count | count-match | PASS | 8 = 8 |
| proto-structure | html-structure | PASS | 8 page(s) |
| proto-links | index-pages-links | PASS | 1 index, 8 page(s) |
| proto-local-links | html-links-exist | PASS | 135 local link(s) |
| proto-offline | pattern-absent | PASS | 7 pattern(s) absent in 11 file(s) |
| proto-assets | file-exists | PASS | 2 file(s) |
| ux-traceability | id-coverage | PASS | 8 id(s) covered by 10 file(s) |
| proto-ru-single-user | manual | MANUAL | Прототип: интерфейс и тексты на русском, один пользователь без логина, ролей и сетевых запросов |
| chunk-ownership-no-overlap | chunk-ledger | PASS | 3 chunk(s), rule ownership |
| chunk-coverage | chunk-ledger | PASS | 3 chunk(s), rule coverage |
| chunk-budget | chunk-ledger | PASS | 3 chunk(s) within 80/200 KB |

Summary: 19 PASS, 0 FAIL, 1 MANUAL, 0 N/A, 0 UNCHECKED.

## Manual review

- proto-ru-single-user: Прототип: интерфейс и тексты на русском, один пользователь без логина, ролей и сетевых запросов

## Usage

- token usage not available (opencode CLI or database unavailable)

## Diff stat

```
docs/activity.puml                                |   2 +-
 docs/activity.svg                                 |   2 +-
 docs/checkpoints/T-021-CP-2.checks.json           | 140 ++++++++++++++++
 docs/checkpoints/T-021-CP-2.md                    |  97 +++++++++++
 docs/chunks/T-021.json                            | 188 ++++++++++++++++++++++
 docs/evidence/T-021.md                            |  20 +++
 docs/plan.md                                      |   2 +-
 docs/tasks/T-021-prototype-model-column.md        |  56 +++++++
 docs/ux/prototype/ai-task-tracker/assets/app.css  |   3 +
 docs/ux/prototype/ai-task-tracker/assets/app.js   |  26 ++-
 docs/ux/prototype/ai-task-tracker/index.html      |   2 +-
 docs/ux/prototype/ai-task-tracker/uc-e2e-003.html |   2 +-
 docs/ux/ui-spec-ai-task-tracker.md                |  25 ++-
 docs/ux/wireframes-ai-task-tracker.puml           |  16 +-
 docs/ux/wireframes-ai-task-tracker.svg            |   2 +-
 15 files changed, 560 insertions(+), 23 deletions(-)
```

## User remarks

- (user fills; may be empty if artifacts were edited directly)

## User edits

- none

## Role response

- Machine checks: 0 FAIL, 0 UNCHECKED, 1 MANUAL.

## Next action

- return to this checkpoint