# C5a: API-002 calendar and tokens

| Field | Value |
|---|---|
| task | T-008 |
| stage | I-01 |
| role | coder |
| depth | 1 |
| owns | `src/main/java/ru/aitasktracker/api/calendar/**`, `.../api/tokens/**`, matching test packages |
| budget | 12 KB write, 40 KB session, stop at 65% |

## Goal

Implement `GET /api/calendar` and `GET /api/tokens` exactly per OpenAPI API-002,
in new subpackages so no existing file changes.

## Inputs

- `docs/architecture/openapi-api-002-ai-task-tracker.yaml` (whole file, 4.5 KB).
- `docs/architecture/contracts-ai-task-tracker.md` lines 86-270 (section 4 REST
  API and section 5 error model only; do not read the rest of the file).

## Work

- `api/calendar/`: controller, service, DTOs for the calendar range query.
- `api/tokens/`: controller, service, DTOs for the token summary.
- Reuse the existing repository layer; do not modify files outside `owns`.
- Validate inputs; return the RU error envelope on bad input.

## Acceptance

- [ ] Response bodies, query parameters, defaults, and error codes match API-002.
- [ ] Integration/slice tests for both endpoints, RU envelope covered.
- [ ] `mvn clean test` green; no TODO/FIXME; no secrets.

## Stop rule

Stop at the budget or when a shared file outside `owns` must change; record with
`chunk.ps1 fail` and a note, do not widen `owns` on your own.

## Return

At most ten lines: produced paths, test counts, state, any issue id.
