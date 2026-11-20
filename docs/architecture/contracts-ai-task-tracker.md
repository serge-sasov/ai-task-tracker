# Contracts: ai-task-tracker (backend ↔ frontend, ingestion, Ollama)

| Field | Value |
|---|---|
| Task | T-006, T-023, chunks R-01b/R-02 |
| Role | architect |
| ADR | `docs/architecture/adr-ai-task-tracker.md` (ADR-0001) |
| Inputs | `docs/analysis/requirements-ai-task-tracker.md`, `docs/analysis/e2e-use-cases-ai-task-tracker.md`, `docs/analysis/e2e-test-data-ai-task-tracker.yaml`, `docs/ux/ui-spec-ai-task-tracker.md` |
| Scope | UC-E2E-001..008, screens S-01..S-06, R-001..R-014, R-020..R-030 |
| Status | proposed |

## 1. Conventions

- Transport: HTTP/1.1, JSON (UTF-8), `Content-Type: application/json; charset=utf-8`.
  Base URL `http://localhost:8080` (env `E2E_BASE_URL`, points to the `frontend` nginx
  container); the frontend serves the SPA at `/` and reverse-proxies `/api/*` to the backend.
- Time: ISO 8601 local wall-clock seconds (`2026-11-10T09:00:00`); `date` params — `YYYY-MM-DD`.
- IDs: `task_id` = the source-provided `task_id` when present (seed/e2e fixtures use
  `TASK-001`..`TASK-005`); otherwise a stable derived `TASK-<short hash of session_id>`.
  Uniqueness enforced. `message_id` opaque string.
- Status enum (RU, canonical): `Завершена | Прервана | Не завершена` (R-006, Q-008).
- All user-visible error texts are Russian (R-030) and returned in a `message` field
  safe for direct UI display; machine codes are stable ASCII (section 5).
- Empty state is a **200 with empty collections**, never 4xx; the UI shows its
  «нет данных» state. Errors are 4xx/5xx with the error envelope (section 5).
- Pagination: `page` (1-based, default 1), `size` (default 50, max 500) — R-024.
- Caching: `Cache-Control: no-store` on all `/api/*` GETs; UI polls per section 3.

## 2. Data model (PostgreSQL 16, `pgvector`)

| Table | Key columns | Notes |
|---|---|---|
| `tasks` | `task_id TEXT PK`, `session_id TEXT UNIQUE`, `name TEXT NOT NULL`, `started_at TIMESTAMPTZ NOT NULL`, `finished_at TIMESTAMPTZ NULL`, `status TEXT NOT NULL CHECK (status IN ('Завершена','Прервана','Не завершена'))`, `source_path TEXT`, `created_at`, `updated_at` | 1 task = 1 opencode session (Q-002, R-002); `task_id` may come from the source (see §3); `duration_s` derived: `EXTRACT(EPOCH FROM (COALESCE(finished_at, last_step_at) - started_at))` (R-009) |
| `steps` | `step_id BIGSERIAL PK`, `task_id TEXT FK→tasks ON DELETE CASCADE`, `message_id TEXT NOT NULL`, `at TIMESTAMPTZ`, `text TEXT`, UNIQUE(`task_id`,`message_id`) | chronological session messages (R-003) |
| `solutions` | `solution_id BIGSERIAL PK`, `task_id TEXT FK ON DELETE CASCADE`, `kind TEXT CHECK (kind IN ('proposed','chosen'))`, `text TEXT NOT NULL`, `source_message_id TEXT NULL` (NULL for structured sources, provenance `extracted_by='structured'`), `extracted_by TEXT NULL` (`structured` or `llm:<model>@<prompt_ver>`), UNIQUE(`task_id`,`kind`,`text`,`source_message_id`) | R-004/R-005; ≤1 `chosen` row per task; `extracted_by` records provenance (Q-008) |
| `token_usage` | `usage_id BIGSERIAL PK`, `task_id TEXT FK ON DELETE CASCADE`, `message_id TEXT NULL`, `model TEXT NOT NULL`, `input_tokens BIGINT NOT NULL DEFAULT 0`, `output_tokens BIGINT NOT NULL DEFAULT 0`, `cache_tokens BIGINT NOT NULL DEFAULT 0`, `scope TEXT CHECK (scope IN ('message','session'))`, UNIQUE(`task_id`,`message_id`,`model`,`scope`) | keyed per message; session-level rows only when no per-message usage (Q-003, R-012); no double counting |
| `tasks.embedding` | `vector(1024) NULL` | task digest vector for AI search (R-010); column on `tasks`, re-embedded on change |
| `ingest_state` | `key TEXT PK` (`last_file_mtime`, `last_sync_at`, `last_file_count`, `parse_errors`), `value TEXT` | ingestion watermark + diagnostics (R-025, R-026) |

