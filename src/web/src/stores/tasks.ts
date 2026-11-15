import { defineStore } from 'pinia';
import { tasksApi, ApiError } from '@/api/client';
import type { TaskListResponse, TasksQuery } from '@/api/types';

export const useTasksStore = defineStore('tasks', {
  state: () => ({
    items: [] as TaskListResponse['items'],
    total: 0,
    page: 1,
    size: 50,
    loading: false,
    error: null as ApiError | null,
    lastUpdated: null as string | null,
  }),

  getters: {
    isEmpty: (s) => s.total === 0,
    hasData: (s) => s.total > 0,
  },

  actions: {
    async load(params: TasksQuery = {}) {
      this.loading = true;
      this.error = null;
      const page = params.page ?? this.page;
      const size = params.size ?? this.size;
      try {
        const data = await tasksApi.list({
          page,
          size,
          from: params.from,
          to: params.to,
          status: params.status,
        });
        this.items = data.items;
        this.total = data.total;
        this.page = page;
        this.size = size;
        this.lastUpdated = new Date().toISOString();
      } catch (err) {
        this.error =
          err instanceof ApiError
            ? err
            : new ApiError(500, 'SERVER_ERROR', 'Не удалось загрузить задачи');
      } finally {
        this.loading = false;
      }
    },
    async retry() {
      await this.load({ page: this.page, size: this.size });
    },
  },
});
