-- ADR-0001 §3 / contracts §2: query indexes (R-023 / R-024).

CREATE INDEX idx_steps_task         ON steps (task_id);
CREATE INDEX idx_steps_task_message ON steps (task_id, message_id);

CREATE INDEX idx_solutions_task     ON solutions (task_id);

CREATE INDEX idx_token_usage_task   ON token_usage (task_id);

CREATE INDEX idx_tasks_started_at   ON tasks (started_at);
