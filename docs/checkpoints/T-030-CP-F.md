# T-030 CP-F: A-01 Requirements delta for S-04 calendar day model column

| Field | Value |
|---|---|
| task | T-030 |
| workflow | feature |
| bug | - |
| checkpoint | CP-F |
| role | analyst |
| stage | A-01 |
| artifacts | docs/analysis/requirements-ai-task-tracker.md, docs/analysis/questions-ai-task-tracker.md, docs/analysis/glossary.md |
| status | awaiting_review |
| requested_at | 2026-11-19T18:52:19 |
| reviewed_at | - |
| reviewer | user |
| decision | - |
| snapshot_commit | e031a8f |
| generated_by | stage-done/1 |
| checks_file | docs/checkpoints/T-030-CP-F.checks.json |

## Review scope

- Stage `A-01` acceptance for T-030.
- `R-015` is added as an atomic Must requirement: S-04 day view (`/calendar/:date`) shows a "Модель" column after "Задача" with the primary model, `+N` when more than one, the full ordered list in `title`/`aria-label`, and "Нет данных" when `models` is empty.
- `R-015` traces to the S-04 source line and to UC-E2E-005; it is consistent with `R-014` (S-01) and reuses the same ordering/tie-break rule; no duplicate requirement is introduced.
- The glossary is extended with the S-04 day-view context (day table, day-view model cell) and reuses the existing S-01 terms (primary model, `+N`, "Нет данных"); no new conflicting term is added.
- No new blocking question is opened; assumptions (Russian UI, no backend change, `GET /api/tasks` already returns `models`) are preserved from T-029.
- The requirement explicitly states no backend/API/schema change is required and the S-01 behavior is unchanged.
- `CP-1` (requirements delta) and `CP-F` (final acceptance) are declared.

## Chunks

| Chunk | State | Depth | Role | Produced | Title |
|---|---|---|---|---|---|
| A1 | done | d1 | analyst | 3 | R-015 S-04 day-view model column requirements delta |

Ledger: `docs/chunks/T-030.json`

## Checks

| ID | Check | Result | Detail |
|---|---|---|---|
| artifacts-exist | file-exists | PASS | 3 file(s) |
| artifact-hashes | hash-match | PASS | 3 artifact(s) match ledger |
| requirements-exist | file-exists | PASS | 1 file(s) |
| requirements-ids | ids-unique | PASS | 26 unique id(s) |
| requirements-atomic | pattern-present | PASS | 1 pattern(s) in 1 file(s) |
| open-questions | file-exists | PASS | 1 file(s) |
| chunk-ownership-no-overlap | chunk-ledger | PASS | 1 chunk(s), rule ownership |
| chunk-coverage | chunk-ledger | PASS | 1 chunk(s), rule coverage |
| chunk-budget | chunk-ledger | PASS | 1 chunk(s) within 32/64 KB |

Summary: 9 PASS, 0 FAIL, 0 MANUAL, 0 N/A, 0 UNCHECKED.

## Usage

| Sessions | Input | Output | Reasoning | Cache read | Cache/Input | Minutes | Cost |
|---|---|---|---|---|---|---|---|
| 5 | 269947 | 41094 | 0 | 3590810 | 13.3x | 116 | 0.00 |

## Diff stat

```
docs/checkpoints/T-030-CP-1.md                     |  2 +-
 ...l-file-removal-cmdlet-denied-in-analyst-role.md | 53 ++++++++++++++++++++++
 docs/issues/README.md                              |  1 +
 docs/plan.md                                       |  2 +-
 docs/tasks/T-030-requirements-s04-model.md         |  3 +-
 5 files changed, 58 insertions(+), 3 deletions(-)
```

## User remarks

- (user fills; may be empty if artifacts were edited directly)

## User edits

- none

## Role response

- Machine checks: 0 FAIL, 0 UNCHECKED, 0 MANUAL.

## Next action

- return to this checkpoint