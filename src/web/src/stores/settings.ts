import { defineStore } from 'pinia';
import { tasksApi, ApiError } from '@/api/client';
import type { DeleteHistoryResponse } from '@/api/types';

export const useSettingsStore = defineStore('settings', {
  state: () => ({
    deleting: false,
    result: null as DeleteHistoryResponse | null,
    error: null as ApiError | null,
    message: null as string | null,
  }),

  actions: {
    async deleteHistory(confirm: boolean) {
      this.deleting = true;
      this.error = null;
      this.message = null;
      this.result = null;
      try {
        const data = await tasksApi.deleteHistory(confirm);
        this.result = data;
        this.message = data.deleted
          ? 'История удалена.'
          : 'Данных для удаления нет';
      } catch (err) {
        this.error =
          err instanceof ApiError
            ? err
            : new ApiError(
                500,
                'INTERNAL_ERROR',
                'Не удалось удалить историю. Повторите попытку.',
              );
      } finally {
        this.deleting = false;
      }
    },
  },
});
