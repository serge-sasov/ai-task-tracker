# T-018 CP-F: A-01 Requirements baseline: S-01 model column

| Field | Value |
|---|---|
| task | T-018 |
| workflow | feature |
| bug | - |
| checkpoint | CP-F |
| role | analyst |
| stage | A-01 |
| artifacts | docs/analysis/requirements-ai-task-tracker.md, docs/analysis/glossary.md |
| status | approved |
| requested_at | 2026-11-18T10:00:25 |
| reviewed_at | 2026-11-18T10:00:25 |
| reviewer | auto (explicit -AutoApprove) |
| decision | approved |
| snapshot_commit | - |
| generated_by | stage-done/1 |
| checks_file | docs/checkpoints/T-018-CP-F.checks.json |

## Review scope

- Stage `A-01` acceptance for T-018.
- `R-014` is added as an atomic Must requirement with description, source, acceptance and dependencies.
- `R-014` traces to source line ИТ:4 (3) and is consistent with R-012/R-013; no duplicate requirement is introduced.
- The glossary defines the primary model, the ordered model list, the `+N` indicator and the no-usage state, including the Russian UI labels "Модель" and "Нет данных".
- No new blocking question is opened; assumptions Q-006 (Russian UI) and the T-017 contract remain unchanged.
- T-017 decisions are preserved: `models: string[]`, total-token ordering with name tie-break, empty array for no usage, no database migration, D-01 waived.
- `CP-1` (requirements baseline) and `CP-F` (final acceptance) are declared.
- Chunk C1 is `done` and the produced file hashes are recorded in `docs/chunks/T-018.json`.

## Chunks

| Chunk | State | Depth | Role | Produced | Title |
|---|---|---|---|---|---|
| C1 | done | d1 | analyst | 2 | Requirements and glossary for the model column |

Ledger: `docs/chunks/T-018.json`

## Checks

| ID | Check | Result | Detail |
|---|---|---|---|
| artifacts-exist | file-exists | PASS | 2 file(s) |
| artifact-hashes | hash-match | PASS | 2 artifact(s) match ledger |
| requirements-exist | file-exists | PASS | 1 file(s) |
| requirements-ids | ids-unique | PASS | 25 unique id(s) |
| requirements-atomic | pattern-present | PASS | 1 pattern(s) in 1 file(s) |
| open-questions | file-exists | PASS | 1 file(s) |
| chunk-ownership-no-overlap | chunk-ledger | PASS | 1 chunk(s), rule ownership |
| chunk-coverage | chunk-ledger | PASS | 1 chunk(s), rule coverage |
| chunk-budget | chunk-ledger | PASS | 1 chunk(s) within 32/64 KB |

Summary: 9 PASS, 0 FAIL, 0 MANUAL, 0 N/A, 0 UNCHECKED.

## Usage

- token usage not available (opencode CLI or database unavailable)

## Diff stat

```
docs/activity.puml                            |  2 +-
 docs/activity.svg                             |  2 +-
 docs/analysis/glossary.md                     |  8 ++-
 docs/analysis/requirements-ai-task-tracker.md | 23 ++++++--
 docs/checkpoints/T-018-CP-1.checks.json       | 74 +++++++++++++++++++++++++
 docs/checkpoints/T-018-CP-1.md                | 80 +++++++++++++++++++++++++++
 docs/chunks/T-018.json                        | 69 +++++++++++++++++++++++
 docs/evidence/T-018.md                        |  8 +++
 docs/plan.md                                  |  2 +-
 docs/tasks/T-018-requirements-model-column.md | 72 ++++++++++++++++++++++++
 10 files changed, 331 insertions(+), 9 deletions(-)
```

## User remarks

- (user fills; may be empty if artifacts were edited directly)

## User edits

- none

## Role response

- Machine checks: 0 FAIL, 0 UNCHECKED, 0 MANUAL.

## Next action

- accept task