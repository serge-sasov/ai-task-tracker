# T-004: UX flows and screens (U-01)

| Field | Value |
|---|---|
| id | T-004 |
| title | UX flows and screens (U-01) |
| workflow | feature |
| stage | U-01 |
| role | ux-designer |
| status | done |
| depends_on | T-002 (done), T-003 (done) |
| inputs | `docs/analysis/requirements-ai-task-tracker.md`, `docs/analysis/questions-ai-task-tracker.md`, `docs/analysis/glossary.md`, `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/analysis/e2e-test-data-ai-task-tracker.yaml`, `requirements/исходные требования.txt` |
| artifacts | `docs/ux/user-flows-ai-task-tracker.md`, `docs/ux/screens-ai-task-tracker.md` |
| e2e | required — UX flows must map 1:1 to e2e use cases UC-E2E-001..008 (consumed by Q-01 coverage matrix); this stage defines the user journeys the e2e tests will exercise |
| ux | required — U-01 is the UX stage for the user-facing web interface (calendar view, AI search, token dashboard); produces user flows and screen inventory/states |
| chunks | 3 — UX Markdown docs: 2-3 sections per chunk; write a skeleton first, then fill one section per edit; do not create a large artifact in one write |
| budget | max_write_kb: 8 / max_session_kb: 20 / context_stop_pct: 60 (per `harness/process.md` §11.1) |
| issues | - |
| commit | - (recorded after closure commit) |

## Acceptance

- [x] User flows: personas, primary journeys, information architecture, and navigation between screens; each journey mapped to at least one e2e use case (UC-E2E-NNN) for traceability. — verified at CP-F: `docs/ux/user-flows-ai-task-tracker.md` (P-1/P-2, F-1..F-8 + BF-1/BF-2, IA §2, UC-E2E-001..008 coverage §5).
- [x] Screen inventory: every screen with a stable screen ID, purpose, primary content, and key interactions. — verified at CP-F: `docs/ux/screens-ai-task-tracker.md` §1 (S-01..S-06).
- [x] Screen states: for each screen, the states (loading/empty/error/success) and what triggers each. — verified at CP-F: `docs/ux/screens-ai-task-tracker.md` §2.
- [x] Traceability: a mapping table screen ID <-> e2e UC <-> source requirements (R-NNN) so Q-01 can build the coverage matrix. — verified at CP-F: `docs/ux/screens-ai-task-tracker.md` §5 + `docs/ux/user-flows-ai-task-tracker.md` §5.
- [x] Russian UI copy respected (per Q-006 resolution: «по умолчанию русский»); single-user scope (Q-004 default) reflected. — verified at CP-F: canonical RU copy table (screens §4), scope header in both artifacts.
- [x] No contradictions with the resolved Q-NNN decisions and the R-NNN requirements. — verified at CP-F: screens §6 (C-UX-1..C-UX-3 закрыт; open Q-UX-1/Q-UX-2 non-blocking, carried to T-006/T-005).

## Checkpoints

| ID | Stage | Artifacts | Status | Review file |
|---|---|---|---|---|
| CP-1 | U-01 flows and screens | `docs/ux/user-flows-ai-task-tracker.md`, `docs/ux/screens-ai-task-tracker.md` | approved (2026-11-12, explicit chat decision relayed by the lead) | `docs/checkpoints/T-004-CP-1.md` |
| CP-F | final acceptance | all task artifacts | approved (2026-11-12, same explicit decision) | `docs/checkpoints/T-004-CP-F.md` |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.

## Blockers

- none

## Log

- 2026-11-12: T-004 started (U-01 UX flows and screens); card created; T-004 -> in_progress; board and diagram updated; inputs T-002/T-003 done.
- 2026-11-12: U-01 artifacts delivered: `docs/ux/user-flows-ai-task-tracker.md` (personas P-1/P-2, IA, flows F-1..F-8 + background BF-1..BF-2, traceability to UC-E2E-001..008, assumptions/open questions), `docs/ux/screens-ai-task-tracker.md` (registry S-01..S-06, states loading/empty/success/error, a11y, RU copy, traceability, no contradictions with Q-NNN/R-NNN). CP-1 -> awaiting_review; stopped at checkpoint per process.md (no checkpoint file, no further stages).
- 2026-11-12: orchestrator finalized CP-1: `docs/checkpoints/T-004-CP-1.md` created; card status -> awaiting_review; board and diagram (blue #87CEFA) updated; snapshot committed.
- 2026-11-12: user decision recorded (explicit chat decision of 2026-11-12, relayed by the lead): «consider T-004 CP-1 and CP-F approved» — CP-1 -> approved; `docs/checkpoints/T-004-CP-F.md` created with decision approved; all six acceptance items verified against `docs/ux/user-flows-ai-task-tracker.md` and `docs/ux/screens-ai-task-tracker.md` and marked [x]; card status -> done; board -> done (green #90EE90); open non-blocking Q-UX-1/Q-UX-2 carried to T-006/T-005.
