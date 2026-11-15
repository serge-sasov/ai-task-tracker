import { defineStore } from 'pinia';
import { analyticsApi, ApiError } from '@/api/client';
import type { CalendarResponse, CalendarView } from '@/api/types';

export const useCalendarStore = defineStore('calendar', {
  state: () => ({
    view: 'week' as CalendarView,
    date: new Date().toISOString().slice(0, 10),
    response: null as CalendarResponse | null,
    loading: false,
    error: null as ApiError | null,
    lastUpdated: null as string | null,
  }),

  getters: {
    isEmpty: (s) => (s.response?.days ?? []).every((d) => d.tasks.length === 0),
    days: (s) => s.response?.days ?? [],
    months: (s) => s.response?.months ?? [],
    total: (s) =>
      (s.response?.days ?? []).reduce(
        (acc, d) => acc + d.tasks.length,
        0,
      ),
  },

  actions: {
    async load(view: CalendarView, date: string) {
      this.loading = true;
      this.error = null;
      this.view = view;
      this.date = date;
      try {
        this.response = await analyticsApi.calendar({ view, date });
      } catch (err) {
        this.error =
          err instanceof ApiError
            ? err
            : new ApiError(500, 'SERVER_ERROR', 'Не удалось загрузить календарь');
      } finally {
        this.loading = false;
        this.lastUpdated = new Date().toISOString();
      }
    },
    async reload() {
      await this.load(this.view, this.date);
    },
  },
});
