# ISSUE-20261113-115617: R-01 contracts file exceeded role session budget

| Field | Value |
|---|---|
| id | ISSUE-20261113-115617 |
| status | resolved |
| severity | minor |
| type | process |
| detected_at | 2026-11-13T11:56:17.7039321+03:00 |
| source | harness-architect |
| role | architect |
| location | docs/architecture/contracts-ai-task-tracker.md (22.9 KB) |
| environment | OS: Microsoft Windows NT 10.0.26300.0; cwd: C:\study\ai\research\ai-task-tracker |

## Symptoms

Chunk R-01b produced a 22.9 KB markdown file in one role session; process 11.1 max_session_kb is 20 KB for markdown.

## Reproduction

1. 
2. 
3. 

## Expected / actual behavior

- Expected:
- Actual:

## Cause

Contracts were authored in one session after the ADR chunk; the role did not split the contracts into sub-chunks.

## Solution / workaround

Accepted the artifact after content validation; future R-01/R-02 sessions must split contracts/diagrams into sub-chunks (data model+ingestion; REST+Ollama+traceability) or use a same-role self-sub-session.

## Verification

verify-stage -Task T-006 -Stage R-01 passes adr-exist, adr-sections, contracts-exist, architecture-requirement-trace (24/24).

## Prevention / follow-up

Card budget field 8/20/60 kept; R-02 will run in a separate session; record a budget check in the card log.

## Links

docs/tasks/T-006-architecture-contracts.md; docs/architecture/contracts-ai-task-tracker.md

## Timeline

- 2026-11-13T11:56:17.7039321+03:00: detected
