import { defineStore } from 'pinia';
import { searchApi, ApiError } from '@/api/client';
import type { SearchResponse } from '@/api/types';

export const useSearchStore = defineStore('search', {
  state: () => ({
    query: '',
    response: null as SearchResponse | null,
    loading: false,
    error: null as ApiError | null,
    validationError: null as string | null,
  }),

  getters: {
    isEmpty: (s) => (s.response?.results ?? []).length === 0,
    results: (s) => s.response?.results ?? [],
    isValid: (s) => s.query.trim().length > 0,
  },

  actions: {
    setQuery(query: string) {
      this.query = query;
      this.validationError = query.trim().length === 0 ? 'Введите запрос' : null;
    },
    async run(query: string) {
      const q = query.trim();
      if (q.length === 0) {
        this.validationError = 'Введите запрос';
        this.response = null;
        return;
      }
      this.loading = true;
      this.error = null;
      this.validationError = null;
      this.query = q;
      try {
        this.response = await searchApi.search({ query: q }, 5);
      } catch (err) {
        this.error =
          err instanceof ApiError
            ? err
            : new ApiError(500, 'SERVER_ERROR', 'AI-поиск недоступен');
      } finally {
        this.loading = false;
      }
    },
  },
});