Invariants (enforced by constraints/transaction, checked in e2e):
- `finished_at` NULL ⇒ `status = 'Не завершена'`; `status='Завершена'` ⇒ `finished_at` NOT NULL.
- All four data tables delete atomically on `DELETE /api/history` (section 4).
- Unknown/extra source fields are ignored; per-file parse failures logged in `ingest_state.parse_errors`, never fatal (ADR-0001 risks).

## 3. Ingestion contract (opencode files → Postgres)

- Source: JSON files under `OPENCODE_DATA_DIR` (env; default opencode local share dir),
  bind-mounted **read-only** into the backend container (R-027, C-3). File layout: one
  JSON document per opencode session — **per-session files are the unit of ingestion**.
  Input schema (opencode-tolerant, both shapes accepted): `session_id`, optional
  `task_id`, `title`, `started_at`, `finished_at`, `final_status` (maps to `status`),
  `actions:[{at,text}]` (fixture) or `messages:[{message_id,at,text}]` (opencode),
  structured `solutions:{proposed:[string],chosen:string}` (no message ids — `chosen`
  stored with NULL source ref), and `usage` as a list of
  `{model, input_tokens|input, output_tokens|output, cache_tokens|cache}` entries or
  `null`. Field names are matched case-insensitively; unknown fields ignored.
  Mapping (fixture/opencode field → column):

  | Source field | → column |
  |---|---|
  | `task_id` (fixture) | `tasks.task_id` (source-provided wins, else derived) |
  | `session_id` | `tasks.session_id` (natural key) |
  | `title` | `tasks.name` |
  | `started_at` / `finished_at` | `tasks.started_at` / `tasks.finished_at` |
  | `final_status` (fixture) / session end-state (opencode) | `tasks.status` |
  | `actions[{at,text}]` (fixture) | `steps.at`, `steps.text` |
  | `messages[{message_id,at,text}]` (opencode) | `steps.message_id`, `steps.at`, `steps.text` |
  | `solutions.proposed[]` / `solutions.chosen` | `solutions` rows `kind='proposed'/'chosen'`, `source_message_id` NULL |
  | `usage[].{model,input_tokens,output_tokens,cache_tokens}` (or `input/output/cache`) | `token_usage.(model,input_tokens,output_tokens,cache_tokens)` |
  | `usage: null` / absent | no `token_usage` rows (S-05 «нет данных») |
- Cadence: scheduled poll **every 60 s** (env `INGEST_INTERVAL_SECONDS`, default 60),
  guarded by a Redis lock (single worker). R-026: activity visible within ≤ 5 min — met.
- Idempotency (R-025): per-session batch upserts on natural keys
  (`tasks.session_id`, `steps.message_id`, `token_usage.(message_id,model)`,
  `solutions.(session_id,source_ref)`) in one Postgres transaction; re-sync updates,
  never duplicates; failed batch rolls back (no half-records — UC-E2E-007 E3).
- `source_status` per cycle: `available` (files read) / `unavailable` (dir missing or
  unreadable) / `stale` (read but no change for > 3× interval); exposed via
  `/api/health` and the S-01 banner (R-028, UC-E2E-002).
- After `DELETE /api/history` the next poll re-imports from unchanged source files
  (UC-E2E-008 A1) — source files are never modified or deleted by the service (C-3).
