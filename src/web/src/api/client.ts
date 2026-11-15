// Typed fetch-based API client covering API-001..API-004.
//
// A single ApiError is thrown for any non-2xx response; it carries the RU
// error envelope { code, message } so the UI renders one consistent error.
import type {
  ApiErrorEnvelope,
  CalendarResponse,
  CalendarView,
  DeleteHistoryResponse,
  HealthResponse,
  SearchRequest,
  SearchResponse,
  TaskDetailResponse,
  TaskListResponse,
  TokenStatsResponse,
} from './types';

export class ApiError extends Error {
  code: string;
  status: number;

  constructor(status: number, code: string, message: string) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
  }
}

const BASE = '';

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(BASE + path, {
    ...init,
    headers: {
      Accept: 'application/json',
      ...(init?.headers ?? {}),
    },
  });

  const raw = await res.text();
  let body: ApiErrorEnvelope = { code: '', message: '' };
  try {
    if (raw) body = JSON.parse(raw) as ApiErrorEnvelope;
  } catch {
    // ignore malformed body for non-2xx path; handled below
  }

  if (res.ok) {
    return body as unknown as T;
  }

  const code =
    body && 'code' in body ? String(body.code) : String(res.status);
  const message =
    body && 'message' in body
      ? String(body.message)
      : `Ошибка сервера (${res.status})`;

  throw new ApiError(res.status, code, message);
}

function qs(
  params: Record<string, string | number | boolean | undefined>,
): string {
  const search = new URLSearchParams();
  for (const key of Object.keys(params)) {
    const value = params[key];
    if (value !== undefined) search.append(key, String(value));
  }
  const s = search.toString();
  return s ? `?${s}` : '';
}

// --- API-001: Tasks and history ---------------------------------------------
export const tasksApi = {
  list(params: {
    from?: string;
    to?: string;
    status?: string;
    page?: number;
    size?: number;
  } = {}) {
    return request<TaskListResponse>('/api/tasks' + qs(params));
  },
  get(taskId: string) {
    return request<TaskDetailResponse>(
      `/api/tasks/${encodeURIComponent(taskId)}`,
    );
  },
  deleteHistory(confirm: boolean) {
    return request<DeleteHistoryResponse>('/api/history', {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ confirm }),
    });
  },
};

// --- API-002: Analytics -----------------------------------------------------
export const analyticsApi = {
  calendar(params: { view: CalendarView; date: string }) {
    return request<CalendarResponse>('/api/calendar' + qs(params));
  },
  tokens(params: {
    period?: string;
    date?: string;
    from?: string;
    to?: string;
  }) {
    return request<TokenStatsResponse>('/api/tokens' + qs(params));
  },
};

// --- API-003: AI semantic search --------------------------------------------
export const searchApi = {
  search(body: SearchRequest, limit = 5) {
    return request<SearchResponse>('/api/search', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ...body, limit }),
    });
  },
};

// --- API-004: Health --------------------------------------------------------
export const healthApi = {
  get() {
    return request<HealthResponse>('/api/health');
  },
};
