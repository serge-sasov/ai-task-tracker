# OpenCode real-data exporter (local Docker deployment)

The e2e stack normally ingests per-session JSON files. To show your **real**
opencode activity, a small exporter reads the opencode SQLite DB
(`opencode.db`) read-only and writes per-session JSON files into a shared
directory; the backend polls and upserts them. It runs as a cron-like loop in
the `exporter` compose service.

## How it works

```
opencode.db  --(read-only)-->  scripts/opencode-export.py  --(JSON)-->  data/opencode-export
                                                                              |
                                                            backend (INGEST_INTERVAL_SECONDS)
                                                                              v
                                                                    tasks / calendar / tokens
```

- Only **top-level sessions** (`session.parent_id IS NULL`) are exported.
- Sessions are re-exported only when `time_updated` changes (state in
  `data/opencode-export/.state`); active sessions update on the next cycle.
- Per session: `session_id`, `title`, `started_at`, `finished_at`,
  `final_status` («Завершена»), up to `EXPORT_MAX_MESSAGES` text parts
  (`messages[]`), and aggregate `usage[]` (model + input/output/cache tokens).
- The DB is mounted **read-only**; nothing is written to `opencode.db`.
- All data stays on this machine.

## Run

```powershell
copy .env.example .env      # once, then set OPENCODE_DB_DIR (and optional tuning)
docker compose -f docker-compose.yml -f docker-compose.deploy.yml up -d
docker logs aitasktracker-exporter
docker compose -f docker-compose.yml -f docker-compose.deploy.yml ps
```

`.env` is loaded automatically from the repository root. `OPENCODE_DB_DIR`
points at the directory that contains `opencode.db`
(default in `.env.example`: `C:/Users/your-user/.local/share/opencode`).

Exporter logs:

```
[exporter] db=/opencode-src/opencode.db export_dir=/export interval=60s
[exporter] sessions=269 exported=269 state=269
```

Verify: `GET http://127.0.0.1:8080/api/tasks?size=1` (total) and open the
calendar UI (`http://127.0.0.1:8081/calendar`). The health `source_status`
becomes `available` after an ingest cycle.

## Cron-like periods

| Env | Default | Meaning |
|---|---|---|
| `EXPORT_INTERVAL_SECONDS` | 60 | How often the exporter re-reads the DB (cron-like loop) |
| `INGEST_INTERVAL_SECONDS` | 60 | How often the backend ingests the JSON files |

The exporter loop is the cron substitute inside the container. To change the
cadence, edit `.env` and restart:

```powershell
docker compose -f docker-compose.yml -f docker-compose.deploy.yml up -d
```

## One-off export (host, no container)

```powershell
$env:OPENCODE_DB="C:/Users/your-user/.local/share/opencode/opencode.db"
$env:EXPORT_DIR="C:/study/ai/research/ai-task-tracker/data/opencode-export"
python scripts/opencode-export.py --once
```

## Tuning / limits

- `EXPORT_LIMIT_SESSIONS` (default 1000) — latest top-level sessions.
- `EXPORT_MAX_PARTS` (400) — parts scanned per session.
- `EXPORT_MAX_MESSAGES` (50) — text steps kept per session.
- `EXPORT_MAX_TEXT` (500) — characters per step.
- Solutions are not extracted from real sessions (the UI shows the empty
  solutions state); structured solutions were only part of the e2e fixtures.
- To go back to the deterministic e2e seed corpus, set
  `OPENCODE_DATA_DIR=./data/e2e-opencode` in `.env` and recreate the backend
  (the Postgres volume keeps previously ingested tasks unless removed).