- LLM work during ingestion: solution extraction (only when structured fields absent)
  and re-embedding of changed tasks; Ollama unavailable ⇒ skip extraction/embedding,
  log, retry next cycle — ingestion of tasks/steps/tokens never blocked by Ollama (R-028).

## 4. REST API (backend ↔ Vue SPA)

All routes under `/api`; error envelope — section 5; time/IDs — section 1.
Client polling: the SPA re-requests the active section ≤ 5 min and on tab focus;
indicator «обновлено HH:MM:SS» (`aria-live="polite"`) (R-026, R-028).

### 4.1 `GET /api/health` (R-028, UC-E2E-001)

Liveness and readiness in one endpoint. 200:

```json
{ "status": "ok", "checks": { "postgres": "ok", "redis": "ok" },
  "source_status": "available", "ollama_status": "up",
  "last_ingest_at": "2026-11-13T10:00:00" }
```

`source_status` ∈ `available|unavailable|stale`; `checks.postgres`/`checks.redis` ∈
`ok|down`; 503 `SERVICE_UNAVAILABLE` («Сервис недоступен») when a check is down.
E2E: `GET /api/health` → 200 with `$.status == "ok"`, `$.checks.postgres == "ok"`,
`$.checks.redis == "ok"` — matches e2e `$.checks.postgres/$.checks.redis == "ok"`
(UC-E2E-001 step 2, UC-E2E-007 step 1).

### 4.2 `GET /api/tasks` — list (S-01, R-007, R-014, UC-E2E-003)

Query: `from`, `to` (dates, inclusive), `status` (enum of section 1), `page`, `size`.
200:

```json
{ "total": 5, "items": [
  { "task_id": "TASK-001", "name": "Настройка CI для репозитория ai-task-tracker",
    "started_at": "2026-11-10T09:00:00", "finished_at": "2026-11-10T11:30:00",
    "status": "Завершена", "duration_s": 9000, "tokens_total": 12150,
    "models": ["qwen3.8:27b", "llama3.1:8b"] } ] }
```

- `models`: ordered by total tokens (input + output + cache) descending, then by
  model name ascending. An empty array means no usage and the S-01 cell shows
  «Нет данных» (R-014). The field is derived from the same `token_usage` rows as
  `tokens_total`; no schema change is needed.
- Empty: `{"total":0,"items":[]}` → S-01 «нет данных» (UC-E2E-001 F-5).
- Load < 3 s (R-023); list stays fast on the `large` profile (R-024).
- Fixture expectations (UC-E2E-003/007): 5 tasks TASK-001..005, durations
  9000/7200/8100/4200/2400 s (total 30900 s = 8ч 35мин), statuses
  Завершена/Завершена/Прервана/Не завершена/Завершена; counts unchanged after
  restart and re-sync (no duplicates, R-025).

### 4.3 `GET /api/tasks/{id}` — detail (S-02, R-003..R-006, R-009, UC-E2E-003)

200:

```json
{ "task_id": "TASK-001", "name": "…", "started_at": "2026-11-10T09:00:00",
  "finished_at": "2026-11-10T11:30:00", "status": "Завершена", "duration_s": 9000,
  "steps": [ { "message_id": "m1", "at": "2026-11-10T09:05:00",
               "text": "Анализ структуры репозитория и требований к CI" } ],
  "solutions": {
    "proposed": [ { "text": "GitLab CI", "source_message_id": "m2" },
                  { "text": "GitHub Actions", "source_message_id": "m2" } ],
    "chosen": { "text": "GitLab CI", "source_message_id": "m3" } },
  "tokens": { "by_model": [ { "model": "qwen3.8:27b", "input_tokens": 7200,
                  "output_tokens": 3000, "cache_tokens": 0, "total_tokens": 10200 },
                { "model": "llama3.1:8b", "input_tokens": 1400, "output_tokens": 550,
                  "cache_tokens": 0, "total_tokens": 1950 } ],
              "total": { "input_tokens": 8600, "output_tokens": 3550, "total_tokens": 12150 } } }
```

