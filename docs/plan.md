# Plan (board)

Process rules: `harness/process.md`.
Workflows: `harness/workflows/feature.md`, `harness/workflows/bugfix.md`.
Diagram: `docs/activity.puml`. Updated after every task.

## Status legend

| Status | Diagram color | Meaning |
|---|---|---|
| `pending` | gray `#D3D3D3` | not started |
| `in_progress` | yellow `#FFE082` | in progress |
| `awaiting_review` | blue `#87CEFA` | waiting for user checkpoint review |
| `done` | green `#90EE90` | completed |
| `failed` | red `#FF9090` | error, fix required |
| `blocked` | orange `#FFB347` | external blocker |

At most one task is active (`in_progress` or `awaiting_review`) across both workflows.

## Board

| ID | Workflow | Task | Role | Dependencies | Status | Bug | Artifacts | Commit |
|---|---|---|---|---|---|---|---|---|
