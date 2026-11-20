<template>
  <section class="dashboard" aria-labelledby="dashboard-title">
    <header class="dashboard__header">
      <h1 id="dashboard-title">Задачи</h1>
      <form class="dashboard__search" @submit.prevent="onSearch" role="search">
        <label for="dashboard-query" class="visually-hidden">Поиск по задачам</label>
        <input
          id="dashboard-query"
          v-model="query"
          type="search"
          class="dashboard__input"
          placeholder="Поиск по задачам"
        />
        <button class="dashboard__button" type="submit">Поиск</button>
      </form>
    </header>

    <p class="dashboard__updated" aria-live="polite">{{ updatedLabel }}</p>

    <div class="stat-grid">
      <StatCard
        title="Запущено"
        :value="stats.running"
      />
      <StatCard title="Завершено сегодня" :value="stats.completedToday" />
      <StatCard title="Завершена на неделе" :value="stats.completedWeek" />
      <StatCard title="Всего токенов" :value="stats.tokens.toLocaleString('ru-RU')" />
    </div>

    <div class="panel">
      <div class="panel__head">
        <h2 class="panel__title">Последние задачи</h2>
      </div>

      <div v-if="tasks.loading" class="panel__state" role="status" aria-live="polite">
        <span class="spinner" aria-hidden="true"></span> Загрузка…
      </div>

      <div v-else-if="banner" class="alert alert--error" role="alert">
        Источник данных (opencode) недоступен
      </div>

      <div v-else-if="tasks.error && tasks.total === 0" class="alert alert--error" role="alert">
        <div>Не удалось загрузить задачи</div>
        <button class="alert__retry" @click="onRetry">Повторить</button>
      </div>

      <div v-else-if="tasks.isEmpty" class="panel__state">
        Задач пока нет. Данные будут импортированы автоматически после синхронизации opencode.
      </div>

      <table v-else aria-label="Список задач">
        <thead>
          <tr>
            <th scope="col">Название</th>
            <th scope="col">Модель</th>
            <th scope="col">Дата</th>
            <th scope="col">Продолжительность</th>
            <th scope="col">Статус</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="task in tasks.items"
            :key="task.task_id"
            class="row--link"
          >
            <td>
              <router-link :to="`/tasks/${task.task_id}`">{{ task.name }}</router-link>
            </td>
            <td>
              <ModelCell :models="task.models" />
            </td>
            <td>{{ formatDate(task.started_at) }}</td>
            <td>{{ formatDuration(task.duration_s) }}</td>
            <td>
              <StatusBadge :status="task.status" />
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script lang="ts">
import { onMounted, reactive, computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useTasksStore } from '@/stores/tasks';
import { useHealthStore } from '@/stores/health';
import StatCard from '@/components/dashboard/StatCard.vue';
import StatusBadge from '@/components/dashboard/StatusBadge.vue';
import ModelCell from '@/components/ModelCell.vue';
import { formatDuration, formatDate } from '@/components/format';

export default {
  name: 'DashboardView',
  components: { StatCard, StatusBadge, ModelCell },
  setup() {
    const tasks = useTasksStore();
    const health = useHealthStore();
    const router = useRouter();
    const query = ref('');

    const updatedLabel = computed(
      () =>
        tasks.lastUpdated
          ? `Обновлено ${new Date(tasks.lastUpdated).toLocaleTimeString('ru-RU')}`
          : '',
    );

    const banner = computed(() => !health.sourceAvailable && tasks.total > 0);

    const stats = computed(() => {
      const items = tasks.items;
      const now = new Date();
      const startOfWeek = new Date(now);
      startOfWeek.setDate(now.getDate() - now.getDay());
      startOfWeek.setHours(0, 0, 0, 0);

      let running = 0;
      let completedToday = 0;
      let completedWeek = 0;
      let tokens = 0;
      for (const task of items) {
        if (task.status === 'Не завершена') running += 1;
        if (task.status === 'Завершена') {
          const date = new Date(task.started_at);
          if (!Number.isNaN(date.getTime())) {
            const sameDay = date.toDateString() === now.toDateString();
            const sameWeek = date >= startOfWeek;
            if (sameDay) completedToday += 1;
            if (sameWeek) completedWeek += 1;
          }
        }
        if (typeof task.tokens_total === 'number') tokens += task.tokens_total;
      }
      return { running, completedToday, completedWeek, tokens };
    });

    function onSearch(): void {
      const q = query.value.trim();
      const params = q ? `?q=${encodeURIComponent(q)}` : '';
      router.push(`/search${params}`);
    }

    function onRetry(): void {
      void tasks.retry();
      void health.probe();
    }

    onMounted(() => {
      void tasks.load();
      void health.probe();
    });

    return { tasks, health, updatedLabel, stats, banner, query, onSearch, onRetry, formatDuration, formatDate };
  },
};
</script>

<style scoped>
.dashboard__header {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}
.dashboard__input {
  flex: 1;
  min-width: 200px;
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  background: var(--surface);
  color: var(--text);
}
.dashboard__button {
  padding: var(--space-2) var(--space-3);
  border: 0;
  border-radius: var(--radius);
  background: var(--primary);
  color: var(--primary-contrast);
  cursor: pointer;
}
.dashboard__updated {
  color: var(--muted);
  font-size: 14px;
  margin: 0 0 var(--space-3);
}
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}
.stat-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: var(--space-3);
}
.stat-card__value {
  font-size: 28px;
  font-weight: 700;
}
.stat-card__label {
  color: var(--muted);
  margin-top: var(--space-1);
}
.panel {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: var(--space-3);
  margin-bottom: var(--space-4);
}
.panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-3);
}
.panel__count {
  color: var(--muted);
  font-size: 14px;
}
.row--link a {
  font-weight: 600;
}
.table-note,
.panel__state,
.alert,
.empty {
  color: var(--muted);
}
.alert {
  background: var(--error-bg);
  color: var(--error);
  border: 1px solid var(--error);
  border-radius: var(--radius);
  padding: var(--space-3);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}
.alert__retry,
.alert button {
  padding: var(--space-2) var(--space-3);
  border: 0;
  border-radius: var(--radius);
  background: var(--error);
  color: var(--surface);
  cursor: pointer;
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
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius);
  font-size: 13px;
}
.status-badge__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--muted);
}
.status-badge--Завершена .status-badge__dot {
  background: var(--success);
}
.status-badge--Прервана .status-badge__dot {
  background: var(--warn);
}
.status-badge--Не завершена .status-badge__dot {
  background: var(--primary);
}
</style>