- 404 `NOT_FOUND` «Задача не найдена».
- No solutions: `"solutions": {"proposed": [], "chosen": null}` → S-02
  «Решений не зафиксировано» (TASK-004/005).
- No usage: `"tokens": {"by_model": [], "has_data": false}` → task shown without
  token block (TASK-002; UC-E2E-006 E2).
- Steps — chronological, TASK-001 has exactly 6 (R-003).
- `chosen` is highlighted with the label «выбрано» (not color only, R-030/WCAG).

### 4.4 `POST /api/search` — AI search (S-03, R-010, R-011, UC-E2E-004)

Body: `{ "query": "настройка CI для репозитория", "limit": 5 }` (`limit` default 5,
max 20). Client-side validation: blank/whitespace `query` → field message «Введите запрос»,
request not sent (UC-E2E-004, fixture `ai_search.empty_query`).
200 (answer < 15 s, R-023):

```json
{ "query": "настройка CI для репозитория", "results": [
  { "task_id": "TASK-001", "score": 0.91,
    "snippet": "Настройка CI: выбрано решение GitLab CI (stages lint/test/build)" } ] }
```

- `results` ordered by `score` desc; top-K ≥ 5 when data allows (R-011); each result
  carries a non-empty `snippet` explaining relevance (R-011) — never empty text.
- Honest empty: unrelated query (fixture «рецепт борща») → 200 `{"results": []}` → S-03
  «Ничего не найдено. Попробуйте изменить запрос.» — no fabricated matches (UC-E2E-004 step 5).
- Ollama down/timeout → 503 `AI_SEARCH_UNAVAILABLE` «AI-поиск недоступен: LLM не отвечает.
  Попробуйте позже.» (UC-E2E-004; list/calendar/tokens keep working — R-028).
- E2E: «настройка CI для репозитория» ⇒ TASK-001 in top-K; «авторизация и токены
  доступа» ⇒ TASK-002; only localhost Ollama calls in logs (R-027).

### 4.5 `GET /api/calendar` — calendar (S-04, R-008, R-009, UC-E2E-005)

Query: `view` ∈ `day|week|month|year`, `date` = `YYYY-MM-DD` (anchor: that day /
Mon-anchored week containing it / that month / that year). 200:

```json
{ "view": "day", "date": "2026-11-11", "days": [
  { "date": "2026-11-11", "tasks": [
    { "task_id": "TASK-003", "started_at": "2026-11-11T08:30:00",
      "finished_at": "2026-11-11T10:45:00", "duration_s": 8100 },
    { "task_id": "TASK-004", "started_at": "2026-11-11T11:00:00",
      "finished_at": "2026-11-11T12:10:00", "duration_s": 4200 },
    { "task_id": "TASK-005", "started_at": "2026-11-11T09:00:00",
      "finished_at": "2026-11-11T09:40:00", "duration_s": 2400 } ] } ] }
```

- `week` returns 7 day cells (Mon–Sun) with `outside: true` on adjacent days (dimmed,
  not clickable); `month` — full Mon–Sun grid with `outside` cells; `year` — 12 month
  cells each `{date, task_count, total_duration_s, tokens_total}` or `null` values →
  «Нет данных» (e.g. «5 задач · 8ч 35мин · 18 260 токенов»); clicking a month with data
  switches the SPA to `view=month` for that month (Q-UX-004).
- A task spanning two days (profile `large`) appears on **both** days as an interval
  bar — no duplicate records (UC-E2E-005 A1).
- Empty period → 200 with empty `days`/cells → «Нет задач в выбранном периоде.»
  (R-008); invalid/absent date → 400 `INVALID_PERIOD` «Некорректная дата».
- Fixture: week 09–15.11 → 10.11: TASK-001, TASK-002; 11.11: TASK-003..005;
  empty days 09.11, 12–15.11 (UC-E2E-005).

### 4.6 `GET /api/tokens` — token statistics (S-05, R-012, R-013, UC-E2E-006)

