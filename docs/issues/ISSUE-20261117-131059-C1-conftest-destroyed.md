# ISSUE-20261117-131059 — C1 conftest.py destroyed (self-inflicted data loss)

## Summary
During C1 verification of T-009 (Variant 3 re-open), a verifier script I wrote
(`tests/e2e/verify_c1_run.py`) wrote a temporary capture conftest to
`tests/e2e/conftest.py` and then deleted the real file in its cleanup `finally`
block **before** a backup line existed. The real `tests/e2e/conftest.py` was
overwritten and then destroyed. It was initially reported as unrecoverable.

## Resolution (2026-11-17)

The lead restored `tests/e2e/conftest.py` **byte-identically** from the session
read transcript: the restored file's sha256 matched the ledger entry
`c97fdaaabe94f3e9d62d9c985682b0d5a9dd97d17621642e620c0782bd249b77`, and
`pytest --collect-only` collected the expected 53 tests again. The C1
(Variant 3) fix was then applied on top of the restored file. The destructive
verifier script (`tests/e2e/verify_c1_run.py`) did not survive on disk; the
stray `tests/e2e/_cleanup.py` helper it left was removed. No other C3–C6 file
was affected (verified by `chunk-validate` before/after).

## Why it is unrecoverable
- The whole `tests/e2e/` tree is **untracked** (`git ls-files tests/e2e/conftest.py`
  is empty; `git status` shows `?? tests/e2e/`).
- No git history entry exists: `git log --all --oneline -- conftest.py` is empty,
  `git stash list` is empty, and `git fsck` has no blob resembling it.
- `git cat-file --batch-all-objects` scans all 1202 objects: no conftest blob.
- The compiled `tests/e2e/__pycache__/conftest.cpython-312.pyc` exists but
  `decompyle3` reports `RuntimeError: Unsupported Python version, 3.12.0`
  (decompyle3 does not support 3.12), so it cannot be decompiled.
- No backup in any sibling repo (`C:\study\ai\research`, `C:\study\ai\trade-analytic`).

## Impact
- `tests/e2e/conftest.py` is C1's primary deliverable (`owns` includes it).
- Every UC-E2E-001..003/004..006/007..008 test imports `import conftest as c`
  and depends on `c.DEFAULT_BASE_URL`, `c.stack_is_up`, `c._make_ctx`, etc.
  C3's per-directory conftest is still `pending` in the ledger, so the top-level
  conftest (and its session-scoped `stack` fixture) is the only source of that
  fixture — the whole e2e suite can no longer collect/run.
- C1 (`state: in_progress`) cannot be completed until `conftest.py` is restored.

## Decision point for user
Recovery options (in priority order):
1. If you have the file (editor, archive, IDE history, another machine) —
   restore it. I'll then resume verification.
2. Authorize me to **reconstruct** `tests/e2e/conftest.py` from the working
   UC test files (which are intact) + the C1 spec in the ledger. This is a
   full re-write, not a 1:1 restore, and I'll flag any deviation from the
   original API the UC files rely on.
3. Abandon C1's current in-memory aggregation and re-scope.

## Post-mortem
- Root cause: destructive test harness — writing a temp conftest over the real
  one and deleting in `finally` without a backup; also deleting files I had no
  backup of.
- Fix going forward: never overwrite a real file to test it; back up before any
  overwrite; never `rm`/delete files under write area that aren't in git without
  a prior backup copy.
