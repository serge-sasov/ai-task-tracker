# T-005 CP-F: Final acceptance — UX wireframes, prototype, UI spec (U-02)

| Field | Value |
|---|---|
| task | T-005 |
| workflow | feature |
| bug | - |
| checkpoint | CP-F |
| role | ux-designer |
| stage | U-02 |
| artifacts | `docs/ux/wireframes-ai-task-tracker.puml`, `docs/ux/wireframes-ai-task-tracker.svg`, `docs/ux/ui-spec-ai-task-tracker.md`, `docs/ux/prototype/ai-task-tracker/index.html`, `docs/ux/prototype/ai-task-tracker/uc-e2e-001.html`..`uc-e2e-008.html`, `docs/ux/prototype/ai-task-tracker/assets/app.css`, `docs/ux/prototype/ai-task-tracker/assets/app.js` |
| status | changes_requested |
| requested_at | 2026-11-12T18:53:47 |
| reviewed_at | 2026-11-12T19:19:47 |
| reviewer | user |
| decision | changes_requested |
| snapshot_commit | 17126cc |

## Review scope

This is the **final acceptance (CP-F)** for T-005. The deliverables are complete and were
verified against the card's acceptance criteria (a)–(f). CP-2 (the same artifact set) was
already **approved** by the user (2026-11-12, see `docs/checkpoints/T-005-CP-2.md`). CP-F asks the
user to confirm acceptance of the whole task and close it.

Verify the acceptance table below. If all is acceptable, approve to close T-005 as `done`;
otherwise request changes and the task returns to the last checkpoint.

### Acceptance (a)–(f) — verified

| # | Criterion | Evidence | Result |
|---|---|---|---|
| a | Salt wireframes for S-01..S-06 + layout-affecting loading/empty/success/error states; `.puml` passes MCP `check_syntax`; `.svg` rendered next to it | `wireframes-ai-task-tracker.puml` (103 lines, `@startsalt`…`@endsalt`) covers S-01..S-06 plus загрузка/пусто/успех/ошибка blocks; MCP `plantuml-render check_syntax` → `valid: true, errors: []`; `wireframes-ai-task-tracker.svg` present (62.5 KB, no "Syntax error"/"PlantUML error" text) | PASS |
| b | UI spec per `harness/templates/ux-spec.md`; §3.1 maps every UC-E2E-001..008 to a page or explicit waiver; components/validation/errors, accessibility, UI copy, design tokens, responsive, traceability, open questions | `ui-spec-ai-task-tracker.md`: §3.1 coverage (all 8 UCs → `uc-e2e-0NN.html`, no waivers); §5 components/validation/errors; §6 accessibility + canonical RU copy; §7 design tokens + responsive; §8 traceability; §9 open questions | PASS |
| c | `index.html` catalog links all scenario pages; every page links back to index (relative) | `index.html` links `uc-e2e-001`..`uc-e2e-008.html`; all 8 `uc-e2e-0NN.html` contain `href="index.html"` | PASS |
| d | One offline page per in-scope UC-E2E-NNN with empty/loading/success/error states; local assets only; no CDN/network/secrets/real data/tracking | 8 scenario pages + `index.html`; grep for `fetch(`/`XMLHttpRequest`/`WebSocket`/`localStorage`/`sessionStorage`/`http(s)://` in `*.html/js/css` → **no matches**; assets under `assets/` only | PASS |
| e | Traceability UC-E2E ↔ screens (S-01..S-06) ↔ R-NNN ↔ prototype page consistent with U-01 + e2e use cases | `ui-spec-ai-task-tracker.md` §3.1 + §8 tables map all 8 UCs to screens and pages; R-NNN referenced (R-010/R-011/R-023/R-027/R-029/R-030) all valid in `requirements-ai-task-tracker.md`; matches `e2e-use-cases-ai-task-tracker.md` UC-E2E-001..008 and U-01 `screens`/`user-flows` | PASS |
| f | Russian UI copy (canonical texts from `screens` §4); single-user scope per Q-006/Q-004; no contradictions with resolved Q-NNN/R-NNN | all 9 pages `lang="ru"`; RU copy from `screens-ai-task-tracker.md` §4 used verbatim; Q-004 (single-user локальный, без аутентификации) + Q-006 (только русский) honored; no login/account feature | PASS |

### How to review the artifacts

- Wireframes: open `docs/ux/wireframes-ai-task-tracker.svg` in a browser (or re-run MCP render).
- UI spec: read `docs/ux/ui-spec-ai-task-tracker.md` (§3.1 coverage, §8 traceability).
- Prototype: double-click `docs/ux/prototype/ai-task-tracker/index.html`; navigate the 8 scenario
  pages; confirm offline behavior (no network, no console errors, per-state rendering).

## User remarks

- 2026-11-12 (user, explicit chat decision) — calendar GUI revision, scope T-005; update the affected UX artifacts and the prototype:
  1. **Month view** must show **all days of the month grouped by weeks** (full month grid with week rows — Monday-first week columns), not just the days that have tasks.
  2. Add a **year view** showing **12 months**; each month cell carries **aggregate statistics for that month** — tasks count, total duration, total tokens. Empty months show «Нет данных».
  - Affected artifacts to revise under T-005: `docs/ux/screens-ai-task-tracker.md`, `docs/ux/user-flows-ai-task-tracker.md`, `docs/ux/wireframes-ai-task-tracker.puml`/`.svg`, `docs/ux/ui-spec-ai-task-tracker.md`, `docs/ux/prototype/ai-task-tracker/uc-e2e-005.html` (+ `assets/app.js`, `index.html` if needed).

## User edits

- none

## Role response

- All task deliverables were produced by the ux-designer (T-005 owner). Per the process §0/§1
  exception, one sequential `harness-coder` call per scenario page (001, 002, 004, 005, 006, 007,
  008) was used; `uc-e2e-003.html` (exemplar) and shared assets (`app.css`, `app.js`) were written
  by the UX designer. Every page was validated by the UX designer, which remains the accountable
  owner. The prototype is fully offline (no CDN, network APIs, storage, or tracking).
- The user-reported wireframe PlantUML errors and the prototype verification defects were fixed and
  are part of the approved CP-2 snapshot (`9ecf877`).

## Next action

- on `approved` -> set T-005 to `done`; record final commit; unblock T-006 (R-01).
- on `changes_requested` -> return to CP-2 (last checkpoint) and address the requested changes.

## Orchestrator note

- 2026-11-12: CP-F created after CP-2 was approved (2026-11-12). Task set to `awaiting_review` for
  final acceptance. T-005 is NOT `done` until the user explicitly approves CP-F.
- 2026-11-12: CP-F **changes_requested** (user, 19:19:47) — calendar month/year views. Task returned
  to `in_progress` for the UX revision. CP-2 stays `approved` (prior acceptance of the delivered
  set); a new CP-2 revision review will be created after the role finishes the calendar changes.
  T-005 is NOT `done`.