Query: `period` = `all` (default) | a `date` (`YYYY-MM-DD`) | `from`+`to` (inclusive
range, `to` ≥ `from` else 400 `INVALID_PERIOD` «Некорректный период»). 200:

```json
{ "period": "all",
  "by_model": [
    { "model": "qwen3.8:27b", "input_tokens": 10750, "output_tokens": 4590,
      "cache_tokens": 0, "total_tokens": 15340 },
    { "model": "llama3.1:8b", "input_tokens": 2100, "output_tokens": 820,
      "cache_tokens": 0, "total_tokens": 2920 } ],
  "total": { "input_tokens": 12850, "output_tokens": 5410, "total_tokens": 18260 },
  "has_data": true }
```

- Totals are the exact sum of seed `usage` (R-012/R-013, R-026): `all` → 18260
  (qwen3.8:27b 10750/4590; llama3.1:8b 2100/820); 10.11 → 10200/1950
  (TASK-001..002, SES-B has no usage); 11.11 → 5140/970 (TASK-003..005);
  range 10.11–11.11 → equals `all` (UC-E2E-006).
- Model not used in the period: row with zeros or omitted with `has_data` per-model
  semantics → S-05 «нет данных за период», never a broken widget (UC-E2E-006 A1).
- Empty period → 200 `{"by_model":[],"total":{…zeros…},"has_data":false}` →
  S-05 «Нет данных за выбранный период.»
- Load < 3 s (R-023); values survive restart + re-sync unchanged (UC-E2E-007).

### 4.7 `DELETE /api/history` — manual deletion (S-06, R-029, UC-E2E-008)

Body (confirmation is required, R-029): `{ "confirm": true }`; any other body or none →
400 `CONFIRMATION_REQUIRED` «Требуется подтверждение удаления».
200 (or 204): `{"deleted": true}` → S-06 «История удалена.». Semantics:
- atomic single transaction over `tasks`, `steps`, `solutions`, `token_usage`
  (+ `embedding`); **no partial deletion state** (UC-E2E-008 E1);
- source files in `OPENCODE_DATA_DIR` are **not touched** (C-3) — the next poll may
  re-import the same 5 tasks (UC-E2E-008 A1);
- empty DB → 200 `{"deleted": false}` «Данных для удаления нет» (no-op, not an error,
  UC-E2E-008 E2); client cancel (A2) sends no request — data unchanged.
- After success: S-01 «нет данных», S-05 zeros/empty, S-04 empty periods, S-03
  «Ничего не найдено» — all explicit states (R-028, R-030).
- 500 `INTERNAL_ERROR` «Не удалось удалить историю. Повторите попытку.» only on DB
  failure; the transaction guarantees consistency.

## 5. Error model (R-028, R-030)

Envelope on every non-2xx:

```json
{ "code": "AI_SEARCH_UNAVAILABLE",
  "message": "AI-поиск недоступен: LLM не отвечает. Попробуйте позже." }
```

| HTTP | `code` | When | RU `message` (canonical) |
|---|---|---|---|
| 400 | `INVALID_PERIOD` | bad/out-of-range `from`/`to`/`date` | «Некорректный период. Проверьте даты.» |
| 400 | `CONFIRMATION_REQUIRED` | `DELETE /api/history` without `confirm:true` | «Требуется подтверждение удаления.» |
| 404 | `NOT_FOUND` | unknown `task_id` | «Задача не найдена.» |
| 500 | `INTERNAL_ERROR` | unexpected DB/internal failure | «Внутренняя ошибка. Повторите попытку.» |
| 503 | `AI_SEARCH_UNAVAILABLE` | Ollama down/timeout on `POST /api/search` | «AI-поиск недоступен: LLM не отвечает. Попробуйте позже.» |
| 503 | `SERVICE_UNAVAILABLE` | DB unreachable on `/api/health` | «Сервис недоступен.» |

Rules: `code` is stable ASCII for the e2e runner; `message` is Russian and shown
verbatim in the UI (each error state has a «Повторить» action, S-01..S-05). Empty
results are 200, never errors. No stack traces or internals in responses; structured
server logs carry ids only (R-027). UI source-unavailable is the S-01 banner
«Источник данных (opencode) недоступен» (driven by `source_status`, UC-E2E-002).

