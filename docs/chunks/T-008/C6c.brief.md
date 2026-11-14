# C6c: Screens S-04..S-06

| Field | Value |
|---|---|
| task | T-008 |
| stage | I-01 |
| role | coder |
| depth | 1 |
| owns | `src/web/src/views/CalendarView.vue`, `HistoryView.vue`, `SettingsView.vue`, `src/web/src/components/{calendar,history,settings}/**` |
| budget | 12 KB write, 40 KB session, stop at 65% |

## Goal

Implement the calendar (S-04), history (S-05), and manage-tasks (S-06) screens
with RU copy, accessibility, and dialog semantics.

## Inputs

- `docs/ux/screens-ai-task-tracker.md` rows S-04, S-05, S-06.
- `docs/ux/ui-spec-ai-task-tracker.md` (12-week heatmap, filters, dialogs,
  confirmation rules).

## Work

- S-04: period presets, custom range, 12-week heatmap with weekday rows and a
  legend, day drill-down with "open task".
- S-05: filter by type/status/date, total counter, "delete history" action with
  confirmation and a progress/result state.
- S-06: list groups (in queue, in progress, completed), add dialog, edit dialog,
  delete confirmation, token counter, empty state; `role="dialog"`,
  `aria-modal="true"`, focus trap and focus return.
- Use only the C6a store/client; do not modify C6a or C6b files.

## Acceptance

- [ ] Every element and state from the screens/UI spec is present.
- [ ] RU copy; no English UI strings.
- [ ] Dialog and table accessibility per the UI spec.
- [ ] `npm run build` succeeds.

## Stop rule

Stop at the budget or when a shared file outside `owns` must change; record with
`chunk.ps1 fail` and a note.

## Return

At most ten lines: produced paths, build result, state, any issue id.
