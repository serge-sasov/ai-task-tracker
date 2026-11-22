# ISSUE-20261118-185907: harness-qa subagent SSE read timeout during T-026

| Field | Value |
|---|---|
| id | ISSUE-20261118-185907 |
| status | resolved |
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

Root cause: the provider SSE windows (`chunkTimeout`/`headerTimeout`, 300000 ms each) were shorter than the pauses during a large QA generation, so the `harness-qa` task session aborted after claiming C1. Fixed on 2026-11-19 by raising `chunkTimeout` to 900000 ms and `headerTimeout` to 600000 ms for the `ollama`/`ollama-metrics` providers, then verified end-to-end by the large Q-01/Q-02 runs on 2026-11-22.

## Solution / workaround

Resolved. The provider timeouts were raised on 2026-11-19 (`chunkTimeout: 900000`, `headerTimeout: 600000`). Large `harness-qa` sessions completed without SSE read timeouts on 2026-11-22: T-038 Q-01 produced `docs/qa/e2e-plan-s04-model.md`, and T-039 Q-02 produced `docs/qa/e2e-report-s04-model.md` plus `docs/qa/review-T-039.md` (verdict ACCEPT). The lead-fallback path is no longer required.

## Verification

- `docs/qa/e2e-plan-task-list-model.md` exists and lists every documented UC id: UC-E2E-001..UC-E2E-008 (satisfies `e2e-plan-uc-coverage`).
- Requirement→UC→test matrix present (`e2e-plan-matrix`); focused UC-E2E-003 S8 / R-014 block present (ordered `models` list, empty state for TASK-002, `+N`, tooltip/aria, `GET /api/tasks`).
- YAML validator: `8 use cases, 25 requirement links, 0 errors, 0 warnings`.
- `python -m py_compile` passes on `shared.py` and `test_uc_e2e_003_task_list_detail.py`.
- C1 is `done` with a produced hash in `docs/chunks/T-026.json`; ledger VALID.
- 2026-11-22: T-038 Q-01 completed by `harness-qa` (CP-1 and CP-F approved); T-039 Q-02 completed by `harness-qa` (report plus `review-T-039.md`, verdict ACCEPT; CP-2 and CP-F green); no SSE timeout recorded.

## Prevention / follow-up

Implemented: keep the raised provider timeouts in `~/.config/opencode/opencode.jsonc` and keep QA reports chunked (1-2 KB sections). If a stall recurs, open a new issue rather than reusing this one.

## Links

T-026 (Q-01); `docs/tasks/T-026-e2e-plan-model-column.md`; `docs/qa/e2e-plan-task-list-model.md`; `docs/chunks/T-026.json`; `docs/checkpoints/T-026-CP-1.md`, `docs/checkpoints/T-026-CP-F.md`; T-038, T-039; related environment issue ISSUE-20261118-153622.

## Timeline

- 2026-11-18T18:59:07.2018446+03:00: detected (two SSE read timeouts; C1 claimed, no plan)
- 2026-11-18: lead fallback wrote the plan; QA validator + py_compile passed; C1 marked `done`.
- 2026-11-19: added `chunkTimeout: 900000` / `headerTimeout: 600000` to the `ollama` and `ollama-metrics` providers; config validated; read-only QA smoke passed.
- 2026-11-22T18:48:30+03:00: resolved - large Q-01 (T-038) and Q-02 (T-039) `harness-qa` sessions completed without transport errors.