## 6. Ollama contract (localhost only, R-027)

- Base URL: `http://localhost:11434` (env `OLLAMA_BASE_URL`); reachable from the
  backend container via the compose network. No other external endpoints (R-027,
  UC-E2E-001 step 5). All calls synchronous, bounded **15 s** per call (R-023).
- Embeddings — `POST /api/embed` (current Ollama API)
  `{"model":"<embedding-model>","input":"<task digest or query>"}` →
  `{"embeddings":[[…1024 floats…]]}` (dimension fixed at index build time; stored in
  `tasks.embedding`). Used for: task digest = name + steps text + solutions digest
  (re-embedded only when the task changes — ADR-0001 risks); user query (per request).
- Ranking/snippets — `POST /api/chat`
  `{"model":"<chat-model>","messages":[{"role":"system","content":"<ranker prompt v>"}],"stream":false}`
  → ranked `task_id` list with per-result RU `snippet`. Used for: AI search
  re-ranking + snippet generation (R-010, R-011, S-03) and solution extraction when
  structured fields are absent (R-004/R-005, Q-008).
- Degradation (R-028): connection error/timeout ⇒ `POST /api/search` → 503
  `AI_SEARCH_UNAVAILABLE`; ingestion skips extraction/embedding and retries next
  cycle — tasks/steps/tokens ingestion is never blocked by Ollama.
- Telemetry: logs record model name, latency, status code only — no session content
  (R-027). `SEARCH_TOP_K` default 5 (env).

## 7. UI mapping and traceability

Screens → endpoints (UX spec `docs/ux/ui-spec-ai-task-tracker.md`, S-01..S-06):

| Screen | Endpoint(s) | States (RU copy from ui-spec §5–6) |
|---|---|---|
| S-01 «Задачи» | `GET /api/tasks`, `GET /api/health` | loading (skeleton) / empty «Нет данных» / success table / error «Повторить» / banner «Источник данных (opencode) недоступен» |
| S-02 «Детали задачи» | `GET /api/tasks/{id}` | success / «Решений не зафиксировано» / error + «Назад к списку» |
| S-03 «Поиск» | `POST /api/search` | validation «Введите запрос» / loading «Ищем по задачам…» / success top-K=5 / «Ничего не найдено. Попробуйте изменить запрос.» / «AI-поиск недоступен: LLM не отвечает. Попробуйте позже.» |
| S-04 «Календарь» | `GET /api/calendar` | default «Неделя» / День/Месяц (grid Пн–Вс)/Год (12 months + stats) / «Нет задач в выбранном периоде.» / «Не удалось загрузить календарь» + «Повторить» |
| S-05 «Токены» | `GET /api/tokens` | «Всё время» / day / range / «Нет данных за выбранный период.» / «Повторить» |
| S-06 «Управление данными» | `DELETE /api/history` | warning + dialog / «История удалена.» / no-op «Данных для удаления нет» / «Не удалось удалить историю. Повторите попытку.» |

Use case → contract coverage (UC-E2E-001..008, `e2e-use-cases-ai-task-tracker.md`):

| UC | Contract anchors |
|---|---|
| UC-E2E-001 stack up + initial empty | §4.1, §4.2/4.5/4.6 empty states; §1 empty=200 |
| UC-E2E-002 source availability | §3 `source_status`; §5 S-01 banner |
| UC-E2E-003 list + detail | §4.2, §4.3 (6 steps TASK-001; «Решений не зафиксировано» TASK-004/005) |
| UC-E2E-004 AI search | §4.4 (fixtures `ai_search`), §6, §5 `AI_SEARCH_UNAVAILABLE` |
| UC-E2E-005 calendar | §4.5 (fixtures `calendar`), A1 interval task |
| UC-E2E-006 token stats | §4.6 (fixtures `token_totals` 18260), A1 model zeros |
| UC-E2E-007 restart + idempotency | §3 idempotency, §4.1, §2 invariants, §4.2/4.6 fixture invariance |
| UC-E2E-008 deletion | §4.7, A1 re-import (C-3), E2 no-op |

