# Harness time reduction plan

Date: 2026-11-21
Status: implemented 2026-11-21 (H-01..H-09); self-tests and doctor passed.
H-09 is a process guard + spike; the runtime transport retry hook remains external.
Owner: single-model execution (current opencode model)

## Execution policy

- All changes — code, tests, docs, verification — are made by the current
  opencode model only.
- Do not use `local-coder`, `delegate-local`, LM Studio, or Ollama for code
  generation, review, or patching.
- Do not delegate edits to subagents.
- RAG: indexing only. Do not use RAG search for code changes; use `read`,
  `grep`, `glob`, and direct inspection.
- Commit only when explicitly requested by the user.

## Baseline

T-033 U-02 (S-04 new screen):

- work time: 3 h 49 min;
- chunk authoring: 19 min 15 sec (c1 2 min 29, c2 13 min 06, c3 3 min 40);
- process/context: 2 h 11 min (46 min chunk-budget triage, 62 min
  approval/precedent, 17 min context/compaction, 6 min budget fix);
- calendar: 4 h 26 min (4:26:40); review 39 min;
- 11 sessions, 644,320 input, 4,573,594 cache, 307 usage minutes;
- T-021 U-02 delta: 5 min; T-005 full U-02: 8 h 06 min.

Root causes:

1. `chunk.ps1 init` hardcodes 12/40 KB and ignores stage/task budgets.
2. `verify-artifacts.ps1` and `chunk-validate.py` measure final file size, not
   authored delta; derived SVG and pre-existing file size cause false FAILs.
3. There is no deterministic command for recording an intermediate checkpoint
   user decision; the lead spent 62 min on precedent/mechanism.
4. Preflight is optional and not required before `stage-done.ps1`.
5. U-02 delta and new-screen tasks are not classified separately.
6. `usage.min` is 0 for chunks; authoring time is invisible.
7. Compaction/session handoff is a process rule but not enforced.
8. CP-2 and CP-F can ask the same manual confirmation twice.

## Backlog

### H-01 (P0): budget profiles + authored-delta chunk budget

Changes:

- Add `harness/budgets.json`:
  - per-type limits: md 8/20, puml 8/20, openapi 6/15, yaml 6/15, code 12/40,
    html 12/40;
  - stage profiles, including U-02 new-screen 80/200 and delta 40/100;
  - exclusions (`*.svg`, `package-lock.json`, lockfiles, build outputs).
- `chunk.ps1 init`: add `-Profile`, `-ScopeClass`, `-BudgetMode`; write
  `budget.mode`, `budget.profile`, `budget.scope_class`, `budget.per_type`,
  `budget.exclude`; no silent 12/40 default.
- `chunk-validate.py`: authored-delta budget for `budget.mode=authored-delta`;
  base/current file-size delta for tracked files and file size for new files;
  exclude derived/generated; `E_BUDGET` on authored limits.
- `verify-artifacts.ps1`: budget rule uses the validator for authored mode;
  keeps legacy final-size behavior for ledgers without `budget.mode`.
- `check-budget.ps1`: mark as legacy final-size helper; authoritative check is
  the chunk ledger budget rule.
- Update `harness/checks/feature.json`/`bugfix.json` only if needed.
- Update templates and `harness/process.md` 11.1.

Acceptance:

- a U-02 ledger with 80/200 and small authored edits passes;
- a new 9 KB Markdown file fails the per-type 8 KB write limit;
- a 70 KB derived SVG and package-lock are excluded;
- historical ledgers without `budget.mode` keep legacy behavior;
- self-tests cover pre-existing large file + small edit, derived SVG,
  generated lockfile, new oversized file.

### H-02 (P0): checkpoint decision command

- Add `harness/scripts/checkpoint-decision.ps1`:
  - `-Task -Checkpoint -Decision approved|changes_requested|rejected -Remark
    [-Resume]`;
  - validates `awaiting_review`; writes decision/reviewed_at/reviewer=user;
  - updates card/plan through `board-update.ps1`;
  - commits `T-XXX: CP-N <decision> (user)`;
  - transitions: intermediate approved -> in_progress; CP-F approved -> done;
    changes_requested -> in_progress; rejected -> failed.
