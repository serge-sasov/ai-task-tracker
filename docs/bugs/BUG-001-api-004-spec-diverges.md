# BUG-001: API-004 health spec diverges from the contracts

| Field | Value |
|---|---|
| id | BUG-001 |
| title | API-004 health spec diverges from the contracts |
| severity | major |
| status | open |
| detected_at | 2026-11-14T21:10:00+03:00 |
| source | T-045 replay (T-006.R1) |
| component | docs/architecture |
| tasks | T-006, T-008 |
| updated | 2026-11-14T21:10:00+03:00 |

## Symptoms

`docs/architecture/openapi-api-004-ai-task-tracker.yaml` does not match
`docs/architecture/contracts-ai-task-tracker.md` section 4.1 and section 5. The
backend implementation follows the spec file, so `GET /api/health` returns a
different body than the contract requires.

## Expected / actual behavior

| Item | Contract | Accepted spec |
|---|---|---|
| Response body | `status`, `checks.postgres`, `checks.redis`, `source_status`, `ollama_status`, `last_ingest_at` | flat `status`, `backend`, `database`, `ollama` |
| `status` | `ok` string | enum `[ok, degraded]` |
| `required` | `status`, `checks`, `source_status`, `ollama_status` | `status`, `backend`, `database` |
| 503 | `SERVICE_UNAVAILABLE` response documented | absent |
| Error envelope | shared `Error` schema `{code,message}` | absent |

## Reproduction

Regenerate the spec from the contracts and diff it with the accepted file
(`docs/architecture/replay/replay-api-004.yaml` is the regenerated version):
`diff` shows the five differences above.

## Cause

The R-01 stage produced the contracts and the OpenAPI specs in one session; the
spec for API-004 was authored before the health row was finalized in the
contract, and no check compares spec fields against the contract text.

## Solution

Choose one source and align the other; the contracts are the design source of
truth, so update the spec (or record an ADR if the spec shape is preferred) and
then align the backend `HealthResponse` and its tests.

## Verification

- `validate-openapi.py` passes on the updated spec.
- A slice test asserts the documented fields and the 503 case.
- The architecture description's REST API table and the contracts agree.

## Prevention / follow-up

- `contracts-requirement-trace` covers requirements, not field-level agreement
  between a spec and the contract prose. A future check could compare operation
  ids and required fields, but the pair is small enough for a manual review item
  at R-02.

## Links

- `docs/architecture/contracts-ai-task-tracker.md` section 4.1, section 5
- `docs/architecture/openapi-api-004-ai-task-tracker.yaml`
- `docs/architecture/replay/replay-api-004.yaml` (regenerated)
- `docs/tasks/T-006-architecture-contracts.md`, `docs/tasks/T-008-implementation-tests.md`

## Log

- 2026-11-17: the SPA health types/store were aligned to the contracts shape (status, checks.postgres/redis, source_status, ollama_status, last_ingest_at); the Dashboard no longer reports «Источник данных (opencode) недоступен» when the backend is healthy. The API-004 YAML spec still diverges and remains to be regenerated from the contract.
