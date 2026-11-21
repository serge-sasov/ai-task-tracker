# Templates: chunk ledger and brief

The ledger `docs/chunks/T-XXX.json` is the single source of truth for per-chunk
progress. The card and the board carry a summary only. Machine fields
(`state`, `attempts`, `produced.sha256`, `usage`, `base_commit`) are written by
`harness/scripts/chunk.ps1`; planning fields (`title`, `owns`, `inputs`,
`brief`, `acceptance`) are written at plan time.

Validator: `harness/scripts/chunk-validate.py`. Management:
`harness/scripts/chunk.ps1` (`init`, `add`, `plan`, `claim`, `complete`, `fail`,
`list`, `show`, `status`, `verify`).

## Ledger

| Field | Type | Meaning |
|---|---|---|
| `task` | string | `T-XXX`; must equal the file name |
| `version` | int | schema version, currently `1` |
| `updated` | string | ISO timestamp, written by the script |
| `baseline` | string[] | optional: paths that existed before the ledger was adopted; excluded from coverage |
| `budget.mode` | string | `authored-delta` (new ledgers) or `legacy-final-size` |
| `budget.profile` | string | profile name from `harness/budgets.json` |
| `budget.scope_class` | string | `delta`, `new-screen`, `cross-cutting`, or `default` |
| `budget.per_type` | object | per-artifact-type `max_write_kb`/`max_session_kb` |
| `budget.exclude` | string[] | derived/generated paths excluded from authored budget |
| `budget.max_write_kb` | number | legacy/planning fallback write limit |
| `budget.max_session_kb` | number | legacy/planning fallback session limit |
| `budget.context_stop_pct` | number | stop at this percentage of the context window |
| `budget.max_turns` | number | tool turns before a mandatory stop |
| `budget.max_input_tokens` | number | input tokens before a mandatory split |
| `budget.max_wall_min` | number | wall-clock minutes per chunk |
| `chunks` | array | non-empty list of chunk records |

`chunk-budget` compares **authored growth** against per-type limits; final file
size is not the budget. Derived/generated paths (`*.svg`, lockfiles, build
outputs) are excluded. `legacy-final-size` preserves historical behavior.

## Chunk record

| Field | Type | Written by | Meaning |
|---|---|---|---|
| `id` | string | plan | unique within the task, `[A-Za-z0-9][A-Za-z0-9._-]*` |
| `parent` | string or null | plan | parent chunk id; `null` at depth 1 |
| `stage` | string | plan | workflow stage (`R-01`, `I-01`, ...); inherited from the parent |
| `role` | string | plan | role that executes the chunk; inherited from the parent |
| `depth` | int | plan | `1..3`; must equal the parent depth + 1 |
| `title` | string | plan | one line |
| `state` | enum | script | `pending`, `in_progress`, `done`, `failed`, `blocked` |
| `owns` | string[] | plan | repo-relative paths or globs; exclusive among non-ancestors |
| `inputs` | string[] | plan | pointers (paths, optional line ranges), never pasted content |
| `brief` | string | plan | `docs/chunks/T-XXX/<id>.brief.md` |
| `acceptance` | string[] | plan | checkable bullets |
| `produced` | object[] | script | `{path, sha256}`; required when `state = done` and `owns` is not empty |
| `base_commit` | string | script | short HEAD hash at record creation |
| `attempts` | int | script | incremented by `claim` |
| `usage` | object | script | `{in, out, cr, min}` attributed to the chunk |
| `issues` | string[] | script | `I-YYYYMMDD-HHMMSS` ids; required when `blocked` |
| `notes` | string[] | script | free notes from `complete`/`fail` |
| `started` | string or null | script | ISO timestamp |
| `finished` | string or null | script | ISO timestamp |
| `elapsed_min` | number | script | wall-clock minutes from claim to complete |
| `sessions` | number | script | attributed model sessions (best effort) |
| `compactions` | number | script | context compactions observed |
| `handoff` | string | script | `docs/chunks/T-XXX/<id>.handoff.md` when compaction/transport forced a fresh session |

