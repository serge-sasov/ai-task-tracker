# Plan (board)

Process rules: `harness/process.md`.
Workflows: `harness/workflows/feature.md`, `harness/workflows/bugfix.md`.
Diagram: `docs/activity.puml`. Updated after every task.

## Status legend

| Status | Diagram color | Meaning |
|---|---|---|
| `pending` | gray `#D3D3D3` | not started |
| `in_progress` | yellow `#FFE082` | in progress |
| `awaiting_review` | blue `#87CEFA` | waiting for user checkpoint review |
| `done` | green `#90EE90` | completed |
| `failed` | red `#FF9090` | error, fix required |
| `blocked` | orange `#FFB347` | external blocker |

At most one task is active (`in_progress` or `awaiting_review`) across both workflows.

## Board

Project: «Сервис мониторинга активностей пользователя в opencode» (ai-task-tracker).
Source requirements: `requirements/исходные требования.txt`.
Task graph (feature workflow): `P-00 → A-01 → A-02 → U-01 → U-02 → R-01 → R-02 → I-01 → I-02 → Q-01 → Q-02 → D-01` (e2e required, ux required — all stages in scope).

| ID | Workflow | Task | Role | Dependencies | Status | Bug | Artifacts | Usage | Commit |
|---|---|---|---|---|---|---|---|---|---|
| T-001 | feature | P-00 Feature planning: scope, e2e/ux, task graph | orchestrator | - | done | - | docs/tasks/T-001-ai-task-tracker-feature-planning.md, docs/plan.md, docs/checkpoints/T-001-CP-1.md, docs/checkpoints/T-001-CP-F.md, docs/activity.puml, docs/activity.svg | - | 2552702 |
| T-002 | feature | A-01 Requirements analysis (FR/NFR, R-NNN) | analyst | T-001 | done | - | docs/analysis/requirements-ai-task-tracker.md, docs/analysis/questions-ai-task-tracker.md, docs/analysis/glossary.md, docs/checkpoints/T-002-CP-1.md, docs/checkpoints/T-002-CP-F.md | - | bca5cf2 |
| T-003 | feature | A-02 E2E use cases and YAML dataset | analyst | T-002 | done | - | docs/analysis/e2e-use-cases-ai-task-tracker.md, docs/analysis/e2e-test-data-ai-task-tracker.yaml, docs/checkpoints/T-003-CP-2.md, docs/checkpoints/T-003-CP-F.md | - | 87350a0 |
| T-004 | feature | U-01 UX flows and screens | ux-designer | T-002, T-003 | done | - | docs/ux/user-flows-ai-task-tracker.md, docs/ux/screens-ai-task-tracker.md, docs/checkpoints/T-004-CP-1.md, docs/checkpoints/T-004-CP-F.md | - | 36f52cd |
| T-005 | feature | U-02 Wireframes, prototype, UI spec | ux-designer | T-004 | done | - | docs/ux/wireframes-ai-task-tracker.puml, docs/ux/wireframes-ai-task-tracker.svg, docs/ux/ui-spec-ai-task-tracker.md, docs/ux/prototype/ai-task-tracker/index.html, docs/ux/prototype/ai-task-tracker/uc-e2e-*.html, docs/ux/prototype/ai-task-tracker/assets/* | - | 2409d2f (final, accepted); b139595 (CP-2 revision 2); aa13165 (approval; CP-F rev-2 start); 90028a2 (CP-F rev 2) |
| T-006 | feature | R-01 Architecture and contracts (ADR) | architect | T-002, T-003, T-005 | done | - | docs/architecture/architecture-ai-task-tracker.md, docs/architecture/adr-ai-task-tracker.md, docs/architecture/contracts-ai-task-tracker.md, docs/architecture/openapi-api-*.yaml | in=452356 out=182070 cr=4372564 ratio=9.7x min=211 | 411881b |
| T-007 | feature | R-02 Diagrams and module handoff | architect | T-006 | in_progress | - | docs/architecture/component-*.puml, docs/architecture/sequence-scn-*.puml, docs/architecture/data-model-*.puml, docs/architecture/class-*.puml, docs/architecture/swimlane-svc-*.puml, docs/architecture/*.svg | - | - |
| T-008 | feature | I-01 Implementation + unit/integration tests | coder | T-007, T-005 | pending | - | src/, tests/, Dockerfile, docker-compose.yml, Makefile | - | - |
| T-009 | feature | I-02 E2E automation and runner | coder | T-008, T-003, T-007 | pending | - | tests/e2e/, scripts/e2e.ps1, scripts/validate-e2e-data.py | - | - |
| T-010 | feature | Q-01 E2E plan and coverage matrix | qa | T-003, T-009 | pending | - | docs/qa/e2e-plan-ai-task-tracker.md | - | - |
| T-011 | feature | Q-02 QA, acceptance and verdict | qa | T-010, T-009, T-008, T-007, T-005 | pending | - | docs/qa/e2e-report-ai-task-tracker.md, docs/qa/review-T-011.md | - | - |
| T-012 | feature | D-01 Documentation, runbook, ADR finalization | doc-writer | T-007, T-008, T-005, T-011 | pending | - | README.md, docs/guides/* | - | - |

