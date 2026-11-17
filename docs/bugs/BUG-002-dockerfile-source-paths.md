# BUG-002: Docker backend image cannot build (pom sourceDirectory resolves outside the image)

| Field | Value |
|---|---|
| id | BUG-002 |
| title | Docker backend image build fails: pom sourceDirectory resolves outside the image |
| status | triaged |
| severity | P1 |
| reported_at | 2026-11-16T16:30:00+03:00 |
| reporter | lead (via orchestrator, at T-009 C7) |
| affected | `Dockerfile` (build stage), `backend/pom.xml` (path expressions), local Docker stack |
| requirements | not applicable (build/deployment infrastructure; blocks all UC-E2E-* verification) |
| use_cases | UC-E2E-001..UC-E2E-008 (blocked: no e2e results can be captured) |
| tasks | T-013, T-014 |
| duplicate_of | - |
| commit | - |
| issues | - |

Statuses: `new`, `triaged`, `in_progress`, `fixed`, `verified`, `closed`,
`duplicate`, `wontfix`. Only the orchestrator edits this card and
`docs/bugs/README.md`.

## Observed behavior

On the local stack, `docker compose -f docker-compose.yml build backend` fails.
The build stage sets `WORKDIR /app`, copies `backend/pom.xml` to `/app/pom.xml`
and the sources to `/app/src/main/java` + `/app/src/main/resources`, then runs
`mvn -q -f pom.xml -DskipTests package`. Because `backend/pom.xml` sets
`<sourceDirectory>${project.basedir}/../src/main/java</sourceDirectory>` and
`<resources><directory>${project.basedir}/../src/main/resources</directory></resources>`,
those paths resolve to `/src/main/java` and `/src/main/resources` (outside the
copied tree), so zero classes/resources are built and the spring-boot repackage
step fails (no main class). No `ai-task-tracker-backend` image is produced, the
stack never comes up, and zero e2e results are captured in
`tests/e2e/results/e2e-results.json`. Maven itself is healthy:
`mvn -f backend/pom.xml -DskipTests package` succeeds (last recorded build:
129 tests, 0 failures).

## Expected behavior

`mvn package` succeeds on the local stack and the built jar is copied to the
runtime stage. The local stack (postgres16+pgvector, redis7, backend, frontend
nginx) must come up for T-009 C7 (full e2e run) and for QA (T-011) to capture
results. Source of truth: `backend/pom.xml` (lines 59-64) and the `Dockerfile`
build stage.

## Reproduction

1. `docker compose -f docker-compose.yml build backend` (base revision).
2. Observe the backend build stage: `WORKDIR /app`, `COPY backend/pom.xml
   ./pom.xml`, `COPY src/main/java ./src/main/java`, `COPY src/main/resources
   ./src/main/resources`, then `RUN mvn -q -f pom.xml -DskipTests package`.
3. Observe the failure: the pom's `sourceDirectory` / `resources` point one
   level above the image root (`/src/main/...`), nothing is compiled, and the
   spring-boot repackage step reports no main class.

- Frequency: always, on the local stack, on the base revision (90c22f7).
- Environment: local Docker stack, `docker-compose.yml`, base commit 90c22f7.
- Evidence: `tests/e2e/results/e2e-results.json` (zero results captured);
  T-009 C7 hold note (2026-11-16T16:30:00+03:00).

## Impact and triage

- Severity rationale: P1. A major broken flow: the local stack cannot build the
  backend, which blocks the e2e automation (T-009 C7), QA (T-011), and any
  local run. It is not P0 (no data loss / security / outage) and not P2/P3 (it
  blocks the primary local workflow).
- Affected users/data/security: any local developer or CI running the stack;
  no data or security impact.
- Workaround: build the jar outside Docker and mount it (not reproducible; not
  a substitute for the fix).
- Duplicate check: none. BUG-001 is an API-004 spec divergence, unrelated.
- Escalation to feature-style work: no. The defect is a build-path fix inside
  the existing `Dockerfile` + `backend/pom.xml`; no public API, schema, or
  security change.

## Root cause

Filled by the coder at T-013 CP-1; reference the exact `Dockerfile` lines and
`backend/pom.xml:59-64`.

## Fix summary

Minimal change: keep `pom.xml` under `backend/` inside the image (e.g.
`WORKDIR /app/backend` + `COPY src/main/java ../src/main/java` +
`COPY src/main/resources ../src/main/resources`), so `sourceDirectory` and
`resources` resolve inside the image; adjust the runtime `COPY` to the new jar
path. Files: `Dockerfile`. Optional follow-up: a root `.dockerignore` (outside
the coder role write area; not required for correctness; build context is
~130 MB due to `src/web/node_modules` + `backend/target`). Commit: filled at
T-013 CP-2.

## Verification

QA fail-before/pass-after evidence in `docs/qa/review-T-014.md`:
- fail-before: on base commit, `docker compose build` backend stage fails.
- pass-after: after T-013 fix, `docker compose up --build` succeeds and the
  backend jar is present.
- regression: `tests/regression/test_dockerfile_source_paths.py` passes.
- residual risk: none expected (build-path only).

## Postmortem (P0/P1)

P1 requires a postmortem before closure (B-05): cause, detection, response,
prevention, follow-up task. Owner: orchestrator.

## Links

- Tasks: T-013 (fix), T-014 (QA)
- Reviews: `docs/qa/review-T-014.md`
- Related ADRs/contracts: `docs/architecture/contracts-ai-task-tracker.md` (deployment)
- Harness issues: -

## Log

- 2026-11-16: reported by lead at T-009 C7 (container backend build fails;
  Maven itself builds cleanly via `mvn -f backend/pom.xml`, 129 tests green).
- 2026-11-16: triaged P1; tasks T-013 (coder, B-01/B-02) and T-014 (QA, B-03)
  created pending; status set to `triaged`.
