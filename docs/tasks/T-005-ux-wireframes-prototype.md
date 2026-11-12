# T-005: UX wireframes, prototype, UI spec (U-02)

| Field | Value |
|---|---|
| id | T-005 |
| title | UX wireframes, prototype, UI spec (U-02) |
| workflow | feature |
| stage | U-02 |
| role | ux-designer |
| status | awaiting_review (CP-2 revision 2) |
| depends_on | T-004 (done) |
| inputs | `docs/ux/user-flows-ai-task-tracker.md`, `docs/ux/screens-ai-task-tracker.md`, `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/analysis/e2e-test-data-ai-task-tracker.yaml`, `docs/analysis/requirements-ai-task-tracker.md`, `docs/analysis/questions-ai-task-tracker.md`, `docs/analysis/glossary.md`, `requirements/исходные требования.txt` |
| artifacts | `docs/ux/wireframes-ai-task-tracker.puml`, `docs/ux/wireframes-ai-task-tracker.svg`, `docs/ux/ui-spec-ai-task-tracker.md`, `docs/ux/prototype/ai-task-tracker/index.html`, `docs/ux/prototype/ai-task-tracker/uc-e2e-001.html`..`uc-e2e-008.html`, `docs/ux/prototype/ai-task-tracker/assets/*` |
| e2e | required — 1:1 mapping of UC-E2E-001..008 to prototype scenario pages (consumed by Q-01 coverage matrix) |
| ux | required — U-02 is the UX stage: wireframes, UI spec, static offline HTML prototype |
| chunks | 4 — (1) wireframes: `wireframes-*.puml` covering S-01..S-06 + layout-affecting states, then `check_syntax` + SVG render; (2) UI spec: 2-3 sections per write per `harness/templates/ux-spec.md` (skeleton first, then one section per edit); (3) prototype `index.html` + shared `assets/*` (one scenario page or shared asset per write); (4) scenario pages `uc-e2e-001.html`..`uc-e2e-008.html`, one page per chunk; harness-coder delegation for page implementation allowed under process §0/§1 exception (depth 2, sequential, one child at a time, coder writes only `docs/ux/prototype/ai-task-tracker/**`, UX designer validates and remains accountable at CP-2) |
| budget | Markdown: max_write_kb 8 / max_session_kb 20 / context_stop_pct 60; HTML/CSS/JS and PlantUML: max_write_kb 12 / max_session_kb 40 / context_stop_pct 65 (per `harness/process.md` §11.1) |
| issues | - |
| commit | 17126cc (CP-F snapshot) — artifacts at 9ecf877 (CP-2) |

## Acceptance

- [ ] (a) Salt wireframes for every screen S-01..S-06 and every layout-affecting loading/empty/success/error state; `docs/ux/wireframes-ai-task-tracker.puml` passes MCP `plantuml-render check_syntax` and `docs/ux/wireframes-ai-task-tracker.svg` is rendered next to it.
- [ ] (b) UI spec `docs/ux/ui-spec-ai-task-tracker.md` per `harness/templates/ux-spec.md` incl. §3.1 scenario page coverage mapping every in-scope UC-E2E-001..008 to a page or an explicit waiver; sections: components/validation/errors, accessibility, UI copy, design tokens, responsive rules, traceability, open questions.
- [ ] (c) `index.html` catalog links all scenario pages and every scenario page links back to index (relative links).
- [ ] (d) One offline page per in-scope UC-E2E-NNN with the relevant empty/loading/success/error states; local assets only under `assets/`; no CDN, no network APIs, no secrets, no real data, no tracking.
- [ ] (e) Traceability UC-E2E <-> screens (S-01..S-06) <-> R-NNN <-> prototype page consistent with U-01 artifacts and `docs/analysis/e2e-use-cases-ai-task-tracker.md`.
- [ ] (f) Russian UI copy (canonical texts from `docs/ux/screens-ai-task-tracker.md` §4) and single-user scope per Q-006/Q-004; no contradictions with resolved Q-NNN/R-NNN.

## Checkpoints

| ID | Stage | Artifacts | Status | Review file |
|---|---|---|---|---|
| CP-2 | U-02 wireframes/prototype/UI spec | all task artifacts | awaiting_review (revision 2 — CP-F change request) | `docs/checkpoints/T-005-CP-2.md` |
| CP-F | final acceptance | all task artifacts | pending (re-review after CP-2 revision) | `docs/checkpoints/T-005-CP-F.md` |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.

