<template>
  <section class="calendar" aria-labelledby="calendar-title">
    <header class="calendar__header">
      <h1 id="calendar-title">Календарь</h1>
      <p class="calendar__updated" aria-live="polite">{{ updatedLabel }}</p>
    </header>

    <div class="panel">
      <div class="calendar__controls">
        <div class="calendar__nav">
          <button class="calendar__nav-btn" type="button" aria-label="Прошлый месяц" @click="onPrev">‹</button>
          <span class="calendar__period" aria-live="polite">{{ periodLabel }}</span>
          <button class="calendar__nav-btn" type="button" aria-label="Следующий месяц" @click="onNext">›</button>
        </div>

        <button class="calendar__today" type="button" @click="onToday">Текущий месяц</button>
      </div>

      <div v-if="loading" class="calendar__state" role="status" aria-live="polite">
        <span class="spinner" aria-hidden="true"></span> Загрузка…
      </div>

      <div v-else-if="error" class="alert alert--error" role="alert">
        <div>Не удалось загрузить календарь</div>
        <button class="alert__retry" type="button" @click="onRetry">Повторить</button>
      </div>

      <div v-else-if="isEmpty" class="calendar__state" role="status">
        Нет задач в выбранном периоде.
      </div>

      <template v-else>
        <Heatmap
          :rows="weeks"
          :weekday-labels="WEEK_LABELS"
          @open="onOpenTask"
        />
        <Legend />
      </template>
    </div>
  </section>
</template>

<script lang="ts">
import { onMounted, ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useCalendarStore } from '@/stores/calendar';
import Heatmap, { type HeatmapDay } from '@/components/calendar/Heatmap.vue';
import Legend from '@/components/calendar/Legend.vue';

const WEEK_LABELS = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];
const MONTH_LABELS = [
  'Январь',
  'Февраль',
  'Март',
  'Апрель',
  'Май',
  'Июнь',
  'Июль',
  'Август',
  'Сентябрь',
  'Октябрь',
  'Ноябрь',
  'Декабрь',
];

function pad(v: number): string {
  return String(v).padStart(2, '0');
}
function toLocal(d: Date): string {
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}
function monthStart(d: Date): string {
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-01`;
}

function monthFromQuery(value: unknown): string | null {
  return typeof value === 'string' && /^\d{4}-(0[1-9]|1[0-2])$/.test(value)
    ? `${value}-01`
    : null;
}

export default {
  name: 'CalendarView',
  components: { Heatmap, Legend },
  setup() {
    const calendar = useCalendarStore();
    const route = useRoute();
    const router = useRouter();

    const monthAnchor = ref(monthFromQuery(route.query.month) ?? monthStart(new Date()));

    const periodLabel = computed(() => {
      const date = new Date(`${monthAnchor.value}T00:00:00`);
      return `${MONTH_LABELS[date.getMonth()]} ${date.getFullYear()}`;
    });

    // One page = one calendar month; the API returns a full Mon-first grid.
    const weeks = computed<HeatmapDay[][]>(() =>
      calendar.weeks.map((row) =>
        row.map((day) => ({
          date: day.date,
          dayOfMonth: Number(day.date.slice(8, 10)),
          count: day.tasks.length,
          outside: day.outside,
          current: day.date === toLocal(new Date()),
          clickable: true,
          label: `${day.date} (${day.tasks.length})`,
          tasks: day.tasks.map((task) => task.task_id),
        })),
      ),
    );

    const isEmpty = computed(() => calendar.isEmpty);

    function syncMonthQuery(): void {
      void router.replace({
        query: { ...route.query, month: monthAnchor.value.slice(0, 7) },
      });
    }

    function shiftMonth(delta: number): void {
      const date = new Date(`${monthAnchor.value}T00:00:00`);
      date.setMonth(date.getMonth() + delta, 1);
      monthAnchor.value = monthStart(date);
      syncMonthQuery();
      void calendar.loadMonth(monthAnchor.value);
    }

    function onPrev(): void {
      shiftMonth(-1);
    }
    function onNext(): void {
      shiftMonth(1);
    }
    function onToday(): void {
      monthAnchor.value = monthStart(new Date());
      syncMonthQuery();
      void calendar.loadMonth(monthAnchor.value);
    }

    function onOpenTask(date: string): void {
      void router.push(`/calendar/${date}`);
    }

    function onRetry(): void {
      void calendar.loadMonth(monthAnchor.value);
    }

    const loading = computed(() => calendar.loading);
    const error = computed(() => calendar.error);

    const updatedLabel = computed(() =>
      calendar.lastUpdated
        ? `Обновлено ${new Date(calendar.lastUpdated).toLocaleTimeString('ru-RU')}`
        : '',
    );

    onMounted(() => {
      void calendar.loadMonth(monthAnchor.value);
    });

    return {
      calendar,
      loading,
      error,
      weeks,
      isEmpty,
      WEEK_LABELS,
      periodLabel,
      updatedLabel,
      onPrev,
      onNext,
      onToday,
      onOpenTask,
      onRetry,
    };
  },
};
</script>

<style scoped>
.calendar {
  width: 100%;
}
.calendar__header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
}
.calendar__updated {
  color: var(--muted);
  font-size: 14px;
}
.panel {
  width: 100%;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: var(--space-3);
}
.calendar__controls {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
}
.calendar__nav {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}
.calendar__nav-btn {
  width: var(--space-5);
  height: var(--space-5);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  background: var(--surface);
  color: var(--text);
  cursor: pointer;
  font-size: 18px;
}
.calendar__period {
  color: var(--text);
  font-weight: 600;
  min-width: 160px;
  text-align: center;
}
.calendar__today {
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  background: var(--surface);
  color: var(--text);
  cursor: pointer;
  font-size: 14px;
}
.calendar__state {
  color: var(--muted);
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
