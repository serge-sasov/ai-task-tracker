# T-018: A-01 Requirements baseline for the model column

| Field | Value |
|---|---|
| id | T-018 |
| title | A-01 Requirements baseline: S-01 model column |
| workflow | feature |
| stage | A-01 |
| role | analyst |
| status | in_progress |
| hold | false |
| depends_on | T-017 |
| inputs | `docs/plan.md`, `docs/tasks/T-017-task-list-model-planning.md`, `harness/workflows/feature.md`, `docs/analysis/requirements-ai-task-tracker.md`, `docs/analysis/glossary.md` |
| artifacts | `docs/analysis/requirements-ai-task-tracker.md`, `docs/analysis/glossary.md` |
| e2e | required - R-014 is user-visible in S-01 and observable through `GET /api/tasks`; A-02 will extend UC-E2E-003 and the YAML dataset |
| ux | required - S-01 gets a visible "Модель" column, `+N`, tooltip/aria full list and "Нет данных"; U-01/U-02 are in the graph |
| chunks | `docs/chunks/T-018.json` (C1: requirements and glossary) |
| budget | max_write 32 KB / session 64 KB; one C1 chunk |
| base_commit | 8fab879 |
| usage | n/a |
| issues | - |
| commit | - |

## Acceptance

- [x] `R-014` is added as an atomic Must requirement with description, source, acceptance and dependencies.
- [x] `R-014` traces to source line ИТ:4 (3) and is consistent with R-012/R-013; no duplicate requirement is introduced.
- [x] The glossary defines the primary model, the ordered model list, the `+N` indicator and the no-usage state, including the Russian UI labels "Модель" and "Нет данных".
- [x] No new blocking question is opened; assumptions Q-006 (Russian UI) and the T-017 contract remain unchanged.
- [x] T-017 decisions are preserved: `models: string[]`, total-token ordering with name tie-break, empty array for no usage, no database migration, D-01 waived.
- [x] `CP-1` (requirements baseline) and `CP-F` (final acceptance) are declared.
- [x] Chunk C1 is `done` and the produced file hashes are recorded in `docs/chunks/T-018.json`.

## Checkpoints

| ID | Stage | Artifacts | Checks | Status | Review file |
|---|---|---|---|---|---|
| CP-1 | A-01 requirements baseline | `docs/analysis/requirements-ai-task-tracker.md`, `docs/analysis/glossary.md` | 8P/0F/0M | approved | docs/checkpoints/T-018-CP-1.md |
| CP-F | final acceptance | all task artifacts | - | pending | `docs/checkpoints/T-018-CP-F.md` |

Statuses: `pending`, `awaiting_review`, `approved`, `changes_requested`.

## Requirement draft

### R-014. Модель обработки в списке задач - Must

- **Описание**: Список задач S-01 показывает, какая LLM-модель обрабатывала каждую задачу.
  Если у задачи есть usage нескольких моделей, отображается основная модель (максимальная сумма
  input + output + cache tokens) и видимый индикатор `+N`; полный упорядоченный список доступен
  в tooltip и в доступном имени (`aria-label`). Если usage нет, отображается «Нет данных».
- **Источник**: ИТ:4 (3) (токены в разрезе моделей) + решение пользователя на P-00.
- **Акцептанс**:
  - для задачи с usage нескольких моделей API `GET /api/tasks` возвращает `models` в порядке
    total tokens desc с tie-break по имени модели; UI показывает первую модель и `+N`;
  - tooltip и `aria-label` содержат полный список моделей в том же порядке;
  - для задачи без usage API возвращает пустой массив, UI показывает «Нет данных»;
  - существующие поля ответа и поведение остальных экранов не меняются; миграция БД не требуется.
- **Зависит от**: R-012 (учёт токенов), R-013 (просмотр статистики), R-007 (список задач).
- **Связанные артефакты**: `docs/tasks/T-017-task-list-model-planning.md`, A-02 UC-E2E-003,
  `docs/architecture/contracts-ai-task-tracker.md`, `docs/ux/ui-spec-ai-task-tracker.md`.

## Chunk plan

| Chunk | Stage | Role | Title | Owns | State |
|---|---|---|---|---|---|
| C1 | A-01 | analyst | Requirements and glossary for the model column | `docs/analysis/requirements-*.md`, `docs/analysis/glossary.md` | done |

## Log

- 2026-11-18: T-018 started on base `8fab879`; T-017 CP-F is approved. Requirement R-014 and glossary terms added; traceability and summary updated. C1 recorded as done.
- 2026-11-18: CP-1 approved (A-01); checks 8 PASS/0 FAIL/0 MANUAL/0 UNCHECKED; usage n/a; reviewer auto (P2)
