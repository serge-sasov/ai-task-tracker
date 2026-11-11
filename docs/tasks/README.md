# Tasks

Task cards are created from `harness/templates/task-card-feature.md` or
`harness/templates/task-card-bugfix.md`; `task-card.md` is a deprecation pointer.
File name: `T-XXX-<slug>.md`. Status and log live in the card.
Cards declare `workflow`, `stage`, `inputs`, e2e/UX or bug/severity applicability,
and at least one checkpoint; `CP-F` is mandatory.
Product bugs live in `docs/bugs/`; harness problems live in `docs/issues/`.
Checkpoint reviews are recorded in `docs/checkpoints/T-XXX-CP-N.md` from `harness/templates/checkpoint.md`.
