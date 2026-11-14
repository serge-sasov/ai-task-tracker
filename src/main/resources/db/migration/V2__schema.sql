-- ADR-0001 §3 / contracts §2: core data-model tables.

CREATE TABLE tasks (
    task_id    TEXT PRIMARY KEY,
    session_id TEXT NOT NULL UNIQUE,
    name       TEXT NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    finished_at TIMESTAMPTZ,
    status     TEXT NOT NULL CHECK (status IN ('Завершена','Прервана','Не завершена')),
    source_path TEXT,
    embedding  vector(1024),
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ
);

CREATE TABLE steps (
    step_id     BIGSERIAL PRIMARY KEY,
    task_id     TEXT NOT NULL REFERENCES tasks (task_id) ON DELETE CASCADE,
    message_id  TEXT NOT NULL,
    at          TIMESTAMPTZ,
    text        TEXT,
    UNIQUE (task_id, message_id)
);

CREATE TABLE solutions (
    solution_id       BIGSERIAL PRIMARY KEY,
    task_id           TEXT NOT NULL REFERENCES tasks (task_id) ON DELETE CASCADE,
    kind              TEXT NOT NULL CHECK (kind IN ('proposed','chosen')),
    text              TEXT NOT NULL,
    source_message_id TEXT,
    extracted_by      TEXT,
    UNIQUE (task_id, kind, text, source_message_id)
);

CREATE TABLE token_usage (
    usage_id        BIGSERIAL PRIMARY KEY,
    task_id         TEXT NOT NULL REFERENCES tasks (task_id) ON DELETE CASCADE,
    message_id      TEXT,
    model           TEXT NOT NULL,
    input_tokens    BIGINT NOT NULL DEFAULT 0,
    output_tokens   BIGINT NOT NULL DEFAULT 0,
    cache_tokens    BIGINT NOT NULL DEFAULT 0,
    scope           TEXT NOT NULL CHECK (scope IN ('message','session')),
    UNIQUE (task_id, message_id, model, scope)
);

-- ADR-0001 §2 / R-025: ingestion watermark + diagnostics key/value store.
CREATE TABLE ingest_state (
    key   TEXT PRIMARY KEY,
    value TEXT
);
