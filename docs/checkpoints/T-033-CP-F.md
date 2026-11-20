# T-033 CP-F: U-02 Wireframes, prototype and UI spec for S-04 model column

| Field | Value |
|---|---|
| task | T-033 |
| workflow | feature |
| bug | - |
| checkpoint | CP-F |
| role | ux-designer |
| stage | U-02 |
| artifacts | docs/ux/wireframes-ai-task-tracker.puml, docs/ux/wireframes-ai-task-tracker.svg, docs/ux/ui-spec-ai-task-tracker.md, docs/ux/prototype/ai-task-tracker/uc-e2e-005.html |
| status | awaiting_review |
| requested_at | 2026-11-20T13:42:09 |
| reviewed_at | - |
| reviewer | user |
| decision | - |
| snapshot_commit | - |
| generated_by | stage-done/1 |
| checks_file | docs/checkpoints/T-033-CP-F.checks.json |

## Review scope

- Stage `U-02` acceptance for T-033.
- `docs/ux/ui-spec-ai-task-tracker.md` is extended with the S-04 day-view "Модель" cell specification: primary model, `+N` badge, `title`/`aria-label` full ordered list, and "Нет данных", with the same tokens/labels as the S-01 cell.
- The prototype page `docs/ux/prototype/ai-task-tracker/uc-e2e-005.html` is updated to render the S-04 day table with the model column in all four states, and the page links and structure remain valid (proto-structure, proto-links, proto-offline checks).
- The wireframes `wireframes-ai-task-tracker.puml` are extended with the S-04 day view and re-rendered to `wireframes-ai-task-tracker.svg`; the diagram passes syntax and SVG checks.
- The UI spec traces the S-04 model cell to UC-E2E-005 and `R-015`, and reuses the S-01 model-cell accessibility text; no conflicting token is introduced.
- The prototype is the single-user Russian variant and does not depend on external assets (offline check passes).
- `CP-2` (wireframes, prototype, UI spec) and `CP-F` (final acceptance) are declared.

## Chunks

| Chunk | State | Depth | Role | Produced | Title |
|---|---|---|---|---|---|
| T-033-c1 | done | d1 | ux-designer | 1 | UI spec S-04 model cell section |
| T-033-c2 | done | d1 | ux-designer | 2 | Wireframes S-04 day view + SVG re-render |
| T-033-c3 | done | d1 | ux-designer | 1 | Prototype uc-e2e-005.html day table with model column |

Ledger: `docs/chunks/T-033.json`

## Checks

| ID | Check | Result | Detail |
|---|---|---|---|
| artifacts-exist | file-exists | PASS | 4 file(s) |
| artifact-hashes | hash-match | PASS | 4 artifact(s) match ledger |
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
| proto-local-links | html-links-exist | PASS | 136 local link(s) |
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

| Sessions | Input | Output | Reasoning | Cache read | Cache/Input | Minutes | Cost |
|---|---|---|---|---|---|---|---|
| 11 | 644320 | 163073 | 0 | 4571594 | 7.1x | 307 | 0.00 |

## Diff stat

```
docs/activity.puml                                 |   2 +-
 docs/activity.svg                                  |   2 +-
 docs/checkpoints/T-033-CP-2.checks.json            | 140 ++++++++++++++++++
 docs/checkpoints/T-033-CP-2.md                     | 112 ++++++++++++++
 docs/chunks/T-033.json                             | 162 +++++++++++++++++++++
 docs/evidence/T-033.md                             |  14 ++
 ...-measures-final-file-size-vs-authored-output.md |  81 +++++++++++
 docs/issues/README.md                              |   1 +
 docs/plan.md                                       |   2 +-
 docs/tasks/T-033-ux-prototype-s04-model.md         |  23 ++-
 docs/ux/prototype/ai-task-tracker/uc-e2e-005.html  |  20 ++-
 docs/ux/ui-spec-ai-task-tracker.md                 |  13 +-
 docs/ux/wireframes-ai-task-tracker.puml            |   9 ++
 docs/ux/wireframes-ai-task-tracker.svg             |   2 +-
 14 files changed, 563 insertions(+), 20 deletions(-)
```

## User remarks

- (user fills; may be empty if artifacts were edited directly)

## User edits

- none

## Role response

- Machine checks: 0 FAIL, 0 UNCHECKED, 1 MANUAL.

## Next action

- return to this checkpoint