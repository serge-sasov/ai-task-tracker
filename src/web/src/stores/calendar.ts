import { defineStore } from 'pinia';
import { analyticsApi, ApiError } from '@/api/client';
import type { CalendarDay, CalendarResponse, CalendarView } from '@/api/types';

function pad(value: number): string {
  return String(value).padStart(2, '0');
}

function toLocal(date: Date): string {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

export function monthOf(baseDate: string): string {
  return `${baseDate.slice(0, 7)}-01`;
}

export function addMonths(baseDate: string, delta: number): string {
  const date = new Date(`${baseDate}T00:00:00`);
  date.setMonth(date.getMonth() + delta, 1);
  return toLocal(date);
}

function chunkWeeks(days: CalendarDay[], size = 7): CalendarDay[][] {
  const weeks: CalendarDay[][] = [];
  for (let i = 0; i < days.length; i += size) {
    weeks.push(days.slice(i, i + size));
  }
  return weeks;
}

export const useCalendarStore = defineStore('calendar', {
  state: () => ({
    view: 'month' as CalendarView,
    month: monthOf(toLocal(new Date())),
    days: [] as CalendarDay[],
    response: null as CalendarResponse | null,
    loading: false,
    error: null as ApiError | null,
    lastUpdated: null as string | null,
  }),

  getters: {
    weeks: (s) => chunkWeeks(s.days),
    isEmpty: (s) => s.days.length === 0,
    months: (s) => s.response?.months ?? [],
    total: (s) => s.days.reduce((acc, day) => acc + day.tasks.length, 0),
  },

  actions: {
    /** Load the full Mon-first month grid for the month containing `baseDate`. */
    async loadMonth(baseDate: string) {
      this.loading = true;
      this.error = null;
      try {
        const month = monthOf(baseDate);
        const response = await analyticsApi.calendar({ view: 'month', date: month });
        this.days = response.days;
        this.response = response;
        this.view = 'month';
        this.month = month;
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
      await this.loadMonth(this.month);
    },
  },
});
