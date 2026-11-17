import { defineStore } from 'pinia';
import { analyticsApi, ApiError } from '@/api/client';
import type { CalendarDay, CalendarResponse, CalendarView } from '@/api/types';

function pad(value: number): string {
  return String(value).padStart(2, '0');
}

function toLocal(date: Date): string {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

function addDays(base: string, delta: number): string {
  const date = new Date(`${base}T00:00:00`);
  date.setDate(date.getDate() + delta);
  return toLocal(date);
}

function mondayOf(base: string): string {
  const date = new Date(`${base}T00:00:00`);
  const dow = date.getDay();
  return addDays(base, dow === 0 ? -6 : 1 - dow);
}

export const useCalendarStore = defineStore('calendar', {
  state: () => ({
    view: 'week' as CalendarView,
    date: toLocal(new Date()),
    weeks: [] as CalendarDay[][],
    response: null as CalendarResponse | null,
    loading: false,
    error: null as ApiError | null,
    lastUpdated: null as string | null,
  }),

  getters: {
    isEmpty: (s) =>
      s.weeks.length === 0 ||
      s.weeks.every((week) => week.every((day) => day.tasks.length === 0)),
    days: (s) => s.weeks.flat(),
    months: (s) => s.response?.months ?? [],
    total: (s) =>
      s.weeks.reduce(
        (acc, week) => acc + week.reduce((a, day) => a + day.tasks.length, 0),
        0,
      ),
  },

  actions: {
    /** Load a heatmap window of `count` consecutive Mon-anchored weeks. */
    async loadWeeks(baseDate: string, count = 12) {
      this.loading = true;
      this.error = null;
      try {
        const start = mondayOf(baseDate);
        const dates: string[] = [];
        for (let i = 0; i < count; i += 1) {
          dates.push(addDays(start, i * 7));
        }
        const responses = await Promise.all(
          dates.map((date) => analyticsApi.calendar({ view: 'week', date })),
        );
        this.weeks = responses.map((response) => response.days);
        this.response = responses[0] ?? null;
        this.view = 'week';
        this.date = baseDate;
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
      await this.loadWeeks(this.date);
    },
  },
});
