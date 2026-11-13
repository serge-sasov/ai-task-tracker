# T-007: R-02 Diagrams and handoff

| Field | Value |
|---|---|
| id | T-007 |
| title | R-02 Diagrams and handoff |
| workflow | feature |
| stage | R-02 |
| role | architect |
| status | in_progress |
| hold | false (true forces human review at every checkpoint) |
| depends_on | T-006 |
| inputs | `docs/architecture/architecture-ai-task-tracker.md`, `docs/architecture/adr-ai-task-tracker.md`, `docs/architecture/contracts-ai-task-tracker.md`, `docs/analysis/requirements-ai-task-tracker.md`, `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/analysis/e2e-test-data-ai-task-tracker.yaml` |
| artifacts | `docs/architecture/component-*.puml`, `docs/architecture/sequence-scn-*.puml`, `docs/architecture/data-model-*.puml`, `docs/architecture/class-*.puml`, `docs/architecture/swimlane-svc-*.puml`, `docs/architecture/*.svg` |
| e2e | required — sequence diagrams cover UC-E2E-001..UC-E2E-008 scenarios (one `sequence-scn-*` per in-scope `SCN-NNN`, traceable to the mapped `UC-E2E-NNN`) |
| ux | required — component diagram reflects screens S-01..S-06 serving flow |
| chunks | 6 — one diagram per chunk: (1) `component-*.puml`; (2) `data-model-*.puml`; (3) `class-*.puml`; (4) `sequence-scn-*.puml` (one per `SCN-NNN`); (5) `swimlane-svc-*.puml` (one per `SVC-NNN`, or explicit `No microservices` waiver honored); (6) `check_syntax` + `.svg` render for every `.puml` and link every artifact in `architecture-ai-task-tracker.md` |
| budget | PlantUML: max_write_kb 8 / max_session_kb 20 / context_stop_pct 60 (per `harness/process.md` §11.1) |
| base_commit | 955f9f9 |
| usage | - |
| issues | - |
| commit | - |

## Acceptance

- [ ] Component diagram (`component-*.puml`), data model diagram (`data-model-*.puml`), and class diagram (`class-*.puml`) are present and reflect the architecture description, contracts, and `ADR` decisions.
- [ ] One sequence diagram per in-scope `SCN-NNN` (`sequence-scn-*.puml`) covering UC-E2E-001..UC-E2E-008, and one swimlane per `SVC-NNN` (`swimlane-svc-*.puml`) — or an explicit `No microservices` waiver honored from the architecture description's `Scope and applicability` section (silence is not a waiver).
- [ ] Every `.puml` passes MCP `plantuml-render check_syntax` and is rendered to a fresh `.svg` next to it.
- [ ] Every R-02 artifact is linked/referenced in `docs/architecture/architecture-ai-task-tracker.md` (diagram index / traceability).

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-2 | R-02 diagrams and handoff | `docs/architecture/component-*.puml`, `docs/architecture/sequence-scn-*.puml`, `docs/architecture/data-model-*.puml`, `docs/architecture/class-*.puml`, `docs/architecture/swimlane-svc-*.puml`, `docs/architecture/*.svg` | - | pending | `docs/checkpoints/T-007-CP-2.md` |
| CP-F | final acceptance | all task artifacts | - | pending | `docs/checkpoints/T-007-CP-F.md` |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.
`Checks` is a machine summary `nP/nF/nM` written by `stage-done.ps1`.

## Blockers

- (filled on status=failed|blocked: cause, output, what is needed to unblock)

## Log

- 2026-11-13: card created for R-02 (Diagrams and handoff); status pending; depends on T-006 (R-01, reopened in_progress after harness 2.6.0 upgrade extended R-01 scope).
- 2026-11-13: T-006 done; T-007 started (R-02 diagrams, 6 chunks). base_commit set to 955f9f9 (HEAD at start).