## State machine

```
pending -> in_progress -> done
                    \--> failed --(claim)--> in_progress
                    \--> blocked --(orchestrator clears)--> pending
```

Only `done` chunks count as coverage. A partially written file makes a chunk
`failed`, never `done`.

## Depth and ownership

- `d1` plans the chunks and may execute the ones that fit the budget.
- `d2` is a same-role child for a chunk that does not fit one session.
- `d3` is a further same-role split; a fourth level is never spawned.
- Children are sequential, one at a time, never parallel.
- `owns` must be disjoint between chunks that are not ancestor/descendant. A
  parent's `owns` is the union of its children's `owns`.
- Every existing file under the card artifacts of that stage must be owned by
  some top-level chunk (`chunk-coverage`), and a chunk may not own a path outside
  those artifacts (`E_OWN_OUTSIDE_CARD`). Card artifacts are usually coarse globs
  (`src/**`), so the check walks the real files instead of comparing globs.
- `owns` must stay inside the role write area (`process.md` section 12) plus
  `docs/chunks/**` and `docs/issues/**`.

## Example

```json
{
  "task": "T-008",
  "version": 1,
  "updated": "2026-11-14T12:00:00",
  "budget": {
    "mode": "authored-delta",
    "profile": "I-01/default",
    "scope_class": "cross-cutting",
    "max_write_kb": 80,
    "max_session_kb": 200,
    "context_stop_pct": 65,
    "max_turns": 40,
    "max_input_tokens": 60000,
    "max_wall_min": 45,
    "per_type": {
      "code": {"max_write_kb": 12, "max_session_kb": 40}
    },
    "exclude": ["**/*.svg", "**/package-lock.json", "**/*.lock"]
  },
  "chunks": [
    {
      "id": "C4a",
      "parent": null,
      "stage": "I-01",
      "role": "coder",
      "depth": 1,
      "title": "API-001 tasks CRUD and RU error envelope",
      "state": "done",
      "owns": ["backend/src/main/java/**/api/**", "backend/src/main/java/**/error/**"],
      "inputs": ["docs/architecture/contracts-ai-task-tracker.md"],
      "brief": "docs/chunks/T-008/C4a.brief.md",
      "acceptance": ["API-001 endpoints match the contract", "RU error envelope in tests"],
      "produced": [
        {"path": "backend/src/main/java/ru/aitracker/api/TaskController.java", "sha256": "0000000000000000000000000000000000000000000000000000000000000000"}
      ],
      "base_commit": "abc1234",
      "attempts": 1,
      "usage": {"in": 32000, "out": 8000, "cr": 240000, "min": 31},
      "issues": [],
      "notes": [],
      "started": "2026-11-14T12:00:00",
      "finished": "2026-11-14T12:31:00"
    }
  ]
}
```

## Brief

`docs/chunks/T-XXX/<id>.brief.md` must let a cold session execute the chunk
without re-reading the corpus:

```markdown
# <id>: <title>

| Field | Value |
|---|---|
| task | T-XXX |
| stage | I-01 |
| role | coder |
| depth | 1 |
| owns | backend/src/main/java/**/api/** |
| budget | authored-delta: 12 KB write, 40 KB session, stop at 65% |

## Goal

One sentence.

## Inputs

- `docs/architecture/contracts-<slug>.md` lines 120-180 (API-001 section)
- `backend/src/main/java/.../Task.java` (existing entity)

## Acceptance

- [ ] bullet
- [ ] bullet

## Stop rule

Stop when authored growth would exceed the per-type `max_write_kb`, when the
context reaches `context_stop_pct`, or when the acceptance list is met. Record
the state with `chunk.ps1 complete` or `chunk.ps1 fail`.

## Return

At most ten lines: produced paths, state, and any issue id. No artifact text.
```
