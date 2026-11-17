<template>
  <section class="day" aria-labelledby="day-title">
    <header class="day__header">
      <router-link class="day__back" :to="monthLink">‹ К календарю</router-link>
      <div class="day__nav">
        <button class="day__nav-btn" type="button" aria-label="Предыдущий день" @click="shiftDay(-1)">‹</button>
        <h1 id="day-title" class="day__title">{{ dayLabel }}</h1>
        <button class="day__nav-btn" type="button" aria-label="Следующий день" @click="shiftDay(1)">›</button>
      </div>
    </header>

    <div v-if="loading" class="day__state" role="status" aria-live="polite">
      <span class="spinner" aria-hidden="true"></span> Загрузка…
    </div>

    <div v-else-if="error" class="alert alert--error" role="alert">
      <div>{{ error.message || 'Не удалось загрузить задачи дня' }}</div>
      <button class="alert__retry" type="button" @click="load">Повторить</button>
    </div>

    <div v-else-if="tasks.length === 0" class="panel day__empty" role="status">
      На {{ dayLabel.toLowerCase() }} задач нет.
    </div>

    <template v-else>
      <div class="day__summary" aria-live="polite">
        <span>{{ tasks.length }} {{ taskWord }}</span>
        <span>Суммарно: {{ formatDuration(totalDuration) }}</span>
      </div>

      <div class="panel">
        <table aria-label="Задачи за день">
          <thead>
            <tr>
              <th scope="col">Задача</th>
              <th scope="col">Начало</th>
              <th scope="col">Окончание</th>
              <th scope="col">Продолжительность</th>
              <th scope="col">Статус</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="task in tasks" :key="task.task_id">
              <td>
                <router-link :to="`/tasks/${task.task_id}`">{{ task.name }}</router-link>
              </td>
              <td>{{ timeOf(task.started_at) }}</td>
              <td>{{ task.finished_at ? timeOf(task.finished_at) : '—' }}</td>
              <td>{{ formatDuration(task.duration_s) }}</td>
              <td>
                <StatusBadge :status="task.status" />
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>
  </section>
</template>

<script lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { tasksApi, ApiError } from '@/api/client';
import type { TaskListItem } from '@/api/types';
import StatusBadge from '@/components/dashboard/StatusBadge.vue';
import { formatDuration } from '@/components/format';

const MONTHS_GENITIVE = [
  'января',
  'февраля',
  'марта',
  'апреля',
  'мая',
  'июня',
  'июля',
  'августа',
  'сентября',
  'октября',
  'ноября',
  'декабря',
];

function pad(v: number): string {
  return String(v).padStart(2, '0');
}
function toLocal(d: Date): string {
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}
function plural(n: number, one: string, few: string, many: string): string {
  const mod10 = n % 10;
  const mod100 = n % 100;
  if (mod10 === 1 && mod100 !== 11) return one;
  if (mod10 >= 2 && mod10 <= 4 && (mod100 < 12 || mod100 > 14)) return few;
  return many;
}

export default {
  name: 'CalendarDayView',
  components: { StatusBadge },
  setup() {
    const route = useRoute();
    const router = useRouter();

    const tasks = ref<TaskListItem[]>([]);
    const loading = ref(true);
    const error = ref<ApiError | null>(null);

    const date = computed(() => {
      const raw = String(route.params.date ?? '');
      return /^\d{4}-\d{2}-\d{2}$/.test(raw) ? raw : '';
    });
    const validDate = computed(() => date.value !== '' && !Number.isNaN(new Date(`${date.value}T00:00:00`).getTime()));

    const dayLabel = computed(() => {
      if (!validDate.value) return 'Некорректная дата';
      const d = new Date(`${date.value}T00:00:00`);
      return `${d.getDate()} ${MONTHS_GENITIVE[d.getMonth()]} ${d.getFullYear()}`;
    });

    const monthLink = computed(() => {
      const month = validDate.value ? date.value.slice(0, 7) : '';
      return month ? `/calendar?month=${month}` : '/calendar';
    });

    const totalDuration = computed(() =>
      tasks.value.reduce((acc, task) => acc + (task.duration_s || 0), 0),
    );
    const taskWord = computed(() => plural(tasks.value.length, 'задача', 'задачи', 'задач'));

    function timeOf(iso: string): string {
      const d = new Date(iso);
      if (Number.isNaN(d.getTime())) return iso;
      return `${pad(d.getHours())}:${pad(d.getMinutes())}`;
    }

    async function load(): Promise<void> {
      loading.value = true;
      error.value = null;
      if (!validDate.value) {
        tasks.value = [];
        error.value = new ApiError(400, 'INVALID_PERIOD', 'Некорректная дата');
        loading.value = false;
        return;
      }
      try {
        const data = await tasksApi.list({
          from: `${date.value}T00:00:00`,
          to: `${date.value}T23:59:59`,
          size: 500,
        });
        tasks.value = [...data.items].sort((a, b) => a.started_at.localeCompare(b.started_at));
      } catch (err) {
        tasks.value = [];
        error.value =
          err instanceof ApiError
            ? err
            : new ApiError(500, 'SERVER_ERROR', 'Не удалось загрузить задачи дня');
      } finally {
        loading.value = false;
      }
    }

    function shiftDay(delta: number): void {
      if (!validDate.value) return;
      const next = new Date(`${date.value}T00:00:00`);
      next.setDate(next.getDate() + delta);
      void router.push(`/calendar/${toLocal(next)}`);
    }

    watch(() => route.params.date, load);

    onMounted(load);

    return {
      tasks,
      loading,
      error,
      dayLabel,
      monthLink,
      totalDuration,
      taskWord,
      timeOf,
      load,
      shiftDay,
      formatDuration,
    };
  },
};
</script>

<style scoped>
.day__header {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
}
.day__back {
  font-weight: 600;
}
.day__nav {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}
.day__nav-btn {
  width: var(--space-5);
  height: var(--space-5);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  background: var(--surface);
  color: var(--text);
  cursor: pointer;
  font-size: 18px;
}
.day__title {
  margin: 0;
  font-size: 22px;
  min-width: 220px;
  text-align: center;
}
.day__summary {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
  color: var(--muted);
  font-size: 14px;
  margin-bottom: var(--space-3);
}
.day__state,
.day__empty {
  color: var(--muted);
}
.panel {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: var(--space-3);
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
.alert__retry {
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
</style>
