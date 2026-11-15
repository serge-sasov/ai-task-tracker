# C6b: Screens S-01..S-03

| Field | Value |
|---|---|
| task | T-008 |
| stage | I-01 |
| role | coder |
| depth | 1 |
| owns | `src/web/src/views/DashboardView.vue`, `TaskView.vue`, `SearchView.vue`, `src/web/src/components/{dashboard,task,search}/**` |
| budget | 12 KB write, 40 KB session, stop at 65% |

## Goal

Implement the dashboard (S-01), task detail (S-02), and AI search (S-03)
screens with RU copy and all specified states.

## Inputs

- `docs/ux/screens-ai-task-tracker.md` rows S-01, S-02, S-03.
- `docs/ux/ui-spec-ai-task-tracker.md` (components, states, accessibility,
  tokens, RU copy).

## Work

- S-01: statistics cards (running, waiting, completed today, completed this
  week), token summary, recent activity, search field; empty/loading/error
  states.
- S-02: summary, steps (progress + time), solutions (description + rendering
  duration), error block; no editing.
- S-03: search field with label, top-K 5 results with score, sources, empty and
  LLM-unavailable states.
- Use only the C6a store/client; do not modify C6a or C6c files.
  Exception: you may add one line to `src/web/src/main.ts`
  (`import './views';`) to trigger side-effect view registration.
  No other C6a file may change.

## Acceptance

- [ ] Every element and state from the screens/UI spec is present.
- [ ] RU copy; no English UI strings.
- [ ] `aria` attributes for tables, dialogs, and form labels.
- [ ] `npm run build` succeeds.

## Stop rule

Stop at the budget or when a shared file outside `owns` must change; record with
`chunk.ps1 fail` and a note.

## Return

At most ten lines: produced paths, build result, state, any issue id.