- Update `process.md`, `harness.md`, `harness-orchestrator.md`, and the
  `/harness-run` command instructions.

Acceptance: four decision scenarios in a temp task; no manual machine-field
edits; next action printed.

### H-03 (P0): mandatory preflight

- Add preflight mode to `verify-stage.ps1` (checks + budget summary, no
  checkpoint write) or a `preflight.ps1` wrapper.
- `chunk.ps1 complete` prints the required preflight command.
- Orchestrator/process require preflight after the last chunk and before
  `stage-done.ps1`.

Acceptance: budget/coverage FAIL is visible before CP and user review; result is
recorded in the card log.

### H-04 (P1): scope class and estimates

- Add `scope_class` = `delta | new-screen | cross-cutting` to feature/bugfix
  card templates and planning instructions.
- Map scope class and stage to a budget profile; record estimate notes.

Acceptance: T-021-like delta and T-033-like new-screen receive different
profiles and estimates.

### H-05 (P1): per-chunk time and usage

- `chunk.ps1 claim` records `started`; `complete` records `finished` and
  `elapsed_min`.
- Best-effort `measure-tokens.ps1 -ByChunk` updates usage/sessions in the ledger.
- `stage-done.ps1` chunk table shows elapsed/usage.

Acceptance: authoring time is visible for every chunk even when token usage is
unavailable.

### H-06 (P1): compaction and session handoff

- Add `harness/templates/session-handoff.md`.
- On compaction: stop the chunk, write a handoff (goal, done, next, pointers,
  stop rule), continue in a fresh session; do not re-read completed chunks.
- Add sessions/compactions fields and warnings to chunk validation; warn when a
  compacted chunk has no handoff.

Acceptance: simulated compaction produces a handoff and a clean resume.

### H-07 (P2, opt-in): manual carryover

- Add `manual_carryover: true|false` to cards (default false).
- At CP-F, when artifacts are byte-identical to CP-2 and the user confirmed the
  manual item at CP-2, mark it `CARRIED` instead of asking again.
- CP-F remains a user checkpoint; no auto-approval is added.

Acceptance: a U-02-like task does not ask `proto-ru-single-user` twice when
artifacts are unchanged.

### H-08 (P2): time report

- Add `harness/scripts/time-report.ps1`:
  - work, authoring, process/context, review, calendar, sessions, input, cache,
    false FAILs;
  - Markdown/JSON output to `docs/metrics/`.
- It must reproduce the U-02 deep-dive numbers for T-021/T-033/T-005.

### H-09 (P2): transport/retry guard

- Spike the `/harness-run`/agent timeout behavior.
- On transport timeout/SSE abort: if an artifact exists, resume/validate; if
  not, mark the chunk failed and split; never repeat the same oversized call.
- Record the issue and the resume path.

## Sequencing

1. H-01
2. H-02, H-03
3. H-04, H-05
4. H-06, H-07
5. H-08, H-09
6. Full regression, doctor, plan update

## Migration and compatibility

- `budget.mode=authored-delta` for new ledgers.
- Ledgers without `budget.mode` use `legacy-final-size`; historical T-021/T-033/
  T-036 are not rewritten.
- P2, CP-F, and manual checks are not weakened.
- Product tasks, checkpoints, diagrams, and `docs/plan.md` are not changed.

## Testing

- `python harness/scripts/chunk-validate.py --self-test`
- `powershell -File harness/scripts/verify-artifacts.ps1 -SelfTest`
- temp task T-999/T-998 with a real git base commit;
- checkpoint decision script: four scenarios;
- approval/preflight smoke;
- `doctor.ps1`;
- read-only regression on existing ledgers.

## Target metrics

- T-033-like process/context: 131 min -> <= 30 min.
- false chunk-budget FAIL: 0 for U-02 tasks that extend existing files or
  produce derived SVG.
- approval time: user decision -> next action <= 2 min.
- authoring time visible for 100% of chunks.
- sessions/input/cache tracked per chunk where available.

## Out of scope

- Model latency, reasoning effort, and runtime throughput.
- Product scope and test data.
- User review policy beyond H-07 opt-in.
