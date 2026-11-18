# T-017 CP-F: P-00 Feature planning: S-01 task list model column (D-01 waived)

| Field | Value |
|---|---|
| task | T-017 |
| workflow | feature |
| bug | - |
| checkpoint | CP-F |
| role | orchestrator |
| stage | P-00 |
| artifacts | docs/plan.md, docs/tasks/T-017-task-list-model-planning.md, docs/activity.puml, docs/activity.svg |
| status | approved |
| requested_at | 2026-11-18T09:55:27 |
| reviewed_at | 2026-11-18T09:55:27 |
| reviewer | auto (explicit -AutoApprove) |
| decision | approved |
| snapshot_commit | 5a28f17 |
| generated_by | stage-done/1 |
| checks_file | docs/checkpoints/T-017-CP-F.checks.json |

## Review scope

- Stage `P-00` acceptance for T-017.
- Scope is fixed: S-01 gets a visible "Модель" column immediately after "Название"; it shows the primary model plus `+N`, the full ordered list in tooltip and `aria-label`, and "Нет данных" when there is no usage.
- API contract is fixed: `TaskListItem` gets `models: string[]` for `GET /api/tasks`, ordered by total tokens (input+output+cache) descending with model-name tie-break; an empty array means no usage; existing fields stay unchanged; no DB migration.
- e2e applicability decided with a reason (`e2e: required`, see field above); A-02, I-02, Q-01 and Q-02 are in the graph.
- UX applicability decided with a reason (`ux: required`, see field above); U-01 and U-02 are in the graph.
- D-01 is explicitly waived for this feature: README/changelog do not exist and are owned by the already-pending baseline T-012 D-01; documentation for this change is covered by requirements, UX spec, architecture/contracts, OpenAPI and QA evidence.
- Task graph built from the feature workflow stage table: `P-00 -> A-01 -> A-02 -> U-01 -> U-02 -> R-01 -> R-02 -> I-01 -> I-02 -> Q-01 -> Q-02 -> F-01` (D-01 waived).
- All P-00 successor tasks are registered as `pending` rows in `docs/plan.md` (T-018..T-028) with explicit dependencies. No successor cards are created at this checkpoint; cards are created at the start of each task.
- Existing pending tasks T-010, T-011 and T-012 are untouched.
- Pre-flight commits are recorded: 5f8d5a7 (harness CRLF fix), b11428c (calendar web fix), 9937007 (phase artifacts diagram), 5abdd53 (card parser/deps fix).
- `CP-1` (plan and task graph) and `CP-F` (final acceptance) are declared in the checkpoints table.

## Checks

| ID | Check | Result | Detail |
|---|---|---|---|
| artifacts-exist | file-exists | PASS | 4 file(s) |
| artifact-hashes | hash-match | NOT_APPLICABLE | ledger has only harness-managed artifacts |
| plan-board | headings-required | PASS | 1 file(s) |
| cards-exist | file-exists | PASS | 16 file(s) |
| card-fields | card-fields | PASS | 16 card(s) |
| deps-acyclic | deps-acyclic | PASS | 16 card(s) |

Summary: 5 PASS, 0 FAIL, 0 MANUAL, 1 N/A, 0 UNCHECKED.

## Usage

| Sessions | Input | Output | Reasoning | Cache read | Cache/Input | Minutes | Cost |
|---|---|---|---|---|---|---|---|
| 2 | 1341131 | 201058 | 0 | 2551015 | 1.9x | 327 | 0.00 |

## Diff stat

```
docs/activity.puml                           |  14 +++-
 docs/activity.svg                            |   2 +-
 docs/checkpoints/T-017-CP-1.checks.json      |  56 ++++++++++++++
 docs/checkpoints/T-017-CP-1.md               |  74 ++++++++++++++++++
 docs/evidence/T-017.md                       |  10 +++
 docs/plan.md                                 |  14 +++-
 docs/tasks/T-017-task-list-model-planning.md | 111 +++++++++++++++++++++++++++
 7 files changed, 278 insertions(+), 3 deletions(-)
```

## User remarks

- (user fills; may be empty if artifacts were edited directly)

## User edits

- none

## Role response

- Machine checks: 0 FAIL, 0 UNCHECKED, 0 MANUAL.

## Next action

- accept task