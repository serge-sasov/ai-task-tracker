import { defineStore } from 'pinia';
import { healthApi, ApiError } from '@/api/client';
import type { HealthResponse } from '@/api/types';

export const useHealthStore = defineStore('health', {
  state: () => ({
    response: null as HealthResponse | null,
    loading: false,
    error: null as ApiError | null,
    available: null as boolean | null,
  }),

  getters: {
    // The source is readable when it is not explicitly unavailable; `stale`
    // still carries the ingested data (only the file mtime is old).
    sourceAvailable: (s) => s.response != null && s.response.source_status !== 'unavailable',
    sourceStale: (s) => s.response?.source_status === 'stale',
    searchAvailable: (s) => s.response?.ollama_status === 'up',
  },

  actions: {
    async probe() {
      this.loading = true;
      this.error = null;
      try {
        this.response = await healthApi.get();
        this.available =
          this.response?.checks?.postgres === 'ok' &&
          this.response?.checks?.redis === 'ok';
      } catch {
        this.available = false;
        this.response = null;
      } finally {
        this.loading = false;
      }
    },
  },
});
