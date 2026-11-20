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

Project: Р’В«Р РЋР ВµРЎР‚Р Р†Р С‘РЎРѓ Р СР С•Р Р…Р С‘РЎвЂљР С•РЎР‚Р С‘Р Р…Р С–Р В° Р В°Р С”РЎвЂљР С‘Р Р†Р Р…Р С•РЎРѓРЎвЂљР ВµР в„– Р С—Р С•Р В»РЎРЉР В·Р С•Р Р†Р В°РЎвЂљР ВµР В»РЎРЏ Р Р† opencodeР’В» (ai-task-tracker).
Source requirements: `requirements/Р С‘РЎРѓРЎвЂ¦Р С•Р Т‘Р Р…РЎвЂ№Р Вµ РЎвЂљРЎР‚Р ВµР В±Р С•Р Р†Р В°Р Р…Р С‘РЎРЏ.txt`.
Task graph (feature workflow): `P-00 РІвЂ вЂ™ A-01 РІвЂ вЂ™ A-02 РІвЂ вЂ™ U-01 РІвЂ вЂ™ U-02 РІвЂ вЂ™ R-01 РІвЂ вЂ™ R-02 РІвЂ вЂ™ I-01 РІвЂ вЂ™ I-02 РІвЂ вЂ™ Q-01 РІвЂ вЂ™ Q-02 РІвЂ вЂ™ D-01` (e2e required, ux required РІР‚вЂќ all stages in scope).

