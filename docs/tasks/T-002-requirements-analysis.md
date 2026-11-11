# T-002: Requirements analysis (FR/NFR) — «Сервис мониторинга активностей пользователя в opencode»

| Field | Value |
|---|---|
| id | T-002 |
| title | A-01 Requirements analysis: collect/refine atomic FR/NFR (R-NNN), constraints, open questions, glossary |
| workflow | feature |
| stage | A-01 |
| role | analyst |
| status | done |
| depends_on | T-001 |
| inputs | `requirements/исходные требования.txt`, `docs/tasks/T-001-ai-task-tracker-feature-planning.md` (planning scope), `harness/workflows/feature.md` |
| artifacts | `docs/analysis/requirements-ai-task-tracker.md`, `docs/analysis/questions-ai-task-tracker.md`, `docs/analysis/glossary.md` |
| e2e | not applicable for stage A-01 — e2e use cases and the YAML dataset are produced at stage A-02 (T-003); this stage only produces FR/NFR, constraints, questions, and the glossary |
| ux | not applicable for stage A-01 — UX flows/screens (U-01, T-004) and wireframes/prototype/UI spec (U-02, T-005) are produced at the UX stages; this stage only produces FR/NFR, constraints, questions, and the glossary |
| issues | - |
| commit | bca5cf2 |

## Acceptance

- [x] Every functional requirement is atomic, has a unique ID in the `R-NNN` scheme, a short title, an unambiguous description, and a link to the source requirement line in `requirements/исходные требования.txt`.
- [x] Every non-functional requirement (NFR) is atomic, has a unique `R-NNN` ID, a measurable/verifiable target or an explicit "to be decided" marker, and a category (performance / reliability / security / usability / operability / compliance).
- [x] Each source requirement (task activity tracking; task end outcome; calendar view with duration; AI-assisted search; token consumption per model) is covered by at least one FR or NFR, with no requirement left unassigned.
- [x] Tech-stack constraints are fixed as NFRs: backend — Docker, Java, Postgres, Redis; frontend — Vue.js (web); local-only operation (local Ollama models, no external APIs for harness operations).
- [x] Open questions are recorded in `docs/analysis/questions-ai-task-tracker.md` with ID `Q-NNN`, each tagged `blocking` / `non-blocking`, including at minimum the format/source of opencode activity data (sessions files / opencode SDK / opencode server API / other) and the data model for "task" vs "activity".
- [x] `docs/analysis/glossary.md` defines the domain terms used in FR/NFR (task, activity, task end state, token consumption, model, calendar view, AI search) and aligns with `docs/plan.md` scope.
- [x] `docs/analysis/requirements-ai-task-tracker.md` contains a traceability table (R-NNN -> source line -> category) and a section for the recorded stack constraints.

## Checkpoints

| ID | Stage | Artifacts | Status | Review file |
|---|---|---|---|---|
| CP-1 | A-01 requirements baseline | `docs/analysis/requirements-ai-task-tracker.md`, `docs/analysis/questions-ai-task-tracker.md`, `docs/analysis/glossary.md` | approved | `docs/checkpoints/T-002-CP-1.md` |
| CP-F | final acceptance | all task artifacts | approved | `docs/checkpoints/T-002-CP-F.md` |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.

## Blockers

- (filled on status=failed|blocked: cause, output, what is needed to unblock)

## Log

- 2026-11-11: T-002 started (A-01 Requirements analysis). Created card from `harness/templates/task-card-feature.md` (stage A-01, role analyst, `in_progress`, depends_on T-001). Inputs: source requirements + T-001 planning scope + feature workflow. Artifacts to produce (analyst zone): `docs/analysis/requirements-ai-task-tracker.md`, `docs/analysis/questions-ai-task-tracker.md`, `docs/analysis/glossary.md`. Checkpoints CP-1 (requirements baseline) and CP-F. e2e/ux recorded `not applicable` for A-01 with reason (delivered at A-02 / U-01 / U-02). Board row T-002 -> `in_progress`; activity diagram T-002 -> yellow `#FFE082`; SVG re-rendered. Orchestrator only sets up the card; analyst performs the FR/NFR work up to CP-1.
- 2026-11-11: analyst reported **CHECKPOINT CP-1 reached**. Artifacts in `docs/analysis/`: `requirements-ai-task-tracker.md` (FR R-001..R-013, NFR R-020..R-030 — 24 атомарных R-NNN с источниками ИТ и акцептанс-критериями; constraints C-1..C-3; assumptions A-1..A-3; traceability раздел 5), `questions-ai-task-tracker.md` (Q-001..Q-008: 3 blocking — Q-001/Q-002/Q-008 — и 5 non-blocking, каждый с вариантами и recommended default), `glossary.md` (продуктовые и harness-термины). Orchestrator created `docs/checkpoints/T-002-CP-1.md` (status `awaiting_review`), set T-002 -> `awaiting_review` on board/card, activity T-002 -> blue `#87CEFA`, SVG re-rendered. No issues raised; issues index unchanged.
- 2026-11-11: **CP-1 approved** (user: `decision: approved` in `docs/checkpoints/T-002-CP-1.md`; «Делаем default» за Q-001..Q-008 в `docs/analysis/questions-ai-task-tracker.md`). Orchestrator: CP-1 status -> `approved`, reviewed_at 2026-11-11, acceptance checklist CP-1 снят `[x]`, User edits + Role response записаны (defaults зафиксированы как решения, blocking Q-001/Q-002/Q-008 сняты), next action `continue`; строка-статус в сводке `questions-ai-task-tracker.md`. T-002 -> `in_progress` (resume после промежуточного CP-1).
- 2026-11-11: **CHECKPOINT CP-F reached** (A-01 final acceptance). Orchestrator created `docs/checkpoints/T-002-CP-F.md` (status `awaiting_review`): финальная верификация FR/NFR, traceability, stack constraint, Q-NNN решены (defaults), glossary, readiness к T-003. T-002 -> `awaiting_review` on board/card, activity T-002 -> blue `#87CEFA`, SVG re-rendered.
- 2026-11-11: **CP-F approved by user** (`decision: approved` in `docs/checkpoints/T-002-CP-F.md`). Acceptance verified (lead): (1) FR R-001..R-013 + NFR R-020..R-030 atomic with sources/acceptance/MoSCoW; (2) traceability раздел 5 покрывает все строки ИТ (ИТ:1, ИТ:3, ИТ:4, ИТ:7, ИТ:8); (3) стек зафиксирован C-1 + R-020/R-021/R-022; (4) Q-001..Q-008 решены via recommended defaults (blocking сняты); (5) glossary полный; (6) readiness к T-003 подтверждена. T-002 -> `done` (board/card/diagram green #90EE90); all card acceptance items `[x]`; CP-F row `approved`. Final closure commit `bca5cf2` («T-002: requirements analysis (A-01) accepted»); hash recorded on board and card. T-003 (A-02) unblocked — activation only by command.
