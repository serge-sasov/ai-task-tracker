# BUG-005: Calendar heatmap never displays API data

| Field | Value |
|---|---|
| id | BUG-005 |
| title | Calendar heatmap renders a local zero-filled grid and ignores `/api/calendar` |
| status | verified |
| severity | P2 |
| reported_at | 2026-11-17T16:45:00+03:00 |
| reporter | user (during the local Docker deployment) |
| affected | `/calendar` (S-04 heatmap); calendar store |
| requirements | R-008, R-009 |
| use_cases | UC-E2E-005 |
| tasks | - (fix applied during the local deployment) |
| duplicate_of | - |
| commit | calendar heatmap fix |
| issues | - |

## Observed behavior

`http://127.0.0.1:8081/calendar` showed an empty heatmap even though
`GET /api/calendar?view=week&date=2026-11-09` returns 2 tasks on 2026-11-10 and 3
on 2026-11-11 (and the month view has two non-empty days). The UI never changed
for any day/week.

## Expected behavior

The 12-week heatmap shows per-day task counts for the selected window, with
navigation (previous/next 12 weeks) and a custom range reloading the data
(contracts §4.5 / API-002).

## Reproduction

1. Start the local stack and seed the 5 tasks.
2. Open `/calendar` and inspect the heatmap.
3. Before the fix: every cell `count=0`, `tasks=[]`; prev/next only rebuilt the
   local zeros. The API responses were never bound to the grid.

## Impact and triage

- Severity rationale: P2 — the calendar view is unusable; the rest of the
  service is unaffected.
- Affected users/data/security: UI users; no data/security impact.
- Workaround: query the API directly.
- Duplicate check: none.

## Root cause

- `CalendarView.vue` built `weeks` locally in `buildWeeks()` with `count: 0` and
  `tasks: []`; the Pinia store loaded a single week but the view never mapped
  `calendar.response` into the heatmap rows.
- Prev/next/apply-range only rebuilt the zero-filled grid (no API call).
- `Heatmap.vue` used `grid-template-columns: repeat(12, ...)` for 7-day rows.
- The store had no window loader (only one `view=week` request).

## Fix summary

- `src/web/src/stores/calendar.ts`: add `loadWeeks(baseDate, count=12)` — Mon-
  anchored window, 12 parallel `GET /api/calendar?view=week` calls, store
  `CalendarDay[][]`, `isEmpty`/`total` getters, error handling.
- `src/web/src/views/CalendarView.vue`: map the store weeks into `HeatmapDay`
  (`count = tasks.length`, `tasks = task ids`), reload on mount, prev/next,
  custom range, and retry; window label shows the date range; `isEmpty` from the
  store.
- `src/web/src/components/calendar/Heatmap.vue`: 7-column grid (one column per
  weekday) instead of 12.

## Verification

- `npm --prefix src/web run build` → exit 0.
- Docker frontend image rebuilt and restarted (`ai-task-tracker-frontend:latest`).
- Headless Chrome `/calendar` DOM: contains `2026-11-10 (2)` and
  `2026-11-11 (3)`, the heatmap cells render, no calendar error, no
  source-unavailable banner.
- Backend data unchanged (API already returned the correct payloads).

## Postmortem (P0/P1)

Not applicable (P2).

## Links

- `src/web/src/stores/calendar.ts`, `src/web/src/views/CalendarView.vue`,
  `src/web/src/components/calendar/Heatmap.vue`
- `docs/architecture/contracts-ai-task-tracker.md` §4.5,
  `docs/architecture/openapi-api-002-ai-task-tracker.yaml`
- Related UI contract fix: BUG-001 (health shape)

## Log

- 2026-11-17: reported by the user during the local Docker deployment; fixed;
  verified with a headless render; status `verified`.
