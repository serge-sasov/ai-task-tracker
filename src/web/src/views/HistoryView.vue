<template>
  <section class="tokens" aria-labelledby="tokens-title">
    <header class="tokens__header">
      <h1 id="tokens-title">Токены</h1>
      <p class="tokens__updated" aria-live="polite">{{ updatedLabel }}</p>
    </header>

    <div class="panel">
      <div class="tokens__controls">
        <label class="select" for="period-select">
          Период:
          <select id="period-select" :value="selectedPeriod" @change="onPeriodChange">
            <option value="all">Всё время</option>
            <option value="day">День</option>
            <option value="week">Неделя</option>
            <option value="month">Месяц</option>
          </select>
        </label>

        <label v-if="selectedPeriod === 'day'" class="date" for="date-input">
          Дата:
          <input id="date-input" v-model="dayValue" type="date" class="date__input" />
        </label>
      </div>

      <div v-if="loading" class="tokens__state" role="status" aria-live="polite">
        <span class="spinner" aria-hidden="true"></span> Загрузка…
      </div>

      <div v-else-if="error" class="alert alert--error" role="alert">
        <div>Не удалось построить сводку по токенам</div>
        <button class="alert__retry" type="button" @click="onRetry">Повторить</button>
      </div>

      <div v-else-if="!hasData" class="tokens__state" role="status">
        Нет данных за выбранный период.
      </div>

      <table v-else class="tokens__table" aria-label="Токены по моделям">
        <thead>
          <tr>
            <th scope="col">Модель</th>
            <th scope="col">Вход</th>
            <th scope="col">Выход</th>
            <th scope="col">Итого</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="model in byModel" :key="model.model">
            <td scope="row">{{ model.model }}</td>
            <td>{{ formatNumber(model.input_tokens) }}</td>
            <td>{{ formatNumber(model.output_tokens) }}</td>
            <td>{{ formatNumber(model.total_tokens) }}</td>
          </tr>
        </tbody>
        <tfoot>
          <tr>
            <th scope="row">Всего</th>
            <td>{{ formatNumber(total.input_tokens) }}</td>
            <td>{{ formatNumber(total.output_tokens) }}</td>
            <td class="tokens__grand">{{ formatNumber(total.total_tokens) }}</td>
          </tr>
        </tfoot>
      </table>
    </div>

    <div class="totals">
      <StatCard title="Всего токенов" :value="formatNumber(total.total_tokens)" />
    </div>
  </section>
</template>

<script lang="ts">
import { onMounted, ref, computed } from 'vue';
import { useTokensStore } from '@/stores/tokens';
import StatCard from '@/components/dashboard/StatCard.vue';

export default {
  name: 'HistoryView',
  components: { StatCard },
  setup() {
    const tokens = useTokensStore();
    const selectedPeriod = ref('all');
    const dayValue = ref(new Date().toISOString().slice(0, 10));

    const hasData = computed(() => tokens.hasData);
    const loading = computed(() => tokens.loading);
    const error = computed(() => tokens.error);
    const byModel = computed(() => tokens.byModel);
    const total = computed(() => tokens.total);

    const updatedLabel = computed(() =>
      tokens.response
        ? `Обновлено ${new Date().toLocaleTimeString('ru-RU')}`
        : '',
    );

    function selectPeriod(): void {
      const period = selectedPeriod.value;
      const params: { date?: string } = {};
      if (period === 'day') params.date = dayValue.value;
      void tokens.load(period, params);
    }

    function onPeriodChange(e: Event): void {
      selectedPeriod.value = (e.target as HTMLSelectElement).value;
      selectPeriod();
    }
    function onRetry(): void {
      selectPeriod();
    }

    function formatNumber(n: number): string {
      return n.toLocaleString('ru-RU');
    }

    onMounted(selectPeriod);

    return {
      tokens,
      selectedPeriod,
      dayValue,
      hasData,
      loading,
      error,
      byModel,
      total,
      updatedLabel,
      onPeriodChange,
      onRetry,
      formatNumber,
    };
  },
};
</script>

<style scoped>
.tokens__header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
}
.tokens__updated {
  color: var(--muted);
  font-size: 14px;
}
.panel {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: var(--space-3);
  margin-bottom: var(--space-4);
}
.tokens__controls {
  display: flex;
  flex-wrap: wrap;
  align-items: end;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
}
.select,
.date {
  display: inline-flex;
  flex-direction: column;
  font-size: 12px;
  color: var(--muted);
}
.date__input,
.select select {
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  background: var(--surface);
  color: var(--text);
}
.tokens__state {
  color: var(--muted);
}
.tokens__table {
  width: 100%;
}
.tokens__grand {
  font-weight: 700;
  color: var(--text);
}
.totals {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: var(--space-3);
}
.spinner {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid var(--border);
  border-top-color: var(--primary);
  border-radius: 50%;
  vertical-align: -1px;
  animation: spin 0.7s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
