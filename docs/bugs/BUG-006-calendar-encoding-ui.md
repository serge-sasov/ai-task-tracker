# BUG-006: Calendar UI Russian strings are double-encoded; weekday header missing

| Field | Value |
|---|---|
| id | BUG-006 |
| title | Calendar UI strings are double-encoded (UTF-8→CP1251→UTF-8); weekday header not rendered |
| status | verified |
| severity | P3 |
| reported_at | 2026-11-17T16:55:00+03:00 |
| reporter | user (during the local Docker deployment) |
| affected | `/calendar` headings, presets, dialog title/text, weekday labels, updated label |
| requirements | R-030 (RU UI) |
| use_cases | UC-E2E-005 |
| tasks | - (fix applied during the local deployment) |
| duplicate_of | - |
| commit | calendar encoding/UI fix |
| issues | - |

## Observed behavior

The calendar page rendered mojibake, e.g. the day dialog showed
`РћС‚РєСЂС‹С‚СЊ Р·Р°РґР°С‡Сѓ вЂ” 2026-11-10` and
`Р—Р°РґР°С‡Рё РЅР° 2026-11-10. Р’С‹Р±РµСЂРёС‚Рµ Р·Р°РґР°С‡Сѓ, С‡С‚РѕР±С‹ РѕС‚РєСЂС‹С‚СЊ РґРµС‚Р°Р»Рё.`;
the H1/presets/updated label were garbled too. The weekday header (Пн–Вс) was
missing entirely, and the day cells were small (24 px).

## Expected behavior

Correct Russian text on the calendar, a visible Пн–Вс weekday header, and
comfortable day cells.

## Reproduction

1. Open `http://127.0.0.1:8081/calendar`.
2. Before the fix: mojibake headings/presets/dialog; no weekday header; 24 px cells.

## Impact and triage

- Severity rationale: P3 — cosmetic/localization and layout; data and logic work.
- Affected users/data/security: RU UI users; no data/security impact.
- Workaround: none needed after the fix.
- Duplicate check: none. BUG-005 covered the calendar data binding.

## Root cause

- `CalendarView.vue` and `Heatmap.vue` contained Russian strings stored as the
  result of decoding correct UTF-8 bytes as CP1251 and re-encoding them as UTF-8
  (double encoding). The browser faithfully displayed the mojibake.
- `Heatmap.vue` used `<WeekdayRow>` without importing/registering the component,
  so Vue rendered nothing for the weekday header.
- `WeekdayRow.vue`'s `aria-label` prefix was double-encoded.
- The heatmap grid/cells used `--space-5` (24 px).

## Fix summary

- Reverse-decoded `CalendarView.vue` and `Heatmap.vue`
  (`text.encode('cp1251').decode('utf-8')`), verified zero mojibake markers;
  corrected the `WeekdayRow` aria-label to «День недели: …».
- Imported and registered `WeekdayRow` in `Heatmap.vue`.
- Enlarged day cells to 40 px (`repeat(7, 40px)`, cell 40×40, count 12 px, task
  label 10 px); weekday header switched to a flex row aligned with the grid.

## Verification

- `npm --prefix src/web run build` → exit 0.
- Docker frontend image rebuilt/restarted.
- Headless Chrome screenshot of `/calendar`: correct Russian («Календарь»,
  «12 недель», «Произвольно», «Обновлено …»), weekday header «Пн Вт Ср Чт Пт Сб
  Вс», 40 px cells with counts 2/3 and task ids on 2026-11-10/11.
- Source string checks (`unicode_escape`) confirm correct UTF-8 for the dialog
  title/text, presets and weekday labels.

## Postmortem (P0/P1)

Not applicable (P3).

## Links

- `src/web/src/views/CalendarView.vue`,
  `src/web/src/components/calendar/Heatmap.vue`,
  `src/web/src/components/calendar/WeekdayRow.vue`
- Related: BUG-001 (health shape), BUG-005 (calendar data binding), R-030.

## Log

- 2026-11-17: reported by the user; fixed and verified with a headless render;
  status `verified`.