Requirement traceability (R-001..R-014, R-020..R-030):

| Req | Covered by |
|---|---|
| R-001 | §3 (file ingestion), ADR §2 |
| R-002 | §2 `tasks` (session link), §4.2 |
| R-003 | §2 `steps`, §4.3 |
| R-004 | §2 `solutions` (proposed), §4.3, §6 extraction |
| R-005 | §2 `solutions` (chosen), §4.3 «выбрано» |
| R-006 | §2 status enum, §1, §4.2/4.3 |
| R-007 | §4.2, §4.3, S-01/S-02 mapping |
| R-008 | §4.5, S-04 mapping |
| R-009 | §2 duration invariant, §4.2/4.5 `duration_s` |
| R-010 | §4.4, §6, ADR §5 |
| R-011 | §4.4 top-K/snippet |
| R-012 | §2 `token_usage`, §4.6 |
| R-013 | §4.6, S-05 mapping |
| R-014 | §4.2 `models`, S-01/S-02 mapping, UC-E2E-003 |
| R-015 | §4.2 `models` (unchanged), §9 S-04 model-cell boundary + handoff, UC-E2E-005 |
| R-020 | ADR §1 (Java 21/Spring Boot 3, Postgres 16, Redis 7) |
| R-021 | ADR §1, §7 (Vue 3 SPA) |
| R-022 | ADR §1, §8 e2e `docker compose up` (one-command local stack) |
| R-023 | §1/§4.2 (<3 s), §4.4/§6 (15 s) |
| R-024 | §1 pagination, ADR §5, §3 `large` profile |
| R-025 | §3 idempotency, §2 invariants, §4.7 atomicity |
| R-026 | §3 cadence 60 s, §1 polling, §4.1 `last_ingest_at` |
| R-027 | §3 read-only, §5 log rule, §6 localhost-only |
| R-028 | §4.1, §5, §3/§6 degradation |
| R-029 | §4.7 |
| R-030 | §1 (RU messages), §5, §7 RU copy |

## 8. E2E environment and assumptions

- Environment: `docker compose up -d` on a clean host (Windows/PowerShell); 4-container
  topology — `frontend` (nginx) serves the built Vue SPA on host `localhost:8080` and
  reverse-proxies `/api/*` to `backend:8080`; `backend`, `postgres`, `redis` are internal;
  exactly these four services must be Up/healthy (UC-E2E-001 S1). `E2E_BASE_URL` points to
  the frontend. Ports bound to `127.0.0.1` only (R-022, R-027). Seed files pre-placed in
  `E2E_OPPENCODE_DATA_DIR`; `E2E_BASE_URL`, `E2E_OLLAMA_URL` set; timeouts: step 30 s,
  sync wait ≤ 300 s (R-026).
  Full e2e contract: `docs/analysis/e2e-test-data-ai-task-tracker.yaml`
  (`harness-e2e/1`, deterministic, no secrets).
- Fixtures consumed by the API: `fixtures.sessions` (SES-A..E → TASK-001..005),
  `fixtures.token_totals` (18260 total; per-day 10.11 / 11.11), `fixtures.calendar`
  (week/day/month expectations), `fixtures.ai_search` (3 semantic queries,
  `unrelated_query`, `empty_query`, `top_k_default: 5`). Profile `large` (R-024):
  ≥ 12 months, ≥ 50k events, generated by the runner with seed `20261111` — lists and
  search stay within `performance_targets`.
- Assumptions: (1) opencode JSON field names may evolve — parser is version-tolerant,
  unknown fields ignored, per-file errors logged (ADR-0001 risks); (2) Ollama models
  for embedding/chat are operator-configured — the contract fixes the API shape and
  1024-dim vectors, not the model ids; (3) single user, no auth (Q-004), Russian-only
  UI (Q-006); (4) durations computed from timestamps, not summed step durations
  (Q-002); (5) token totals from opencode `usage` fields only — never estimated (Q-003).
