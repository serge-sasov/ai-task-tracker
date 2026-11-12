# T-003 CP-F: E2E use cases and YAML dataset — final acceptance (A-02 closure)

| Field | Value |
|---|---|
| task | T-003 |
| workflow | feature |
| bug | - |
| checkpoint | CP-F |
| role | analyst |
| stage | A-02 final acceptance |
| artifacts | `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/analysis/e2e-test-data-ai-task-tracker.yaml`, `docs/checkpoints/T-003-CP-2.md` |
| status | approved |
| requested_at | 2026-11-12T11:44:07 |
| reviewed_at | 2026-11-12 |
| reviewer | user |
| decision | approved |
| snapshot_commit | d9ca867 |

## Review scope

Финальная верификация стадии A-02 (closure) — стадия завершена, acceptance подтверждён:

1. **Полнота покрытия.** Все 30 требований `R-001..R-030` из `docs/analysis/requirements-ai-task-tracker.md` покрыты хотя бы одним `UC-E2E-NNN` или явно помечены как `indirect` (24 direct + 6 indirect согласно coverage table).
2. **Use cases.** 8 use cases `UC-E2E-001..UC-E2E-008`, у каждого обязательные поля шаблона: source requirements, actor, priority, preconditions, trigger, main/alternate/error flows, test data ref, expected result, postconditions, cleanup, traceability.
3. **YAML dataset (machine-readable contract).** Схема `harness-e2e/1`, детерминированные данные, без секретов (только плейсхолдеры `${E2E_*}`), 8 записей `use_cases[].id` совпадают с UC-ID из MD 1:1.
4. **Token totals.** Детерминированы и перепроверены: qwen3.8:27b 10750/4590/15340; llama3.1:8b 2100/820/2920; grand total 12850/5410/18260.

### Acceptance points for this checkpoint

- [x] Артефакты A-02 полные: `docs/analysis/e2e-use-cases-ai-task-tracker.md` (40 331 B), `docs/analysis/e2e-test-data-ai-task-tracker.yaml` (28 751 B).
- [x] Все 30 требований R-001..R-030 покрыты (24 direct + 6 indirect) — coverage table в use-cases doc.
- [x] 8 use cases UC-E2E-001..UC-E2E-008 с обязательными полями шаблона (source requirements, actor, priority, preconditions, trigger, flows, test data, expected result, postconditions, cleanup, traceability).
- [x] YAML: схема `harness-e2e/1`, детерминированность, без секретов (только `${E2E_*}`), 8 `use_cases[].id` = UC-ID из MD 1:1.
- [x] Token totals перепроверены: qwen3.8:27b 10750/4590/15340; llama3.1:8b 2100/820/2920; grand total 12850/5410/18260.
- [x] CP-2 approved (`docs/checkpoints/T-003-CP-2.md`, `decision: approved`, 2026-11-12).
- [x] Готовность к T-004 (U-01 UX flows and screens): e2e-контракт (use cases + YAML dataset) является входом для U-01/U-02 и далее I-02/Q-01.

## How to review / edit

- Прочитать: `docs/analysis/e2e-use-cases-ai-task-tracker.md` (UC-E2E-NNN + coverage table), `docs/analysis/e2e-test-data-ai-task-tracker.yaml` (схема, шаги, данные, expected values, cleanup), `docs/checkpoints/T-003-CP-2.md`.
- Можно редактировать артефакты и/или написать замечания в `## User remarks` ниже.
- Known NON-BLOCKING nits (cosmetic anchor drift, не блокируют): MD UC-E2E-001 ссылается на `#fixtures.compose` (фактический якорь YAML: `#environment`); MD UC-E2E-006 ссылается на `#fixtures.token_usage` (фактический якорь YAML: `#fixtures.token_totals`). Указаны как follow-up для стадии ADR/I-01 (write area analyst).
- Решение CP-F: впишите `decision: approved` или `decision: changes_requested` в таблице выше.
- Resume-команда: `/harness-run T-003`.

## After `approved` (per plan)

- T-003 -> `done`: верификация acceptance, финал plan/card/activity (green #90EE90)/SVG, фиксация final commit hash на доске.
- Финальный коммит задачи `T-003: e2e use cases and YAML dataset (A-02) accepted`.
- Разблокируется T-004 (U-01 UX flows and screens) — активация по команде, автоматически не стартует.

## User remarks

- (user fills; may be empty if artifacts were edited directly)

## User edits

- `docs/checkpoints/T-003-CP-F.md` — decision: approved (authoritative user decision for CP-F, given in chat 2026-11-12)

## Role response

- Замечаний нет (User remarks пуст).
- Acceptance-точки CP-F верифицированы по артефактам (покрытие R-NNN, поля шаблона UC, схема/ID YAML, token totals).
- Два косметических дрейфа якорей (UC-E2E-001 `#fixtures.compose`, UC-E2E-006 `#fixtures.token_usage`) остаются NON-BLOCKING; зафиксированы как follow-up для стадии ADR/I-01 (write area analyst — оркестратор артефакты аналитика не редактирует).

## Next action

- accept task — T-003 closed as done (2026-11-12); T-004 (U-01 UX flows and screens) unblocked, activation only by command.
