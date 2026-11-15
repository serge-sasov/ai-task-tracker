# ISSUE-20261115-230827: Get-AllFiles exclude regex dead code (harness-lib.ps1:46)

| Field | Value |
|---|---|
| id | ISSUE-20261115-230827 |
| status | open |
| severity | major |
| type | tool |
| detected_at | 2026-11-15T23:08:27.5141336+03:00 |
| source | T-008 CP-F preview (no-todo FAIL) |
| role | orchestrator |
| location | harness\scripts\harness-lib.ps1:46 (Get-AllFiles) |
| environment | OS: Microsoft Windows NT 10.0.26300.0; cwd: C:\study\ai\research\ai-task-tracker |

## Symptoms

T-008 CP-F preview no-todo FAIL: backend/target/ai-task-tracker-backend-0.1.0.jar (4 hits) + src/web/node_modules/@babel/parser/lib/index.js.map (+331 more) were scanned although node_modules is in the exclude list. Repro: temp dir with node_modules/pkg, target, dist, src — Get-AllFiles returns ALL 4 files (exclusion is 100% dead).

## Reproduction

1. Create a temp dir with subdirs `node_modules\pkg`, `target`, `dist`, `src`, one file each.
2. `. harness/scripts/harness-lib.ps1` (as any harness script does), then `Get-AllFiles -Root <dir>`.
3. All four files are returned, including `node_modules/pkg/index.js`, `target/foo.js`, `dist/bar.js` — the exclude list never fires.

## Expected / actual behavior

- Expected: `node_modules/`, `target/`, `dist/` files excluded; `src/main.js` returned.
- Actual: all 4 files returned (exclusion 100% dead), so `no-todo`/`no-secrets` pattern checks scan build output and `node_modules` (see T-008 CP-F preview).

## Cause

The regex at harness-lib.ps1:46 uses backslash anchors (`\\(…)(\\|$)`) but line 49 normalizes relative paths to forward slashes (`Replace('\', '/')`) and line 50 matches against the normalized path, so the backslashes never match: the entire exclude list (`.git`, `node_modules`, `bin`, `obj`, `__pycache__`, `.harness-backup`) is dead code. Additionally `target` and `dist` are not in the list at all.

## Solution / workaround

Replace line 46 with:

```powershell
$exclude = '(^|/)(\.git|node_modules|bin|obj|__pycache__|target|dist|\.harness-backup[^/]*)(/|$)'
```

Forward slashes, `(^|/)` segment boundaries, `target`/`dist` added. Pattern sanity check (live, PowerShell 5.1): `node_modules/pkg/index.js` → excluded, `target/foo.js` → excluded, `dist/bar.js` → excluded, `src/main.js` → kept, `mytarget/foo.js` → kept, `targeted/foo.js` → kept (no over-exclusion).

## Verification

PENDING (patch not yet applied — edit permission on harness/scripts is denied for this session): re-run the Get-AllFiles repro (expect only src/main.js), verify-artifacts.ps1 -SelfTest, and re-evaluate the T-008 CP-F preview no-todo check.

## Prevention / follow-up

chunk.ps1:158 already duplicates its own correct forward-slash pattern — centralize the exclude list in the lib and add a Get-AllFiles exclusion self-test case to verify-artifacts.ps1 -SelfTest.

## Links

docs/tasks/T-008-implementation-tests.md; docs/checkpoints/_preview-T-008-CP-F.json; harness/scripts/chunk.ps1:158

## Timeline

- 2026-11-15T23:08:27.5141336+03:00: detected
- 2026-11-15 (orchestrator): reproduced dead exclusion, root cause confirmed (backslash anchors vs normalized forward-slash paths), solution defined and pattern-verified; application of the one-line patch is pending — this session has no edit permission on `harness/scripts/**` (permission rule `edit: * deny`, only `docs/**`/`README.md`/`.gitignore` allowed).
