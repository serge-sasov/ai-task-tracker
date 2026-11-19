# ISSUE-20261118-185907: harness-qa subagent SSE read timeout during T-026

| Field | Value |
|---|---|
| id | ISSUE-20261118-185907 |
| status | workaround |
| severity | major |
| type | tool |
| detected_at | 2026-11-18T18:59:07.2018446+03:00 |
| source | T-026 |
| role | harness-qa |
| location | harness-qa/opencode task session (SSE read timeout on initial call and resume) |
| environment | OS: Microsoft Windows NT 10.0.26300.0; cwd: C:\study\ai\research\ai-task-tracker |

## Symptoms

The `harness-qa` subagent failed to produce the Q-01 plan for T-026. Two **SSE read timeouts** occurred: once on the initial `task` call and once on the resume. In both attempts chunk C1 was **claimed** (`state` flipped in `docs/chunks/T-026.json`) but **no plan was produced** at `docs/qa/e2e-plan-task-list-model.md`. The subagent session terminated before writing any artifact, so the stage could not be closed by the role.

## Reproduction

1. Start T-026 (Q-01) and dispatch the `harness-qa` role to author `docs/qa/e2e-plan-task-list-model.md` (chunk C1).
2. The first `task` call times out on the server-sent-event read; the chunk is claimed but no file is written.
3. Resume the `harness-qa` role; the second SSE read also times out; still no plan file is produced.

## Expected / actual behavior

- Expected: `harness-qa` authors the e2e plan (requirement→UC→test matrix over UC-E2E-001..008 with a focused UC-E2E-003 S8 / R-014 block), marks C1 `done` with a produced hash, and reports back for CP-1.
- Actual: two SSE read timeouts; C1 was claimed but the plan was never written, so the role could not complete the stage.

## Cause

Not fixed. The `harness-qa` subagent transport (opencode `task` session) hit SSE read timeouts on both the initial call and the resume. The timeouts are an environment/transport issue, not a content or scope defect: the QA deliverable itself is small and within the C1 budget. The root cause (SSE read path / upstream model stream) is not fixed here.

## Solution / workaround

Lead fallback: the lead (orchestrator) authored the plan at `docs/qa/e2e-plan-task-list-model.md` using the T-026 card, UC-E2E-003/S8 (use cases + YAML), the T-025 e2e test files, and ISSUE-20261118-153622. QA checks ran against the result: the YAML validator reports `8 use cases, 25 requirement links, 0 errors, 0 warnings`; `python -m py_compile` passes on the two e2e modules. The plan covers all of UC-E2E-001..008 with a dedicated UC-E2E-003 S8 / R-014 block, the test-data validation command, the execution approach (targeted + full suite), the ISSUE-20261118-153622 environment caveat, and entry/exit criteria with evidence expectations.

Additional mitigation (2026-11-19): the `ollama-metrics` and `ollama` providers in `~/.config/opencode/opencode.jsonc` now set `chunkTimeout: 900000` and `headerTimeout: 600000` (defaults were 300000 each). This raises the SSE inter-chunk window from 5 to 15 minutes and the response-header window from 5 to 10 minutes. `opencode debug config` resolves the values; a new opencode process loaded the updated agent config and completed tool calls; a read-only `harness-qa` smoke run and the MTP benchmarks completed without transport errors. The currently running opencode process must be restarted to pick up the new provider timeouts. The root cause remains a transport stall, so the status stays `workaround` until a large Q-01/Q-02 generation is retried end-to-end.

## Verification

- `docs/qa/e2e-plan-task-list-model.md` exists and lists every documented UC id: UC-E2E-001..UC-E2E-008 (satisfies `e2e-plan-uc-coverage`).
- Requirement→UC→test matrix present (`e2e-plan-matrix`); focused UC-E2E-003 S8 / R-014 block present (ordered `models` list, empty state for TASK-002, `+N`, tooltip/aria, `GET /api/tasks`).
- YAML validator: `8 use cases, 25 requirement links, 0 errors, 0 warnings`.
- `python -m py_compile` passes on `shared.py` and `test_uc_e2e_003_task_list_detail.py`.
- C1 is `done` with a produced hash in `docs/chunks/T-026.json`; ledger VALID.

## Prevention / follow-up

Retry the `harness-qa` role on the next QA task (T-027 / Q-02) and confirm it completes without SSE read timeouts. If the timeouts persist, treat the subagent transport as a harness defect (raise a dedicated issue and/or a follow-up task) rather than relying on lead fallback repeatedly. Keep the lead-fallback path as the recovery for the current task only. After the provider timeout change, retry a large Q-01/Q-02 generation end-to-end and keep the chunked-QA rule (split reports into 1-2 KB sections) as the primary prevention.

## Links

T-026 (Q-01); `docs/tasks/T-026-e2e-plan-model-column.md`; `docs/qa/e2e-plan-task-list-model.md`; `docs/chunks/T-026.json`; `docs/checkpoints/T-026-CP-1.md`, `docs/checkpoints/T-026-CP-F.md`; related environment issue ISSUE-20261118-153622.

## Timeline

- 2026-11-18T18:59:07.2018446+03:00: detected (two SSE read timeouts; C1 claimed, no plan)
- 2026-11-18: lead fallback wrote the plan; QA validator + py_compile passed; C1 marked `done`.
- 2026-11-19: added `chunkTimeout: 900000` / `headerTimeout: 600000` to the `ollama` and `ollama-metrics` providers; config validated; read-only QA smoke passed. Large QA retry still pending.
