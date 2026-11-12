# T-005 CP-F: Final acceptance — UX wireframes, prototype, UI spec (U-02)

| Field | Value |
|---|---|
| task | T-005 |
| workflow | feature |
| bug | - |
| checkpoint | CP-F (revision 2) |
| role | ux-designer |
| stage | U-02 |
| artifacts | `docs/ux/wireframes-ai-task-tracker.puml`, `docs/ux/wireframes-ai-task-tracker.svg`, `docs/ux/ui-spec-ai-task-tracker.md`, `docs/ux/screens-ai-task-tracker.md`, `docs/ux/user-flows-ai-task-tracker.md`, `docs/ux/prototype/ai-task-tracker/index.html`, `docs/ux/prototype/ai-task-tracker/uc-e2e-001.html`..`uc-e2e-008.html`, `docs/ux/prototype/ai-task-tracker/assets/app.css`, `docs/ux/prototype/ai-task-tracker/assets/app.js` |
| status | approved |
| requested_at | 2026-11-13T00:02:57 |
| reviewed_at | 2026-11-13T00:14:04 |
| reviewer | user |
| decision | approved |
| snapshot_commit | `b139595` (deliverables) · `aa13165` (CP-2 rev-2 approval) · `90028a2` (CP-F revision 2) · `2409d2f` (final, accepted) |

## Revision 2 — final acceptance

This is the **final acceptance (CP-F)** for T-005, revision 2. The user's change request from
revision 1 (calendar: full month grid + 12-month year view) has been implemented, and CP-2
**revision 2** was **approved** by the user (2026-11-12T23:10:47, see
`docs/checkpoints/T-005-CP-2.md`). CP-F now asks the user to confirm acceptance of the whole
revised task and close it.

Verify the acceptance table below. If all is acceptable, approve to close T-005 as `done`;
otherwise request changes and the task returns to the last checkpoint (CP-2 revision 3 cycle).

### Acceptance (a)–(f) — verified against revision-2 artifacts

| # | Criterion | Evidence | Result |
|---|---|---|---|
| a | Wireframes for S-01..S-06 + layout-affecting loading/empty/success/error states; `.puml` passes MCP `check_syntax`; `.svg` rendered next to it | `wireframes-ai-task-tracker.puml` covers S-01..S-06 plus загрузка/пусто/успех/ошибка; **S-04** now specifies «Месяц» (полная сетка Пн–Вс, все дни + приглушённые смежные) and «Год» (12 месяцев, статистика); MCP `plantuml-render check_syntax` → `valid: true, errors: []`; `wireframes-ai-task-tracker.svg` re-rendered (62 800 B, no error text) | PASS |
| b | UI spec per `harness/templates/ux-spec.md`; §3.1 maps every UC-E2E-001..008 to a page or explicit waiver; components/validation/errors, accessibility, UI copy, design tokens, responsive, traceability, open questions | `ui-spec-ai-task-tracker.md`: §3.1 coverage — all 8 UCs → `uc-e2e-0NN.html`, **UC-E2E-005 = loading (скелетон сетки) / success Неделя / День / Месяц (полная сетка Пн–Вс) / Год (12 месяцев, статистика) / empty период / error**, no waivers; §5 components/validation/errors; §6 accessibility + canonical RU copy; §7 design tokens + responsive; §8 traceability; §9 open questions | PASS |
| c | `index.html` catalog links all scenario pages; every page links back (relative) | `index.html` links `uc-e2e-001`..`uc-e2e-008.html` + «Открыть сценарий» per scenario; all 8 pages carry the full sidebar backlink set (verified line-by-line) | PASS |
| d | One offline page per in-scope UC-E2E-NNN with empty/loading/success/error states; local assets only; no CDN/network/secrets/real data/tracking | 8 scenario pages + `index.html`; **0** occurrences of `http(s)://`, `fetch(`, `XMLHttpRequest`, `WebSocket`, `localStorage`, `sessionStorage` across `*.html/js/css`; only local `assets/app.css`, `assets/app.js`. `uc-e2e-005.html` provides **loading (скелетон сетки + «Загружаем календарь…»)**, success Неделя/День/Месяц/Год, **empty период**, **error + «Повторить»** | PASS |
| e | Traceability UC-E2E ↔ screens ↔ R-NNN ↔ prototype consistent with U-01 + e2e use cases | `ui-spec` §3/§3.1/§8; `screens` S-04 row (месяц — полная сетка Пн–Вс; год — 12 месяцев со статистикой); `user-flows` F-3 (R-008, R-009, R-023, R-030); `uc-e2e-005.html` implements all; R-NNN all valid in `requirements-ai-task-tracker.md`; matches `e2e-use-cases` UC-E2E-001..008 | PASS |
| f | Russian UI copy (canonical texts from `screens` §4); single-user scope per Q-006/Q-004; no contradictions with resolved Q-NNN/R-NNN | all 9 pages `lang="ru"`; canonical RU copy used verbatim; Q-004/Q-006 honored; no login/account feature | PASS |

