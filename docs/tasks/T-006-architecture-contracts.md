# T-006: R-01 Architecture and contracts (ADR)

| Field | Value |
|---|---|
| id | T-006 |
| title | R-01 Architecture and contracts (ADR) |
| workflow | feature |
| stage | R-01 |
| role | architect |
| status | awaiting_review |
| hold | false (true forces human review at every checkpoint) |
| depends_on | T-002, T-003, T-005 |
| inputs | docs/analysis/requirements-ai-task-tracker.md, docs/analysis/questions-ai-task-tracker.md, docs/analysis/e2e-use-cases-ai-task-tracker.md, docs/analysis/e2e-test-data-ai-task-tracker.yaml, docs/ux/user-flows-ai-task-tracker.md, docs/ux/screens-ai-task-tracker.md, docs/ux/ui-spec-ai-task-tracker.md, docs/ux/wireframes-ai-task-tracker.puml, docs/tasks/T-001-ai-task-tracker-feature-planning.md |
| artifacts | docs/architecture/adr-ai-task-tracker.md, docs/architecture/contracts-ai-task-tracker.md |
| e2e | required — contracts must cover UC-E2E-001..UC-E2E-008 and the YAML dataset |
| ux | required — screens S-01..S-06 and ui-spec must be reflected in the API/UI contract |
| chunks | R-01a: ADR decision and alternatives; R-01b: contracts (data model, ingestion, API, AI search, errors) |
| budget | 8 / 20 / 60 (max_write_kb / max_session_kb / context_stop_pct) |
| base_commit | 1799a8e |
| usage | in=335491 out=132909 cr=3268191 ratio=9.7x min=155 |
| issues | ISSUE-20261113-115617, ISSUE-20261113-124639 |
| commit | 2b6c829 |

## Acceptance

- [ ] ADR contains Context, Decision, Alternatives, Consequences and covers Q-001, Q-002, Q-003, Q-007, Q-008 decisions (opencode session files + polling; 1 task = 1 opencode session; duration = end - start; token usage from session usage fields; local Ollama AI search; LLM extraction of proposed/chosen solutions).
- [ ] Contracts define the PostgreSQL data model, ingestion file format and polling contract, REST API for tasks list/details/calendar/tokens/search/data deletion, AI search contract (local Ollama), error model, and health endpoint.
- [ ] Every requirement ID R-001..R-013 and R-020..R-030 is referenced in the ADR or contracts; screens S-01..S-06 and UC-E2E-001..008 are traceable.
- [ ] CP-1 and CP-F checkpoints recorded.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-1 | R-01 | `docs/architecture/adr-ai-task-tracker.md, docs/architecture/contracts-ai-task-tracker.md` | 5P/0F/0M | approved | docs/checkpoints/T-006-CP-1.md |
| CP-F | final acceptance | all task artifacts | 6P/0F/0M | awaiting_review | docs/checkpoints/T-006-CP-F.md |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.
`Checks` is a machine summary `nP/nF/nM` written by `stage-done.ps1`.

## Blockers

- (filled on status=failed|blocked: cause, output, what is needed to unblock)

## Log

- 2026-11-13: card created; R-01 started; harness upgraded to 2.5.0 (chore commit 1799a8e).
- ISSUE-20261113-115617: architect contracts chunk exceeded 20 KB session budget (minor, resolved)
- 2026-11-13: CP-1 approved (R-01); checks 5 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage in=331179 out=122364 cr=3071626 ratio=9.3x min=141; reviewer auto (P2)
- 6608194: CP-1 snapshot
- 2026-11-13: CP-F awaiting_review (R-01); checks 6 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage in=335491 out=132909 cr=3268191 ratio=9.7x min=155; reviewer user
- 2b6c829: CP-F snapshot
- ISSUE-20261113-124639: stage-done record step failed on git LF/CRLF warning; CP-1/CP-F record commits completed manually (workaround).
