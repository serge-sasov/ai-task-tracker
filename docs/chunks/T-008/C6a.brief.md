# C6a: Vue shell - build config, API client, store, router

| Field | Value |
|---|---|
| task | T-008 |
| stage | I-01 |
| role | coder |
| depth | 1 |
| owns | `src/web/package.json`, `vite.config.ts`, `tsconfig.json`, `index.html`, `src/main.ts`, `src/App.vue`, `src/api/**`, `src/stores/**`, `src/router/**`, `src/assets/**` |
| budget | 12 KB write, 40 KB session, stop at 65% |

## Goal

Create the Vue 3 + TypeScript + Pinia SPA shell that the S-01..S-06 chunks plug
into.

## Inputs

- `docs/ux/ui-spec-ai-task-tracker.md` (design tokens, RU copy rules, states).
- `docs/architecture/openapi-api-001..004-ai-task-tracker.yaml` (client types).

## Work

- Vite project with no CDN assets; local fonts/icons only.
- Typed API client for API-001..API-004, one error type for the RU envelope.
- Pinia store: tasks, calendar, tokens, search, history, settings.
- Router with routes for S-01..S-06 pointing at the view files owned by C6b/C6c
  (import them; do not create them).
- `App.vue`: layout, navigation, RU locale labels.

## Acceptance

- [ ] `npm ci && npm run build` succeeds offline (no network install is
  required by the build step).
- [ ] Client types match the OpenAPI schemas; error envelope handled once.
- [ ] Routes exist for all six screens.

## Stop rule

Stop at the budget or when a file outside `owns` must change; record with
`chunk.ps1 fail` and a note.

## Return

At most ten lines: produced paths, build result, state, any issue id.