### User change request (revision 1) — implemented and verified

| Request | Implementation (evidence in `uc-e2e-005.html`) |
|---|---|
| Month view = all days of the month grouped by weeks (Monday-first) | `data-state="month"`: `role="grid"`, headers Пн–Вс, 42 cells — 30 in-month days (1–30.11) + 12 приглушённые смежные (26–31.10, 1–6.12); task chips on 10.11 (TASK-001, TASK-002) and 11.11 (TASK-003, TASK-004, TASK-005) |
| Year view = 12 months with per-month aggregates (tasks / duration / tokens); empty months «Нет данных» | `data-state="year"`: 12 `.yr-cell` (Январь…Декабрь); **Ноябрь** = button `data-open-month="2026-11"` «5 задач · 8ч 35мин · 18 260 токенов»; remaining 11 months «Нет данных» |
| Loading skeleton + November→month switch (Q-UX-004) | `data-state="loading"`: skeleton title + 6×7 skeleton grid + «Загружаем календарь…»; November year cell is an interactive button opening month mode |

### How to review the artifacts

- Wireframes: open `docs/ux/wireframes-ai-task-tracker.svg` in a browser (S-04 block).
- UI spec: read `docs/ux/ui-spec-ai-task-tracker.md` (§3.1 UC-E2E-005 row, §8 traceability).
- Prototype: double-click `docs/ux/prototype/ai-task-tracker/index.html` → UC-E2E-005;
  switch Состояние: Загрузка / Неделя / День / **Месяц** / **Год** / Пустой период / Ошибка.

## User remarks

- 2026-11-13 (user, explicit chat approval) — CP-F revision 2 **approved**; T-005 closed as `done`.
- 2026-11-12T23:10:47 (user, chat) — CP-2 revision 2 **approved** (calendar month/year revision
  accepted).
- See History — Revision 1 for the original change request.

## User edits

- none

## Role response

- All task deliverables were produced by the ux-designer (T-005 owner). Per the process §0/§1
  exception, sequential `harness-coder` calls were used for scenario pages; `uc-e2e-003.html`
  (exemplar) and shared assets (`app.css`, `app.js`) were written by the UX designer. Every page
  was validated by the UX designer, which remains the accountable owner. The prototype is fully
  offline (no CDN, network APIs, storage, or tracking).
- The revision-1 change request (calendar month/year views) was implemented across
  `screens`, `user-flows`, `wireframes`, `ui-spec`, and `uc-e2e-005.html` (+ `app.css`
  `.cal-year`/`.yr-cell`), committed as `b139595`, and approved by the user at CP-2 revision 2.

## Next action

- **`approved` (executed):** T-005 set to `done`; final commit recorded; T-006 (R-01) unblocked
  (pending, activation only by command).

## Orchestrator note

- 2026-11-12: CP-F revision 1 created after CP-2 approval; task `awaiting_review`.
- 2026-11-12T19:19:47: CP-F revision 1 **changes_requested** (user) — calendar month/year views;
  task returned to `in_progress`.
- 2026-11-12T23:10:47: CP-2 **revision 2 approved** (user); recorded in `aa13165`.
- 2026-11-13: CP-F **revision 2** created (`awaiting_review`); snapshot committed as `90028a2`.
- 2026-11-13T00:14:04: CP-F **revision 2 approved** (user, explicit chat approval). T-005 closed
  as `done`; T-006 (R-01) unblocked (pending). Closure commit `2409d2f`.

## History — Revision 1 (changes_requested)

| Field | Value |
|---|---|
| status | changes_requested |
| requested_at | 2026-11-12T18:53:47 |
| reviewed_at | 2026-11-12T19:19:47 |
| reviewer | user |
| decision | changes_requested |
| snapshot_commit | 17126cc |

### User remarks (revision 1)

- 2026-11-12 (user, explicit chat decision) — calendar GUI revision, scope T-005; update the
  affected UX artifacts and the prototype:
  1. **Month view** must show **all days of the month grouped by weeks** (full month grid with
     week rows — Monday-first week columns), not just the days that have tasks.
  2. Add a **year view** showing **12 months**; each month cell carries **aggregate statistics for
     that month** — tasks count, total duration, total tokens. Empty months show «Нет данных».
  - Affected artifacts to revise under T-005: `docs/ux/screens-ai-task-tracker.md`,
    `docs/ux/user-flows-ai-task-tracker.md`, `docs/ux/wireframes-ai-task-tracker.puml`/`.svg`,
    `docs/ux/ui-spec-ai-task-tracker.md`, `docs/ux/prototype/ai-task-tracker/uc-e2e-005.html`
    (+ `assets/app.js`, `index.html` if needed).