## Blockers

- none

## Log

- 2026-11-12: T-005 started (U-02 UX wireframes, prototype, UI spec); card created; T-004 done (36f52cd) unblocks T-005; board T-005 -> in_progress (yellow #FFE082); diagram re-rendered.
- 2026-11-12: CP-2 reached (2026-11-12T18:11:08); checkpoint file `docs/checkpoints/T-005-CP-2.md` created; board/card T-005 -> awaiting_review (blue #87CEFA); diagram re-rendered.
- 2026-11-12: user-reported wireframe fix: `docs/ux/wireframes-ai-task-tracker.puml` had PlantUML errors (old SVG was a crash image); lead rewrote it as valid Salt, MCP `check_syntax` valid, SVG re-rendered (62.5 KB, no crash text). Prototype verification fixes: uc-e2e-006 token table JS keys (model/input/output instead of undefined), «Всё время» typos, sidebar label «UC-E2E-003 · Список и детали задачи», canonical S-05 error «Не удалось построить сводку по токенам», detail totals TASK-004=1670 / TASK-005=990; verified no external URLs/network/storage. All included in the CP-2 snapshot.
- 2026-11-12: CP-2 snapshot committed as 9ecf877; hash recorded in card and board.
- 2026-11-12: CP-2 approved by user (2026-11-12); approval recorded in `docs/checkpoints/T-005-CP-2.md`. Proceeding to final acceptance.
- 2026-11-12: CP-F created (`docs/checkpoints/T-005-CP-F.md`) with acceptance (a)–(f) evidence; CP-2 row -> approved, CP-F row -> awaiting_review; board T-005 stays awaiting_review (now for CP-F); diagram unchanged (blue #87CEFA). Task NOT done until user approves CP-F.
- 2026-11-12: CP-F **changes_requested** by user (2026-11-12T19:19:47): calendar GUI revision — (1) month view shows all days of the month grouped by weeks; (2) add a year view with 12 months, each month cell with aggregate stats (tasks count, total duration, total tokens; empty months «Нет данных»). Scope: T-005. Card/board T-005 -> in_progress (yellow #FFE082), diagram re-rendered; CP-2 row stays approved; a new CP-2 revision review will be created after the role finishes.
- 2026-11-12: artifacts to be revised under T-005 per the change request: `docs/ux/screens-ai-task-tracker.md`, `docs/ux/user-flows-ai-task-tracker.md`, `docs/ux/wireframes-ai-task-tracker.puml`/`.svg`, `docs/ux/ui-spec-ai-task-tracker.md`, `docs/ux/prototype/ai-task-tracker/uc-e2e-005.html` (+ `assets/app.js`, `index.html` if needed).
- 2026-11-12: **Revision delivered** (ux-designer) for the CP-F change request — calendar S-04: month view = full week grid (Monday-first, all days, 6×7); year view = 12 months with per-month aggregates (tasks count / total duration / total tokens; empty «Нет данных»); plus review fixes (S-04 loading skeleton reachable, functional November year-cell → month view, UI spec §3.1 loading). Revised files: `screens`, `user-flows`, `wireframes .puml`/`.svg`, `ui-spec`, `uc-e2e-005.html`, `index.html`, `assets/app.css`. Card/board T-005 -> awaiting_review (CP-2 revision 2); CP-F row -> pending (re-review after CP-2 revision).
- 2026-11-12: **Verification (lead)**: month grid 42 cells (30 in-month + 12 adjacent), 6 rows × 7 (Пн–Вс), task chips on 10.11 & 11.11 with durations; year 12 cells, November «5 задач · 8ч 35мин · 18 260 токенов», 11 months «Нет данных», November → month view works, loading skeleton reachable; wireframe `check_syntax` valid, SVG 62,800 B no crash; 9 pages `lang="ru"`, index links all 8, all back-links, 0 network/storage strings; UI spec §3.1 UC-E2E-005 includes loading; screens/flows/wireframe/ui-spec consistent; open questions Q-UX-001..005. Awaiting user review at CP-2 (revision 2).