- Open items carried to QA/I-02 (no contract change): e2e test file names follow the
  `test_ids` recommended in the use-case traceability rows; no waiver needed since all
  UC-E2E-001..008 have web UI (ui-spec §3.1).

## 9. S-04 model cell: module boundary and handoff (R-015, T-035)

### 9.1 Module boundary (who owns what)

Requirement R-015 (S-04 day view `/calendar/:date`) is **UI-only**: the API contract is
**unchanged** — no new endpoint, no schema change. `GET /api/tasks` (API-001, §4.2) already
returns the ordered `models` array (R-014); the day view consumes the same field.

| Element | ID | Owns | Does not own |
|---|---|---|---|
| `ModelCell.vue` (shared model-cell element) | CLS-005 (MOD-002 `web`) | the `models: string[]` → display mapping: primary model (first element), visible `+N` when > 1, full ordered list in `title` and `aria-label`, «Нет данных» when empty; design tokens (ellipsis, full value in tooltip) per ui-spec §3.3 | S-01/S-04 row and table layout, task/calendar loading, API aggregation, ordering policy (consumes API-001 order, R-014) |
| `DashboardView.vue` (S-01) | CLS-003 (MOD-002) | task-table layout, status/duration columns, navigation to S-02, search box | model-cell projection (delegates to CLS-005), API calls, ingestion |
| `CalendarDayView.vue` (S-04) | MOD-002 (view layout; distinct from CLS-003) | day-view table layout: «Модель» column immediately after «Задача» (existing «Начало/Окончание/Продолжительность/Статус» columns preserved); hosts `ModelCell` in the «Модель» column | model-cell projection (delegates to CLS-005), calendar aggregation (API-002), ingestion |

Exclusive per level: exactly one class (CLS-005) owns the model-cell display rules; the
two views own only their own layouts. No two classes of the same level own the same
function (see `architecture-ai-task-tracker.md` §Responsibility zones, Classes).

Display contract (identical in S-01 and S-04, ui-spec §3.2–3.3):

| `models` | Cell shows | `title` / `aria-label` |
|---|---|---|
| `[]` | «Нет данных» | «Модель: Нет данных» |
| `[m1]` | `m1` | «Модель: m1» |
| `[m1, m2, …]` | `m1` + `+N` badge | «Модель: m1, m2, …» (API-001 order: total tokens desc, model name asc) |

### 9.2 Module handoff (coder, T-035 R-02)

- **Shared element to create:** `src/web/src/components/ModelCell.vue` (CLS-005) — prop
  `models: string[]` (optional, default `[]`); renders primary + `+N`, sets `title` and
  `aria-label` to the full ordered list, renders «Нет данных» when empty.
- **Files to touch:** `src/web/src/views/CalendarDayView.vue` (add the «Модель» column
  immediately after «Задача», render `ModelCell :models="task.models"` per row);
  `src/web/src/views/DashboardView.vue` (S-01: replace the inline
  `modelLabel`/`modelPrimary`/`modelMoreCount` cell with `ModelCell` — visible and
  accessible behavior must stay identical); `src/web/src/api/types.ts` —
  `TaskListItem.models?: string[]` is already typed (verify only, no change expected).
  No new fetch: `CalendarDayView` already loads its rows via API-001
  `tasksApi.list({from,to})`, whose items already carry `models` (§4.2); API-002
  (`GET /api/calendar`, §4.5) is unchanged by this decision.
- **Test expectations (e2e, UC-E2E-005):** `/calendar/:date` — «Модель» column immediately
  after «Задача»; multi-model task (TASK-001: 2 models) → first model + `+1`, full list in
  `title`/`aria-label` in API order; no-usage task → «Нет данных»; suggested test id
  `e2e/test_calendar.py::test_day_view_model_column`.
- **S-01 must remain green (R-014):** existing S-01 cell tests (primary + `+N`, tooltip,
  «Нет данных», UC-E2E-003) pass unchanged after the refactor to the shared element.
- **Do not change:** backend, OpenAPI specs, `token_usage` schema, API-001/API-002 shapes.
