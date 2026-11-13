# ISSUE-20261113-205922: T-007 card lists conditional swimlane glob that blocks verify-stage

| Field | Value |
|---|---|
| id | ISSUE-20261113-205922 |
| status | resolved |
| severity | major |
| type | card |
| detected_at | 2026-11-13T20:59:22.0000000+03:00 |
| source | harness-architect (T-007 R-02) |
| role | architect |
| location | docs/tasks/T-007-diagrams-handoff.md (artifacts, line 14) + harness/scripts/verify-artifacts.ps1 Check-FileExists L83–85 |
| environment | OS: Microsoft Windows NT 10.0.26300.0; cwd: C:\study\ai\research\ai-task-tracker |

## Symptoms

`verify-stage.ps1 -Task T-007 -Stage R-02` reports:

- FAIL: `artifacts-exist` — "docs/architecture/swimlane-svc-*.puml (unresolved from card)"

All other R-02 checks pass (9 PASS, 0 MANUAL, 3 N/A, 0 UNCHECKED):
`diagram-syntax`, `diagram-svg`, `component-exist`, `data-model-exist`,
`class-diagram-exist`, `sequence-count` (8=8), `sequence-ids`,
`sequence-uc-trace`, `architecture-links` (15 files referenced).

## Reproduction

1. Ensure `docs/architecture/` contains the 11 `.puml`/`.svg` pairs for
   component, data-model, class, and the 8 `sequence-scn-*.puml` scenarios
   (T-007 R-02 deliverables, all valid PlantUML, all rendered SVG).
2. Run: `powershell -NoProfile -ExecutionPolicy Bypass -File harness/scripts/verify-stage.ps1 -Task T-007 -Stage R-02`
3. Observe the single `artifacts-exist` FAIL caused by the unresolved
   `swimlane-svc-*.puml` glob in the card's artifacts field.

## Expected / actual behavior

- Expected (per `harness/workflows/feature.md` L59–64): conditional
  `swimlane-svc-*` files are **not** listed in `card.artifacts`; they are
  "governed by the architecture description's `Scope and applicability`
  section and checked by the stage registry, not by `artifacts-exist`."
  With the "No microservices" waiver in scope, `artifacts-exist` should PASS
  once the glob is removed from the card.
- Actual: the card's `artifacts` field (L14) lists `swimlane-svc-*.puml`.
  `Expand-Artifacts` (harness-lib.ps1) reports it as unresolved;
  `Check-FileExists` (verify-artifacts.ps1 L83–85) turns any unresolved
  item in `card.artifacts` into a FAIL.

## Cause

Card defect. `feature.md` L59–64 is explicit:

> `card.artifacts` lists only unconditional outputs for R-01/R-02 …
> Conditional `openapi-api-*` and `swimlane-svc-*` files are governed by
> the architecture description's `Scope and applicability` section and
> checked by the stage registry, not by `artifacts-exist`.

The T-007 card's L14 `artifacts` field lists `swimlane-svc-*.puml` despite:

1. `architecture-ai-task-tracker.md` L57 — "No microservices. The components
   above are modules of one backend process deployed …"
2. 0 `SVC-\d{3}` identifiers in scope (confirmed: `swimlane-count` →
   NOT_APPLICABLE, `swimlane-ids` → NOT_APPLICABLE).
3. The card's own acceptance L27: "… or an explicit `No microservices` waiver
   honored from the architecture description's `Scope and applicability`
   section (silence is not a waiver)."

The waiver condition is met, yet the `artifacts` field still references the
conditional set, producing a self-contradiction in the card. The architect
role cannot edit `card.artifacts` (out of write-area: only status/log of own
task).

## Solution / workaround

Fix (orchestrator action):

1. Edit `docs/tasks/T-007-diagrams-handoff.md` L14 — remove the
   `swimlane-svc-*.puml` glob from the `artifacts` field, preserving all
   other entries (`component-*.puml`, `sequence-scn-*.puml`,
   `data-model-*.puml`, `class-*.puml`, `*.svg`).
2. Re-run: `powershell -NoProfile -ExecutionPolicy Bypass -File harness/scripts/verify-stage.ps1 -Task T-007 -Stage R-02`
3. Expected: `artifacts-exist` → PASS; RESULT: 10 PASS, 0 FAIL, 0 MANUAL,
   3 N/A, 0 UNCHECKED.
4. Close CP-2 via `harness/scripts/stage-done.ps1` (or record the manual
   waiver in the checkpoint file with a reference to this issue).

Alternative (orchestrator discretion): manually waive the universal
`artifacts-exist` failure in the CP-2 checkpoint record, citing this issue
and the architecture L57 "No microservices" statement.

## Verification

- Before fix (current): `verify-stage.ps1 -Task T-007 -Stage R-02` →
  9 PASS, 1 FAIL (artifacts-exist), 0 MANUAL, 3 N/A, 0 UNCHECKED.
- After fix (expected): 10 PASS, 0 FAIL, 0 MANUAL, 3 N/A, 0 UNCHECKED.

## Prevention / follow-up

- Card-creation tooling should validate `card.artifacts` against
  `harness/workflows/feature.md` L59–64: reject conditional artifact types
  (`openapi-api-*`, `swimlane-svc-*`) when the applicable waiver is in
  effect.
- `check_syntax`/render for swimlane diagrams remains available if a future
  task introduces microservices.

## Links

- `docs/tasks/T-007-diagrams-handoff.md` (card with the defect; L14 artifacts,
  L27 acceptance, L35 CP-2 row)
- `docs/architecture/architecture-ai-task-tracker.md` (L57 "No microservices"
  waiver; Diagram index L110–122)
- `harness/workflows/feature.md` (L59–64 rule)
- `harness/scripts/verify-artifacts.ps1` (L83–85 `Check-FileExists`)
- `harness/scripts/harness-lib.ps1` (L90–137 `Expand-Artifacts`)

## Timeline

- 2026-11-13T20:59:22.0000000+03:00: detected by architect during R-02
  verification
- 2026-11-13T21:15:40: resolved — orchestrator removed the conditional
  `swimlane-svc-*.puml` glob from card L14 `artifacts` and the CP-2 row;
  `verify-stage.ps1 -Task T-007 -Stage R-02` now 10 PASS / 0 FAIL / 0 MANUAL /
  3 N/A / 0 UNCHECKED; CP-2 recorded (snapshot `e4565c7`, P2 auto-approval)
