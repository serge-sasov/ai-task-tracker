# T-017: P-00 Feature planning - S-01 model column (D-01 waived)

| Field | Value |
|---|---|
| id | T-017 |
| title | P-00 Feature planning: S-01 task list model column (D-01 waived) |
| workflow | feature |
| stage | P-00 |
| role | orchestrator |
| status | done |
| hold | false |
| depends_on | - |
| inputs | `docs/plan.md`, `harness/process.md`, `harness/workflows/feature.md`, `docs/analysis/requirements-ai-task-tracker.md` |
| artifacts | `docs/plan.md`, `docs/tasks/T-017-task-list-model-planning.md`, `docs/activity.puml`, `docs/activity.svg` |
| e2e | required - new visible S-01 list behavior and a new `GET /api/tasks` response field; A-02 / I-02 / Q-01 / Q-02 are in the graph |
| ux | required - visible S-01 column, `+N` indicator, tooltip/aria full list, and the no-usage state; U-01 / U-02 are in the graph |
| chunks | - (P-00 planning produces board, card and checkpoints; no ledger) |
| budget | - |
| base_commit | 5abdd53 |
| usage | in=1341131 out=201058 cr=2551015 ratio=1.9x min=327 |
| issues | ISSUE-20261117-232320 |
| commit | c45792a |

## Acceptance

- [x] Scope is fixed: S-01 gets a visible "Модель" column immediately after "Название"; it shows the primary model plus `+N`, the full ordered list in tooltip and `aria-label`, and "Нет данных" when there is no usage.
- [x] API contract is fixed: `TaskListItem` gets `models: string[]` for `GET /api/tasks`, ordered by total tokens (input+output+cache) descending with model-name tie-break; an empty array means no usage; existing fields stay unchanged; no DB migration.
- [x] e2e applicability decided with a reason (`e2e: required`, see field above); A-02, I-02, Q-01 and Q-02 are in the graph.
- [x] UX applicability decided with a reason (`ux: required`, see field above); U-01 and U-02 are in the graph.
- [x] D-01 is explicitly waived for this feature: README/changelog do not exist and are owned by the already-pending baseline T-012 D-01; documentation for this change is covered by requirements, UX spec, architecture/contracts, OpenAPI and QA evidence.
- [x] Task graph built from the feature workflow stage table: `P-00 -> A-01 -> A-02 -> U-01 -> U-02 -> R-01 -> R-02 -> I-01 -> I-02 -> Q-01 -> Q-02 -> F-01` (D-01 waived).
- [x] All P-00 successor tasks are registered as `pending` rows in `docs/plan.md` (T-018..T-028) with explicit dependencies. No successor cards are created at this checkpoint; cards are created at the start of each task.
- [x] Existing pending tasks T-010, T-011 and T-012 are untouched.
- [x] Pre-flight commits are recorded: 5f8d5a7 (harness CRLF fix), b11428c (calendar web fix), 9937007 (phase artifacts diagram), 5abdd53 (card parser/deps fix).
- [x] `CP-1` (plan and task graph) and `CP-F` (final acceptance) are declared in the checkpoints table.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-1 | P-00 plan and task graph | `docs/plan.md`, this card, `docs/activity.puml/.svg` | 5P/0F/0M | approved | docs/checkpoints/T-017-CP-1.md |
| CP-F | final acceptance | all task artifacts | 5P/0F/0M | approved | docs/checkpoints/T-017-CP-F.md |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.

## Blockers

- (none)

## Planning scope (fixed at CP-1)

### Product scope

The S-01 task list must show which model(s) processed each task.

- Column order: Название | Модель | Дата | Продолжительность | Статус.
- Primary model: the model with the largest total tokens (input + output + cache).
- When more than one model has usage, show a visible `+N` indicator next to the primary model. The full ordered list must be available in the tooltip and the accessible name (`aria-label`), not only on hover.
- When a task has no usage rows, show the visible text "Нет данных".
- Long model names must not break the table layout; the UI spec defines truncation/ellipsis and horizontal overflow behavior.

### API and data decisions

