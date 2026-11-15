// DTOs derived directly from the OpenAPI specs API-001..API-004.
// Field names are copied verbatim from the specs — do not invent fields.

// --- API-001 shared / errors -------------------------------------------------
export interface ApiErrorEnvelope {
  code: string;
  message: string;
}

export type TaskStatus = 'Завершена' | 'Прервана' | 'Не завершена';

// --- API-001: Tasks and history ---------------------------------------------
export interface TaskListItem {
  task_id: string;
  name: string;
  started_at: string;
  finished_at: string | null;
  status: TaskStatus;
  duration_s: number;
  tokens_total: number | null;
}

export interface TaskListResponse {
  total: number;
  items: TaskListItem[];
}

export interface Solution {
  text: string;
  source_message_id: string | null;
}

export interface TokenByModel {
  model: string;
  input_tokens: number;
  output_tokens: number;
  total_tokens: number;
}

export interface TaskDetailResponse {
  task_id: string;
  name: string;
  started_at: string;
  finished_at: string | null;
  status: TaskStatus;
  duration_s: number;
  steps: Step[];
  solutions: TaskSolutions;
  tokens: TaskTokens;
}

export interface Step {
  message_id: string;
  at: string;
  text: string;
}

export interface TaskSolutions {
  proposed: Solution[];
  chosen: string | null;
}

export interface TaskTokens {
  by_model: TokenByModel[];
  total: {
    input_tokens: number;
    output_tokens: number;
    total_tokens: number;
  };
}

export interface DeleteHistoryResponse {
  deleted: boolean;
}

export interface TasksQuery {
  from?: string;
  to?: string;
  status?: TaskStatus;
  page?: number;
  size?: number;
}

// --- API-002: Analytics (calendar + tokens) ---------------------------------
export interface CalendarTask {
  task_id: string;
  started_at: string;
  finished_at: string | null;
  duration_s: number;
}

export interface CalendarDay {
  date: string;
  outside: boolean;
  tasks: CalendarTask[];
}

export interface MonthCell {
  date: string;
  task_count: number | null;
  total_duration_s: number | null;
  tokens_total: number | null;
}

export type CalendarView = 'day' | 'week' | 'month' | 'year';

export interface CalendarResponse {
  view: CalendarView;
  date: string;
  days: CalendarDay[];
  months: MonthCell[];
}

export interface TokenStatsTotal {
  input_tokens: number;
  output_tokens: number;
  total_tokens: number;
}

export interface TokenStatsResponse {
  period: string;
  by_model: TokenByModel[];
  total: TokenStatsTotal;
  has_data: boolean;
}

// --- API-003: AI semantic search --------------------------------------------
export interface SearchHit {
  task_id: string;
  kind: 'step' | 'solution';
  text: string;
  score: number;
}

export interface SearchResponse {
  total: number;
  results: SearchHit[];
}

export interface SearchRequest {
  query: string;
  limit?: number;
  task_id?: string | null;
}

// --- API-004: Health --------------------------------------------------------
export type HealthStatus = 'ok' | 'degraded';
export type ServiceState = 'up' | 'down';

export interface HealthResponse {
  status: HealthStatus;
  backend: ServiceState;
  database: ServiceState;
  ollama: ServiceState;
}
