# T-014 - QA verification review (BUG-002 / Dockerfile source paths)

| Field | Value |
|---|---|
| task | T-014 (B-03) |
| bug | BUG-002 |
| role | qa (harness-qa) |
| base_commit | 90c22f7 |
| fix commits | T-013 CP-1 349e7e2, CP-2 dd0f659 |
| reviewed_at | 2026-11-17 |
| scope | Independent fail-before/pass-after, regression, real build, stack health, collateral check |

## Scope

Independently verify the T-013 fix for **BUG-002** (the Docker backend image could
not build because the base `Dockerfile` copied sources to `/app/src/...` while
`backend/pom.xml` resolves `${project.basedir}/../src/main/java` to
`/src/main/java`). Out of scope: the separate stack-health defects (BUG-003,
verified by T-016) and the full T-009 suite.

## Fail-before evidence

`git show 90c22f7:Dockerfile` (base) + `backend/pom.xml`, same invariant as the
regression test:

```
WORKDIR (build stage)      : /app
pom sourceDirectory        : ${project.basedir}/../src/main/java -> /src/main/java
COPY src/main/java to      : ['/app/src/main/java']
MISMATCH: pom expects /src/main/java, Dockerfile copies to ['/app/src/main/java']
check_exit=1
```

The base Dockerfile therefore compiled zero classes and spring-boot repackage had
no main class.

## Pass-after evidence

Current Dockerfile: `WORKDIR /app/backend` + `COPY src/main/java ../src/main/java`
(+ resources) + runtime `COPY --from=build /app/backend/target/*.jar`.

- Regression: `python -m pytest tests/regression/test_dockerfile_source_paths.py -q`
  → **2 passed**.
- Real build: `docker compose -f docker-compose.yml build backend` → **exit 0**;
  image `ai-task-tracker-backend:latest`; `/app/app.jar` present; the application
  main class loads and Tomcat initialises.
- Stack health after the BUG-003 fixes (T-015/T-016): `/api/health` → **200**
  (`postgres=ok`, `redis=ok`; `degraded` only because Ollama is host-only).

## Regression

`python -m pytest tests/regression/ -q` → **4 passed**
(`test_dockerfile_source_paths.py` 2 + `test_stack_health_prereqs.py` 2).

## Real build and health

- Build exit 0; image present; jar present.
- Health 200 after BUG-003; no main-class failure remains.

## Collateral changes

`git diff --stat 90c22f7..HEAD`: only `Dockerfile`, `tests/regression/**`,
product fixes for BUG-003, and docs/chunks/checkpoints/evidence/plan.
No unrelated behavior changed.

## Residual risks

- BUG-003 residual risks apply (host-only Ollama → AI-search skips; legacy
  `spring.data-redis.host` key → in-memory lock). Neither affects the BUG-002 fix.
- Large Docker build context (no root `.dockerignore`): cosmetic/performance only.

## Verdict

**PASS** — fail-before reproduced, the minimal fix passes the regression, the real
image builds and boots, `/api/health` is 200 after BUG-003, and no collateral
changes were introduced. BUG-002 is cleared; the earlier FAIL was caused solely by
the separate BUG-003 stack-health defects, now fixed and verified.
