import { defineStore } from 'pinia';
import { analyticsApi, ApiError } from '@/api/client';
import type { TokenStatsResponse } from '@/api/types';

export const useTokensStore = defineStore('tokens', {
  state: () => ({
    period: 'all',
    response: null as TokenStatsResponse | null,
    loading: false,
    error: null as ApiError | null,
  }),

  getters: {
    hasData: (s) => s.response?.has_data ?? false,
    total: (s) => s.response?.total ?? { input_tokens: 0, output_tokens: 0, total_tokens: 0 },
    byModel: (s) => s.response?.by_model ?? [],
  },

  actions: {
    async load(period: string, params: { date?: string; from?: string; to?: string } = {}) {
      this.loading = true;
      this.error = null;
      this.period = period;
      try {
        this.response = await analyticsApi.tokens({ period, ...params });
      } catch (err) {
        this.error =
          err instanceof ApiError
            ? err
            : new ApiError(500, 'SERVER_ERROR', 'Не удалось загрузить статистику токенов');
      } finally {
        this.loading = false;
      }
    },
  },
});