- `TaskListItem` gains `models: string[]` for `GET /api/tasks`.
- Order: total tokens descending, then model name ascending for deterministic ties.
- No usage -> empty array. The frontend treats a missing field as an empty array for compatibility.
- Existing response fields are unchanged. Add `models` to the OpenAPI properties without expanding the `required` list.
- No database migration: `token_usage.model` already exists and is populated by ingestion/export.
- `TaskQueryService` already reads `token_usage` rows per task for `tokens_total`; reuse the same rows to compute the ordered models so no extra query is added.

### D-01 waiver

D-01 is not applicable to this feature. The repository has no README or changelog; both are already owned by the pending baseline T-012 D-01. Duplicating that work here would absorb unrelated scope. This feature's documentation is covered by:

- `docs/analysis/requirements-ai-task-tracker.md` (R-014);
- `docs/analysis/e2e-use-cases-ai-task-tracker.md` and the YAML dataset;
- `docs/ux/user-flows-ai-task-tracker.md`, `docs/ux/screens-ai-task-tracker.md`, `docs/ux/ui-spec-ai-task-tracker.md`, wireframes and prototype;
- `docs/architecture/architecture-ai-task-tracker.md`, ADR, contracts and `docs/architecture/openapi-api-001-ai-task-tracker.yaml`;
- `docs/qa/` plan, report and review.

### Task graph (board rows, all pending until P-00 closes)

| Board ID | Stage | Title | Role | Depends on |
|---|---|---|---|---|
| T-018 | A-01 | Requirements baseline for the model column | analyst | T-017 |
| T-019 | A-02 | E2E use cases and dataset for the model column | analyst | T-018 |
| T-020 | U-01 | UX flows and screens for S-01 model display | ux-designer | T-018, T-019 |
| T-021 | U-02 | Wireframes, prototype and UI spec for the model column | ux-designer | T-020 |
| T-022 | R-01 | Solution design for model aggregation and S-01 display | architect | T-018, T-019, T-021 |
| T-023 | R-02 | Contracts and OpenAPI for `models` | architect | T-022 |
| T-024 | I-01 | Implementation and unit tests | coder | T-023, T-021 |
| T-025 | I-02 | E2E automation for the model column | coder | T-024, T-019, T-023 |
| T-026 | Q-01 | E2E plan and coverage matrix | qa | T-019, T-025 |
| T-027 | Q-02 | QA, acceptance and verdict | qa | T-026, T-025, T-024, T-023, T-021 |
| T-028 | F-01 | Final acceptance and closure | orchestrator | T-027 |

D-01 is waived and therefore has no row. T-010, T-011 and T-012 remain pending baseline work and are not part of this graph.

### Pre-flight and harness notes

- The working tree was cleaned before P-00 with separate commits: 5f8d5a7, b11428c, 9937007.
- The harness card parser was fixed in 5abdd53: digit-bearing field names (`e2e`) are parsed, and `depends_on` notes in parentheses are ignored. The issue is recorded in `docs/issues/ISSUE-20261117-232320-harness-card-parser-rejects-e2e-field-and-parenthetical-depe.md`.
- `verify-artifacts.ps1 -SelfTest` passes, including the new `card-fields e2e parsed` and `deps-acyclic parenthetical` cases.

## Log

- 2026-11-17: Pre-flight completed. Frontend build and harness self-test passed; dirty tree split into 5f8d5a7, b11428c and 9937007; temporary T-008 checkpoint preview removed.
- 2026-11-17: Harness card-parser bug fixed in 5abdd53 (digit-bearing field names and annotated `depends_on`), with regression self-tests and issue ISSUE-20261117-232320.
- 2026-11-17: P-00 started. Registered T-017 as `in_progress` and T-018..T-028 as `pending`; recorded the S-01 model scope, e2e/UX applicability, D-01 waiver and task graph. CP-1 prepared.
- 2026-11-18: CP-1 approved (P-00); checks 5 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage in=1341131 out=201058 cr=2551015 ratio=1.9x min=327; reviewer auto (P2)
- c45792a: CP-1 snapshot
- 2026-11-18: CP-F approved (P-00); checks 5 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage in=1341131 out=201058 cr=2551015 ratio=1.9x min=327; reviewer auto (explicit -AutoApprove)
