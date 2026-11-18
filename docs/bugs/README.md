# Product Bugs

Product defect registry for this project. One `BUG-XXX` card per bug, created
from `harness/templates/bug-card.md` via `/harness-bug`; harness problems belong
in `docs/issues/`.

| ID | Summary | Severity | Status | Tasks | Updated |
|---|---|---|---|---|---|
| [BUG-001](BUG-001-api-004-spec-diverges.md) | API-004 health spec diverges from the contracts | major | open | T-006, T-008 | 2026-11-14T21:10:00+03:00 |
| [BUG-002](BUG-002-dockerfile-source-paths.md) | Docker backend image build fails: pom sourceDirectory resolves outside the image | P1 | verified | T-013, T-014 | 2026-11-17T15:10:00+03:00 |
| [BUG-003](BUG-003-local-stack-not-healthy.md) | Local stack cannot reach a functional healthy state (Flyway/PG16, port, wiring, JSON shape, token periods) | P1 | verified | T-015, T-016 | 2026-11-17T15:10:00+03:00 |
| [BUG-004](BUG-004-global-ai-search-empty.md) | Global AI search with no task_id returns an empty result set | P2 | verified | - | 2026-11-17T16:20:00+03:00 |
| [BUG-005](BUG-005-calendar-heatmap-not-bound.md) | Calendar heatmap renders a local zero-filled grid and ignores /api/calendar | P2 | verified | - | 2026-11-17T16:45:00+03:00 |
| [BUG-006](BUG-006-calendar-encoding-ui.md) | Calendar UI Russian strings double-encoded; weekday header missing | P3 | verified | - | 2026-11-17T16:55:00+03:00 |
| [BUG-007](BUG-007-pre-existing-backend-test-failures.md) | 5 pre-existing backend test failures (SearchServiceTest ×3, TokenServiceTest ×2) block a clean full-suite run | P2 | open | T-024 | 2026-11-18T13:49:33+03:00 |
| [BUG-008](BUG-008-uc-e2e-004-ai-search-failures.md) | UC-E2E-004 AI-search e2e tests fail: `_post_json` tuple contract + HTTP 503 on English query | P2 | open | T-027 | 2026-11-18T19:57:47+03:00 |
