import { describe, it, expect, vi } from 'vitest';

// S-01 (DashboardView) regression ------------------------------------------------------------
describe('S-01 DashboardView model cell (regression)', () => {
  const s = vi.hoisted(() => {
    const noUsage = {
      task_id: 'TASK-002',
      name: 'Задача без usage',
      started_at: '2026-11-11T13:00:00',
      finished_at: '2026-11-11T15:00:00',
      status: 'Завершена',
      duration_s: 7200,
      tokens_total: null,
      models: [],
    };
    const twoModels = {
      task_id: 'TASK-003',
      name: 'Задача с 2 моделями',
      started_at: '2026-11-11T08:30:00',
      finished_at: '2026-11-11T10:45:00',
      status: 'Прервана',
      duration_s: 8100,
      tokens_total: null,
      models: ['qwen3.8:27b', 'llama3.1:8b'],
    };
    return {
      tasksState: {
        items: [noUsage, twoModels],
        total: 2,
        loading: false,
        error: null,
        lastUpdated: null,
        load: vi.fn(),
        retry: vi.fn(),
      },
      healthState: { sourceAvailable: true, available: null, response: null, probe: vi.fn() },
    };
  });

  beforeEach(() => {
    vi.mock('@/stores/tasks', () => ({ useTasksStore: () => s.tasksState }));
    vi.mock('@/stores/health', () => ({ useHealthStore: () => s.healthState }));
    vi.mock('vue-router', () => ({
      useRouter: () => ({ push: vi.fn() }),
      useRoute: () => ({ params: {} }),
    }));
  });

  it('renders the shared ModelCell with primary + «+1» and full aria-label', async () => {
    const { default: DashboardView } = await import('@/views/DashboardView.vue');
    const { mount } = await import('@vue/test-utils');
    const wrapper = mount(DashboardView, {
      global: { stubs: { StatusBadge: true } },
    });

    const rows = wrapper.findAll('tbody tr');
    const modelCells = rows.flatMap((r) => r.findAll('.model-cell'));
    expect(modelCells.length).toBe(2);

    const twoRow = modelCells[1];
    expect(twoRow.text()).toContain('qwen3.8:27b');
    expect(twoRow.find('.model-cell__more').text()).toContain('+1');
    expect(twoRow.attributes('title')).toBe('Модель: qwen3.8:27b, llama3.1:8b');
    expect(twoRow.attributes('aria-label')).toBe('Модель: qwen3.8:27b, llama3.1:8b');

    const emptyRow = modelCells[0];
    expect(emptyRow.text()).toContain('Нет данных');
    expect(emptyRow.find('.model-cell__more').exists()).toBe(false);
    expect(emptyRow.attributes('title')).toBe('Модель: Нет данных');
  });
});

// S-04 (CalendarDayView) regression ----------------------------------------------------------
describe('S-04 CalendarDayView model cell (regression)', () => {
  const s2 = vi.hoisted(() => {
    const twoModels = {
      task_id: 'TASK-003',
      name: 'Задача с 2 моделями',
      started_at: '2026-11-11T08:30:00',
      finished_at: '2026-11-11T10:45:00',
      status: 'Прервана',
      duration_s: 8100,
      tokens_total: null,
      models: ['qwen3.8:27b', 'llama3.1:8b'],
    };
    const noUsage = {
      task_id: 'TASK-002',
      name: 'Задача без usage',
      started_at: '2026-11-11T13:00:00',
      finished_at: '2026-11-11T15:00:00',
      status: 'Завершена',
      duration_s: 7200,
      tokens_total: null,
      models: [],
    };
    const api = { list: vi.fn() };
    return { twoModels, noUsage, api };
  });

  beforeEach(() => {
    vi.mock('@/api/client', () => ({
      tasksApi: { list: s2.api.list },
      ApiError: class ApiError extends Error {
        status = 0;
        code = '';
      },
    }));
    vi.mock('vue-router', () => ({
      useRouter: () => ({ push: vi.fn() }),
      useRoute: () => ({ params: { date: '2026-11-11' }, query: {} }),
    }));
  });

  async function loadDay(items: unknown[]) {
    s2.api.list.mockResolvedValue({ total: items.length, items });
    const { default: CalendarDayView } = await import('@/views/CalendarDayView.vue');
    const { mount } = await import('@vue/test-utils');
    const wrapper = mount(CalendarDayView, {
      global: { stubs: { StatusBadge: true } },
    });
    await wrapper.vm.$nextTick();
    await new Promise((r) => setTimeout(r, 0));
    return wrapper;
  }

  it('renders «Модель» column immediately after «Задача» with shared cell', async () => {
    const wrapper = await loadDay([s2.twoModels, s2.noUsage]);

    const headerTexts = wrapper.findAll('thead th').map((h) => h.text());
    expect(headerTexts).toEqual(
      expect.arrayContaining([
        'Задача',
        'Модель',
        'Начало',
        'Окончание',
        'Продолжительность',
        'Статус',
      ]),
    );
    expect(headerTexts.indexOf('Модель')).toBe(headerTexts.indexOf('Задача') + 1);

    const rows = wrapper.findAll('tbody tr');
    const firstRowCells = rows[0].findAll('td');
    const modelCell = firstRowCells[1].find('.model-cell');
    expect(modelCell.exists()).toBe(true);
    expect(modelCell.text()).toContain('qwen3.8:27b');
    expect(modelCell.find('.model-cell__more').text()).toContain('+1');
    expect(modelCell.attributes('title')).toBe('Модель: qwen3.8:27b, llama3.1:8b');
  });

  it('renders «Нет данных» cell for a no-usage task in S-04', async () => {
    const wrapper = await loadDay([s2.noUsage]);
    const modelCell = wrapper.findAll('tbody tr')[0].findAll('td')[1].find('.model-cell');
    expect(modelCell.text()).toContain('Нет данных');
    expect(modelCell.find('.model-cell__more').exists()).toBe(false);
    expect(modelCell.attributes('title')).toBe('Модель: Нет данных');
  });
});