| ID | Workflow | Task | Role | Dependencies | Status | Bug | Artifacts | Usage | Commit |
|---|---|---|---|---|---|---|---|---|---|
| T-001 | feature | P-00 Feature planning: scope, e2e/ux, task graph | orchestrator | - | done | - | docs/tasks/T-001-ai-task-tracker-feature-planning.md, docs/plan.md, docs/checkpoints/T-001-CP-1.md, docs/checkpoints/T-001-CP-F.md, docs/activity.puml, docs/activity.svg | - | 2552702 |
| T-002 | feature | A-01 Requirements analysis (FR/NFR, R-NNN) | analyst | T-001 | done | - | docs/analysis/requirements-ai-task-tracker.md, docs/analysis/questions-ai-task-tracker.md, docs/analysis/glossary.md, docs/checkpoints/T-002-CP-1.md, docs/checkpoints/T-002-CP-F.md | - | bca5cf2 |
| T-003 | feature | A-02 E2E use cases and YAML dataset | analyst | T-002 | done | - | docs/analysis/e2e-use-cases-ai-task-tracker.md, docs/analysis/e2e-test-data-ai-task-tracker.yaml, docs/checkpoints/T-003-CP-2.md, docs/checkpoints/T-003-CP-F.md | - | 87350a0 |
| T-004 | feature | U-01 UX flows and screens | ux-designer | T-002, T-003 | done | - | docs/ux/user-flows-ai-task-tracker.md, docs/ux/screens-ai-task-tracker.md, docs/checkpoints/T-004-CP-1.md, docs/checkpoints/T-004-CP-F.md | - | 36f52cd |
| T-005 | feature | U-02 Wireframes, prototype, UI spec | ux-designer | T-004 | done | - | docs/ux/wireframes-ai-task-tracker.puml, docs/ux/wireframes-ai-task-tracker.svg, docs/ux/ui-spec-ai-task-tracker.md, docs/ux/prototype/ai-task-tracker/index.html, docs/ux/prototype/ai-task-tracker/uc-e2e-*.html, docs/ux/prototype/ai-task-tracker/assets/* | - | 2409d2f (final, accepted); b139595 (CP-2 revision 2); aa13165 (approval; CP-F rev-2 start); 90028a2 (CP-F rev 2) |
| T-006 | feature | R-01 Architecture and contracts (ADR) | architect | T-002, T-003, T-005 | done | - | docs/architecture/architecture-ai-task-tracker.md, docs/architecture/adr-ai-task-tracker.md, docs/architecture/contracts-ai-task-tracker.md, docs/architecture/openapi-api-*.yaml | in=452356 out=182070 cr=4372564 ratio=9.7x min=211 | 411881b |
| T-007 | feature | R-02 Diagrams and module handoff | architect | T-006 | done | - | docs/architecture/component-*.puml, docs/architecture/sequence-scn-*.puml, docs/architecture/data-model-*.puml, docs/architecture/class-*.puml, docs/architecture/*.svg | in=570819 out=160155 cr=3311589 ratio=5.8x min=182 | f27a665 |
| T-008 | feature | I-01 Implementation + unit/integration tests | coder | T-007, T-005 | done | - | src/**, tests/**, backend/**, Dockerfile, docker-compose*.yml, Makefile | in=7553059 out=1761270 cr=72365374 ratio=9.6x min=3316 | e71c4ed |
| T-009 | feature | I-02 E2E automation and runner | coder | T-008, T-003, T-007 | done | - | tests/e2e/**, scripts/e2e.ps1, scripts/validate-e2e-data.py | in=4539299 out=1137324 cr=110482991 ratio=24.3x min=2657 | 88acc15 |
| T-010 | feature | Q-01 E2E plan and coverage matrix | qa | T-003, T-009 | pending | - | docs/qa/e2e-plan-ai-task-tracker.md | - | - |
| T-011 | feature | Q-02 QA, acceptance and verdict | qa | T-010, T-009, T-008, T-007, T-005 | pending | - | docs/qa/e2e-report-ai-task-tracker.md, docs/qa/review-T-011.md | - | - |
| T-012 | feature | D-01 Documentation, runbook, ADR finalization | doc-writer | T-007, T-008, T-005, T-011 | pending | - | README.md, docs/guides/* | - | - |
| T-013 | bugfix | Fix BUG-002: Dockerfile source-path mismatch (B-01/B-02) | coder | T-009 | done | BUG-002 | Dockerfile, tests/regression/** | n/a | 095944b |
| T-014 | bugfix | Verify BUG-002: Dockerfile fix (B-03) | qa | T-013 | done | BUG-002 | docs/qa/review-T-014.md | n/a | 4b51ef3 |
| T-015 | bugfix | Fix BUG-003: local stack functional health (B-01/B-02) | coder | T-009 | done | BUG-003 | backend/pom.xml, src/main/**, docker-compose.yml, tests/regression/** | n/a | 4bc664b |
| T-016 | bugfix | Verify BUG-003: local stack (B-03) | qa | T-015 | done | BUG-003 | docs/qa/review-T-016.md | n/a | 3bc860c |
| T-017 | feature | P-00 Feature planning: S-01 model column (D-01 waived) | orchestrator | - | done | - | docs/plan.md, docs/tasks/T-017-task-list-model-planning.md, docs/activity.puml, docs/activity.svg | in=1341131 out=201058 cr=2551015 ratio=1.9x min=327 | 5a28f17 |
| T-018 | feature | A-01 Requirements baseline for the model column | analyst | T-017 | done | - | docs/analysis/requirements-ai-task-tracker.md, docs/analysis/glossary.md | n/a | 153103c |
| T-019 | feature | A-02 E2E use cases and dataset for the model column | analyst | T-018 | done | - | docs/analysis/e2e-use-cases-ai-task-tracker.md, docs/analysis/e2e-test-data-ai-task-tracker.yaml | n/a | 2d0ed43 |
| T-020 | feature | U-01 UX flows and screens for S-01 model display | ux-designer | T-018, T-019 | done | - | docs/ux/user-flows-ai-task-tracker.md, docs/ux/screens-ai-task-tracker.md | n/a | 381ff63 |
| T-021 | feature | U-02 Wireframes, prototype and UI spec for the model column | ux-designer | T-020 | done | - | docs/ux/wireframes-ai-task-tracker.puml, docs/ux/wireframes-ai-task-tracker.svg, docs/ux/ui-spec-ai-task-tracker.md, docs/ux/prototype/ai-task-tracker/index.html, docs/ux/prototype/ai-task-tracker/uc-e2e-*.html, docs/ux/prototype/ai-task-tracker/assets/* | n/a | e5590d5 |
| T-022 | feature | R-01 Solution design for model aggregation and S-01 display | architect | T-018, T-019, T-021 | done | - | docs/architecture/architecture-ai-task-tracker.md, docs/architecture/adr-ai-task-tracker.md, docs/architecture/components-ai-task-tracker.puml, docs/architecture/components-ai-task-tracker.svg, docs/architecture/sequence-scn-003-ai-task-tracker.puml, docs/architecture/sequence-scn-003-ai-task-tracker.svg | in=48057 out=18854 cr=272822 ratio=5.7x min=34 | a4899c2 |
| T-023 | feature | R-02 Contracts and OpenAPI for models | architect | T-022 | done | - | docs/architecture/architecture-ai-task-tracker.md, docs/architecture/contracts-ai-task-tracker.md, docs/architecture/openapi-api-001-ai-task-tracker.yaml, docs/architecture/data-model-ai-task-tracker.puml, docs/architecture/data-model-ai-task-tracker.svg, docs/architecture/class-ai-task-tracker.puml, docs/architecture/class-ai-task-tracker.svg | n/a | f675829 |
| T-024 | feature | I-01 Implementation and unit tests | coder | T-023, T-021 | done | - | src/main/java/ru/aitasktracker/api/dto/TaskListItem.java, src/main/java/ru/aitasktracker/api/service/TaskQueryService.java, tests/java/ru/aitasktracker/api/service/TaskQueryServiceTest.java, src/web/src/api/types.ts, src/web/src/views/DashboardView.vue | in=82511 out=17279 cr=2997680 ratio=36.3x min=10 | 7d20ebc |
| T-025 | feature | I-02 E2E automation for the model column | coder | T-024, T-019, T-023 | done | - | tests/e2e/uc_e2e_001_003/shared.py, tests/e2e/uc_e2e_001_003/test_uc_e2e_003_task_list_detail.py | in=109818 out=26993 cr=6432125 ratio=58.6x min=16 | fb9007b |
| T-026 | feature | Q-01 E2E plan and coverage matrix | qa | T-019, T-025 | done | - | docs/qa/e2e-plan-task-list-model.md | in=60511 out=9996 cr=1620588 ratio=26.8x min=103 | 85c202e |
| T-027 | feature | Q-02 QA, acceptance and verdict | qa | T-026, T-025, T-024, T-023, T-021 | done | ISSUE-20261118-153622, ISSUE-20261118-185907, BUG-008 | docs/qa/e2e-report-task-list-model.md, docs/qa/review-T-027.md | n/a | 1e3ff88 |
| T-028 | feature | F-01 Final acceptance and closure: S-01 model column | orchestrator + lead | T-027 | done | ISSUE-20261118-231906 | docs/plan.md, docs/tasks/T-028-final-acceptance-model-column.md, docs/activity.puml, docs/activity.svg | n/a | 2dd00bd |
| T-029 | feature | P-00 Feature planning: S-04 calendar day model column | orchestrator | T-028 | done | - | docs/plan.md, docs/tasks/T-029-calendar-day-model-planning.md, docs/activity.puml, docs/activity.svg | in=379281 out=53938 cr=2191589 ratio=5.8x min=220 | 07a2f27 |
| T-030 | feature | A-01 Requirements delta for S-04 calendar day model column | analyst | T-029 | done | - | docs/analysis/requirements-ai-task-tracker.md, docs/analysis/questions-ai-task-tracker.md, docs/analysis/glossary.md | in=269947 out=41094 cr=3590810 ratio=13.3x min=116 | e031a8f |
| T-031 | feature | A-02 E2E use cases and dataset delta for S-04 model column | analyst | T-030 | done | - | docs/analysis/e2e-use-cases-ai-task-tracker.md, docs/analysis/e2e-test-data-ai-task-tracker.yaml | in=281084 out=60015 cr=6797571 ratio=24.2x min=282 | f59ae41 |
| T-032 | feature | U-01 UX flows and screens for S-04 model column | ux-designer | T-030, T-031 | done | - | docs/ux/user-flows-ai-task-tracker.md, docs/ux/screens-ai-task-tracker.md | in=240623 out=35379 cr=2662219 ratio=11.1x min=93 | 88742ba |
| T-033 | feature | U-02 Wireframes, prototype and UI spec for S-04 model column | ux-designer | T-032 | in_progress | - | docs/ux/wireframes-ai-task-tracker.puml, docs/ux/wireframes-ai-task-tracker.svg, docs/ux/ui-spec-ai-task-tracker.md, docs/ux/prototype/ai-task-tracker/index.html, docs/ux/prototype/ai-task-tracker/uc-e2e-005.html, docs/ux/prototype/ai-task-tracker/assets/* | - | - |
| T-034 | feature | R-01 Solution design for S-04 day model column | architect | T-030, T-031, T-033 | pending | - | docs/architecture/architecture-ai-task-tracker.md, docs/architecture/adr-ai-task-tracker.md, docs/architecture/components-ai-task-tracker.puml, docs/architecture/components-ai-task-tracker.svg, docs/architecture/sequence-scn-*.puml, docs/architecture/sequence-scn-*.svg | - | - |
| T-035 | feature | R-02 Contracts and module handoff for S-04 | architect | T-034 | pending | - | docs/architecture/architecture-ai-task-tracker.md, docs/architecture/contracts-ai-task-tracker.md, docs/architecture/openapi-api-*.yaml, docs/architecture/data-model-*.puml, docs/architecture/class-*.puml, docs/architecture/swimlane-svc-*.puml | - | - |
| T-036 | feature | I-01 Implementation and unit tests for S-04 model column | coder | T-035, T-033 | pending | - | src/web/src/views/CalendarDayView.vue, src/web/src/api/types.ts, src/web/src/components/ModelCell.vue, tests/** | - | - |
| T-037 | feature | I-02 E2E automation and built-frontend check for S-04 | coder | T-036, T-031, T-035 | pending | - | tests/e2e/**, scripts/e2e.ps1, scripts/validate-e2e-data.py | - | - |
| T-038 | feature | Q-01 E2E plan and coverage for S-04 model column | qa | T-031, T-037 | pending | - | docs/qa/e2e-plan-s04-model.md | - | - |
| T-039 | feature | Q-02 QA, acceptance and verdict for S-04 | qa | T-038, T-037, T-036, T-035, T-033 | pending | - | docs/qa/e2e-report-s04-model.md, docs/qa/review-T-039.md | - | - |
| T-040 | feature | D-01 Documentation for S-04 model column | doc-writer | T-039, T-036, T-035, T-033 | pending | - | README.md, docs/guides/* | - | - |