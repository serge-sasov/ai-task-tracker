<template>
  <section class="calendar" aria-labelledby="calendar-title">
    <header class="calendar__header">
      <h1 id="calendar-title">Календарь</h1>
      <p class="calendar__updated" aria-live="polite">{{ updatedLabel }}</p>
    </header>

    <div class="panel">
      <div class="calendar__controls">
        <div class="segmented" role="radiogroup" aria-label="Пресеты периода">
          <button
            v-for="preset in presets"
            :key="preset.id"
            type="button"
            class="segmented__item"
            :class="{ 'segmented__item--active': modeId === preset.id }"
            role="radio"
            :aria-checked="modeId === preset.id ? 'true' : 'false'"
            @click="onMode(preset.id)"
          >
            {{ preset.label }}
          </button>
        </div>

        <div class="calendar__nav">
          <button class="calendar__nav-btn" type="button" aria-label="Прошлый период" @click="onPrev">‹</button>
          <span class="calendar__period">{{ periodLabel }}</span>
          <button class="calendar__nav-btn" type="button" aria-label="Следующий период" @click="onNext">›</button>
        </div>
      </div>

      <div v-if="customRange" class="calendar__range">
        <label class="range__label">Начало: <input id="range-from" v-model="rangeFrom" type="date" class="range__input" /></label>
        <label class="range__label">Конец: <input id="range-to" v-model="rangeTo" type="date" class="range__input" /></label>
        <button class="range__apply" type="button" @click="onApplyRange">Применить</button>
      </div>

      <div v-if="loading" class="calendar__state" role="status" aria-live="polite">
        <span class="spinner" aria-hidden="true"></span> Загрузка…
      </div>

      <div v-else-if="error" class="alert alert--error" role="alert">
        <div>Не удалось загрузить календарь</div>
        <button class="alert__retry" type="button" @click="onRetry">Повторить</button>
      </div>

      <div v-else-if="weeks.length === 0" class="calendar__state" role="status">
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

    <Dialog
      :open="dialogOpen"
      :title="dialogTitle"
      @close="dialogOpen = false"
      @confirm="onConfirmOpen"
    >
      <p class="dialog__body">{{ dialogText }}</p>
    </Dialog>
  </section>
</template>

<script lang="ts">
import { onMounted, ref, computed } from 'vue';
import { useCalendarStore } from '@/stores/calendar';
import Heatmap, { type HeatmapDay } from '@/components/calendar/Heatmap.vue';
import Legend from '@/components/calendar/Legend.vue';
import Dialog from '@/components/shared/Dialog.vue';

const WEEK_LABELS = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];

function pad(v: number): string {
  return String(v).padStart(2, '0');
}
function toLocal(d: Date): string {
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}
function addDays(d: Date, delta: number): Date {
  const n = new Date(d);
  n.setDate(n.getDate() + delta);
  return n;
}

export default {
  name: 'CalendarView',
  components: { Heatmap, Legend, Dialog },
  setup() {
    const calendar = useCalendarStore();

    const modeId = ref<'12w' | 'custom'>('12w');
    const weekStart = ref(toLocal(addDays(new Date(), -35)));
    const dialogOpen = ref(false);
    const dialogTitle = ref('');
    const dialogText = ref('');
    const customRange = ref(false);
    const rangeFrom = ref(toLocal(addDays(new Date(), -35)));
    const rangeTo = ref(toLocal(new Date()));

    const presets = computed(() => [
      { id: '12w' as const, label: '12 недель' },
      { id: 'custom' as const, label: 'Произвольно' },
    ]);

    const periodLabel = computed(() => weekStart.value);

    // Build 12 weeks × 7 weekday cells starting at the current week.
    function buildWeeks(base: string): typeof weeks {
      const start = new Date(base);
      // Align to Monday.
      const dow = start.getDay();
      const mondayOffset = dow === 0 ? -6 : 1 - dow;
      start.setDate(start.getDate() + mondayOffset);
      start.setHours(0, 0, 0, 0);

      const weeks: HeatmapDay[][] = [];
      for (let w = 0; w < 12; w++) {
        const row: HeatmapDay[] = [];
        for (let day = 0; day < 7; day++) {
          const d = addDays(start, w * 7 + day);
          row.push({
            date: toLocal(d),
            count: 0,
            outside: false,
            current: toLocal(d) === toLocal(new Date()),
            clickable: true,
            label: toLocal(d),
            tasks: [],
          });
        }
        weeks.push(row);
      }
      return weeks;
    }

    const weeks = ref(buildWeeks(weekStart.value));

    function onMode(id: '12w' | 'custom'): void {
      modeId.value = id;
      customRange.value = id === 'custom';
    }

    function onPrev(): void {
      weekStart.value = toLocal(addDays(new Date(weekStart.value), -7 * 12));
      weeks.value = buildWeeks(weekStart.value);
    }
    function onNext(): void {
      weekStart.value = toLocal(addDays(new Date(weekStart.value), 7 * 12));
      weeks.value = buildWeeks(weekStart.value);
    }

    function onApplyRange(): void {
      weekStart.value = rangeFrom.value;
      weeks.value = buildWeeks(rangeFrom.value);
    }

    function onOpenTask(date: string): void {
      dialogTitle.value = `Открыть задачу — ${date}`;
      dialogText.value = `Задачи на ${date}. Выберите задачу, чтобы открыть детали.`;
      dialogOpen.value = true;
    }
    function onConfirmOpen(): void {
      dialogOpen.value = false;
    }

    function onRetry(): void {
      void calendar.reload();
    }

    const loading = computed(() => calendar.loading);
    const error = computed(() => calendar.error);

    const updatedLabel = computed(() =>
      calendar.lastUpdated
        ? `Обновлено ${new Date(calendar.lastUpdated).toLocaleTimeString('ru-RU')}`
        : '',
    );

    onMounted(() => {
      void calendar.reload();
    });

    return {
      calendar,
      loading,
      error,
      weeks,
      WEEK_LABELS,
      modeId,
      periodLabel,
      customRange,
      rangeFrom,
      rangeTo,
      presets,
      updatedLabel,
      dialogOpen,
      dialogTitle,
      dialogText,
      onMode,
      onPrev,
      onNext,
      onApplyRange,
      onOpenTask,
      onConfirmOpen,
      onRetry,
    };
  },
};
</script>

<style scoped>
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
.segmented {
  display: inline-flex;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  overflow: hidden;
}
.segmented__item {
  background: var(--surface);
  border: 0;
  padding: var(--space-2) var(--space-3);
  color: var(--text);
  cursor: pointer;
  font-size: 14px;
}
.segmented__item--active {
  background: var(--primary);
  color: var(--primary-contrast);
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
  color: var(--muted);
  min-width: 140px;
  text-align: center;
}
.calendar__range {
  display: flex;
  flex-wrap: wrap;
  align-items: end;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
}
.range__label {
  display: inline-flex;
  flex-direction: column;
  font-size: 12px;
  color: var(--muted);
}
.range__input {
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  background: var(--surface);
  color: var(--text);
}
.range__apply {
  padding: var(--space-2) var(--space-3);
  border: 0;
  border-radius: var(--radius);
  background: var(--primary);
  color: var(--primary-contrast);
  cursor: pointer;
}
.calendar__state {
  color: var(--muted);
}
.dialog__body {
  margin: 0;
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
