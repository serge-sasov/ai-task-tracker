# C6b: Screens S-01..S-03

| Field | Value |
|---|---|
| task | T-008 |
| stage | I-01 |
| role | coder |
| depth | 1 |
| owns | `src/web/src/views/DashboardView.vue`, `TaskView.vue`, `SearchView.vue`, `views/index.ts`, `src/web/src/components/{dashboard,task,search}/**` |
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

### View registration (required)

The router resolves views at runtime from a registry. No view file imports
your views, so without registration the routes render an empty placeholder.

Create `src/web/src/views/index.ts` (this file is in `owns`):

```ts
import { registerView } from '../router';
registerView('DashboardView', () => import('./DashboardView.vue'));
registerView('TaskView', () => import('./TaskView.vue'));
registerView('SearchView', () => import('./SearchView.vue'));
```

Note: `registerView` is the one exported by `src/web/src/router/index.ts`.
Do not import from `src/web/src/registry/views.ts` — that module is not wired
to the router.

Then add one side-effect import to `src/web/src/main.ts` (C6a-owned file;
this single line is the only allowed C6a change):

```ts
import './views/index';
```

Place it after `import router from './router';`.

## Acceptance

- [ ] Every element and state from the screens/UI spec is present.
- [ ] RU copy; no English UI strings.
- [ ] `aria` attributes for tables, dialogs, and form labels.
- [ ] `views/index.ts` registers DashboardView, TaskView, SearchView via
      `registerView` from `../router`; `main.ts` imports `./views/index`.
- [ ] `npm run build` succeeds.

## Stop rule

Stop at the budget or when a shared file outside `owns` must change; record with
`chunk.ps1 fail` and a note.

## Return

At most ten lines: produced paths, build result, state, any issue id.
