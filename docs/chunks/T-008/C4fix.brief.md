# C4fix: Fix API defects A/B/C and add context wiring test

| Field | Value |
|---|---|
| task | T-008 |
| stage | I-01 |
| role | coder |
| depth | 1 |
| owns | the 8 files listed in the ledger entry C4fix |
| budget | 12 KB write, 40 KB session, stop at 65% |

## Goal

Apply the finalized fix plan A/B/C from the task card and make the application
context resolvable at startup.

## Inputs

- `docs/tasks/T-008-implementation-tests.md` lines 55-60 (defects and finalized
  fix plan).
- `docs/architecture/contracts-ai-task-tracker.md` section 5 (RU error envelope,
  canonical messages).

## Work

- Fix A: `ApiErrorAdapter` returns `ResponseEntity<ApiError>` with
  `{"code","message"}`; delete the `ApiHandle` wrapper.
- Fix B: remove duplicate bean registrations (5 `@Bean` methods in
  `ApiConfiguration`, 2 in `IngestAutoConfiguration`, `@Component` on
  `InMemoryTaskLock` and `RedisTaskLock`); keep a single `@Bean taskLock()`.
- Fix C: add `NotFound()` with the canonical `MESSAGE`; use it in
  `TaskQueryService.detail()`.
- Test: `tests/java/ru/aitasktracker/api/ApiContextWiringTest.java` with
  `AnnotationConfigApplicationContext`, overriding disabled, mock singletons, and
  component scan of `ru.aitasktracker.api` + `ru.aitasktracker.ingestion.lock`.
  It must fail before Fix B and pass after.

## Acceptance

- [ ] `{code,message}` envelope in errors, no `ApiHandle` references.
- [ ] Wiring test fails before Fix B (record that run) and passes after.
- [ ] Canonical RU message for a missing task.
- [ ] `mvn clean test` green (98+ tests) and `mvn clean package` BUILD SUCCESS.

## Stop rule

Stop when a file would exceed `max_write_kb`, the context reaches
`context_stop_pct`, or a dependency outside `owns` must change. Record with
`chunk.ps1 complete` / `chunk.ps1 fail`.

## Return

At most ten lines: produced paths, test result, state, any issue id.
