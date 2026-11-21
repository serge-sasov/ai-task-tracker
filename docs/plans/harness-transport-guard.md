# Harness transport/retry guard (H-09 spike)

Date: 2026-11-21
Status: design spike; process guard implemented, runtime hook external

## Observed cases

- T-026 Q-01: SSE timeout during a long QA session
  (`ISSUE-20261118-185907`); the run was resumed, but the retry path was manual.
- T-033 U-02: automatic context compaction at 10:34; a continuation session
  resumed from a summary. The authoring chunks were already done, but the
  lead/approval work continued in the new session.
- T-036 I-01: scope narrowing and a long context; the task was completed, but the
  checkpoint required a user decision.

These are different failure modes: transport/SSE timeout, context compaction, and
scope/context exhaustion. The harness should stop at a chunk boundary, preserve
evidence, and avoid repeating an oversized call.

## Guard rules

1. A transport timeout or SSE abort is a chunk interruption, not a reason to
   resend the same prompt/session.
2. Inspect `docs/chunks/T-XXX.json` and the working tree:
   - if the chunk has produced artifacts: validate them (`chunk-validate.py`,
     hash/targeted read), then `chunk.ps1 complete` or `fail -Blocked` with a
     handoff;
   - if no artifact exists: `chunk.ps1 fail -Blocked -Issue ...` if external, or
     `fail` and split the chunk into smaller work.
3. Record the issue in `docs/issues/` when the transport/context failure recurs.
4. Write a `docs/chunks/T-XXX/<id>.handoff.md` from
   `harness/templates/session-handoff.md` for compaction or a fresh-session
   resume; do not re-read completed chunks.
5. Never repeat the same model request with the same oversized input. Split the
   chunk, reduce the input pointers, or stop and escalate.
6. The next `/harness-run T-XXX` reads the ledger and handoff before the card
   corpus; it must not infer success from an aborted tool call.

## Runtime hook

The opencode runtime owns the SSE/transport retry. The harness can only enforce
the deterministic parts above. If a future runtime exposes a retry hook, it must
call the same chunk state machine rather than replaying an arbitrary session.

## Acceptance for the spike

- A simulated timeout with an existing artifact resumes by validation, not by
  regenerating the artifact.
- A simulated timeout without an artifact marks the chunk failed/blocked and
  splits or escalates with an issue.
- A compaction produces a handoff and a fresh session starts from it.
