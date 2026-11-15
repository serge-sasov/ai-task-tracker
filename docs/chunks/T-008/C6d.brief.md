# C6d: Frontend build wiring and full verification

| Field | Value |
|---|---|
| task | T-008 |
| stage | I-01 |
| role | coder |
| depth | 1 |
| owns | `Dockerfile`, `docker-compose.yml`, `Makefile`, `src/web/Dockerfile`, `src/web/nginx.conf`, `src/web/.dockerignore` |
| budget | 12 KB write, 40 KB session, stop at 65% |

## Goal

Make the one-command stack real: the frontend is built and served, the API is
proxied, ports stay on 127.0.0.1, and `make test` runs the full backend suite.

## Inputs

- Current `Dockerfile`, `docker-compose.yml`, `Makefile`.
- `docs/ux/ui-spec-ai-task-tracker.md` (base path, static hosting notes).

## Work

- `src/web/Dockerfile`: multi-stage node build -> nginx static serve.
- `src/web/nginx.conf`: SPA history fallback and `/api` proxy to the backend.
- `docker-compose.yml`: replace the frontend stub with the build; keep every
  port bound to `127.0.0.1`.
- `Makefile`: `up`, `down`, `test`, `build`, `logs` verified; `test` runs
  `mvn clean test`.

## Acceptance

- [ ] `docker compose config` validates; the frontend service builds.
- [ ] All published ports are `127.0.0.1:...`.
- [ ] `make test` green; `make build` produces a single backend jar.

## Stop rule

Stop at the budget; record with `chunk.ps1 fail` and a note if a service outside
`owns` must change.

## Return

At most ten lines: produced paths, command results, state, any issue id.
