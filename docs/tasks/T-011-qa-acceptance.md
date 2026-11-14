# T-011: Q-02 QA and acceptance

| Field | Value |
|---|---|
| id | T-011 |
| title | Q-02 QA and acceptance |
| workflow | feature |
| stage | Q-02 |
| role | qa |
| status | pending |
| hold | false |
| depends_on | T-010, T-009, T-008, T-007, T-005 |
| inputs | `docs/qa/e2e-plan-*.md`, `tests/e2e/**`, `docs/architecture/contracts-ai-task-tracker.md`, `docs/ux/ui-spec-ai-task-tracker.md` |
| artifacts | `docs/qa/e2e-report-*.md`, `docs/qa/review-T-011.md`, `docs/qa/vision-review-*` |
| e2e | required - run and report |
| ux | required - visual review of the prototype screens |
| chunks | ledger `docs/chunks/T-011.json` (single source of truth for per-chunk state); code/artifact review, e2e evidence per UC batch, findings and verdict, vision review |
| budget | `max_write_kb` / `max_session_kb` / `context_stop_pct` / `max_turns` / `max_input_tokens` / `max_wall_min`; effective values in the ledger `budget` object |
| base_commit | - |
| usage | - |
| issues | - |
| commit | pending |

## Acceptance

- [ ] The review covers the card acceptance and the contracts, not the whole source tree at once.
- [ ] Each UC batch has pass/fail evidence from the runner output.
- [ ] The verdict names blocking, major, and minor findings; no finding is skipped.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-1 | review and evidence | `docs/qa/review-T-011.md`, `docs/qa/e2e-report-*.md` | - | pending | `docs/checkpoints/T-011-CP-1.md` |
| CP-2 | verdict and vision | `docs/qa/e2e-report-*.md`, `docs/qa/vision-review-*` | - | pending | `docs/checkpoints/T-011-CP-2.md` |
| CP-F | final acceptance | all task artifacts | - | pending | `docs/checkpoints/T-011-CP-F.md` |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.

## Blockers

- none

## Log

- 2026-11-14: card created during the 3.0.0 rollout (T-046) so the board row has a card and a chunk ledger before the task starts.
