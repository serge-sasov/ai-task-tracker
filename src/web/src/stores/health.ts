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
    sourceAvailable: (s) => s.available === true,
    searchAvailable: (s) => s.response?.ollama === 'up',
  },

  actions: {
    async probe() {
      this.loading = true;
      this.error = null;
      try {
        this.response = await healthApi.get();
        this.available = this.response?.backend === 'up';
      } catch {
        this.available = false;
        this.response = null;
      } finally {
        this.loading = false;
      }
    },
  },
});
