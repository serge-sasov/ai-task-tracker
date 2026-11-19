# T-029 CP-F: P-00 Feature planning: S-04 calendar day model column

| Field | Value |
|---|---|
| task | T-029 |
| workflow | feature |
| bug | - |
| checkpoint | CP-F |
| role | orchestrator |
| stage | P-00 |
| artifacts | docs/plan.md, docs/tasks/T-029-calendar-day-model-planning.md, docs/activity.puml, docs/activity.svg |
| status | approved |
| requested_at | 2026-11-19T17:21:00 |
| reviewed_at | 2026-11-19T17:30:53 |
| reviewer | user |
| decision | approved |
| snapshot_commit | 07a2f27 |
| generated_by | stage-done/1 |
| checks_file | docs/checkpoints/T-029-CP-F.checks.json |

## Review scope

- Stage `P-00` acceptance for T-029.
- Scope fixed: S-04 day view (`/calendar/:date`) shows a visible "Модель" column immediately after "Задача"; it shows the primary model plus `+N`, the full ordered list in `title` and `aria-label`, and "Нет данных" when `models` is empty.
- Reuse: no backend/API/schema change is expected; `GET /api/tasks` already returns `models` per task and `CalendarDayView.vue` already calls it. Confirm against contracts §4.2/§4.5 before planning.
- Consistency: the S-04 cell follows the S-01 model-cell rules and does not change S-01 behavior; if a shared component or composable is introduced, the S-01 regression is covered.
- e2e applicability recorded (`required`) with a concrete UC-E2E-005 delta and YAML dataset update; evidence must verify the actually built frontend (rebuilt image or DOM smoke), not only the offline prototype.
- UX applicability recorded (`required`) with an S-04 screens/UI-spec/prototype delta; the UX deliverable includes the model-cell states and accessibility text.
- Task graph is created with explicit dependencies and per-stage applicability; every waiver has a reason; no silent skip of required stages.
- Launch protocol (anti-hang) is included in this card and copied into every successor card created at CP-1.
- Existing pending baseline tasks T-010, T-011 and T-012 are untouched.
- `CP-1` (plan and task graph) and `CP-F` (final acceptance) are declared.

## Checks

| ID | Check | Result | Detail |
|---|---|---|---|
| artifacts-exist | file-exists | PASS | 4 file(s) |
| artifact-hashes | hash-match | NOT_APPLICABLE | ledger has only harness-managed artifacts |
| plan-board | headings-required | PASS | 1 file(s) |
| cards-exist | file-exists | PASS | 39 file(s) |
| card-fields | card-fields | PASS | 39 card(s) |
| deps-acyclic | deps-acyclic | PASS | 39 card(s) |

Summary: 5 PASS, 0 FAIL, 0 MANUAL, 1 N/A, 0 UNCHECKED.

## Usage

| Sessions | Input | Output | Reasoning | Cache read | Cache/Input | Minutes | Cost |
|---|---|---|---|---|---|---|---|
| 5 | 379281 | 53938 | 0 | 2191589 | 5.8x | 220 | 0.00 |

## Diff stat

```
docs/activity.puml                              | 14 ++++-
 docs/activity.svg                               |  2 +-
 docs/checkpoints/T-029-CP-1.checks.json         | 56 ++++++++++++++++++
 docs/checkpoints/T-029-CP-1.md                  | 73 ++++++++++++++++++++++++
 docs/evidence/T-029.md                          | 10 ++++
 docs/plan.md                                    | 14 ++++-
 docs/tasks/T-029-calendar-day-model-planning.md | 76 +++++++++++++++++++++++++
 docs/tasks/T-030-requirements-s04-model.md      | 58 +++++++++++++++++++
 docs/tasks/T-031-e2e-s04-model.md               | 58 +++++++++++++++++++
 docs/tasks/T-032-ux-flows-s04-model.md          | 58 +++++++++++++++++++
 docs/tasks/T-033-ux-prototype-s04-model.md      | 58 +++++++++++++++++++
 docs/tasks/T-034-architecture-s04-model.md      | 59 +++++++++++++++++++
 docs/tasks/T-035-contracts-s04-model.md         | 59 +++++++++++++++++++
 docs/tasks/T-036-implement-s04-model.md         | 58 +++++++++++++++++++
 docs/tasks/T-037-e2e-s04-model.md               | 58 +++++++++++++++++++
 docs/tasks/T-038-e2e-plan-s04-model.md          | 58 +++++++++++++++++++
 docs/tasks/T-039-qa-s04-model.md                | 58 +++++++++++++++++++
 docs/tasks/T-040-docs-s04-model.md              | 58 +++++++++++++++++++
 18 files changed, 882 insertions(+), 3 deletions(-)
```

## User remarks

- Approved via chat: "decision: approved" (T-029 CP-F final acceptance).

## User edits

- none

## Role response

- Machine checks: 0 FAIL, 0 UNCHECKED, 0 MANUAL.

## Next action

- return to this checkpoint