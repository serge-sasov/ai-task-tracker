<template>
  <div class="heatmap" role="group" aria-label="Календарь активности задач">
    <WeekdayRow :weekday-labels="weekdayLabels" />
    <div class="heatmap__rows" role="rowgroup">
      <div
        v-for="(week, weekIndex) in rows"
        :key="weekIndex"
        class="heatmap__week"
        role="row"
      >
        <div
          v-for="(day, dayIndex) in week"
          :key="dayIndex"
          class="heatmap__cell"
          :class="cellClass(day)"
          :role="day.clickable ? 'button' : 'gridcell'"
          :tabindex="day.clickable ? 0 : -1"
          :aria-label="day.label"
          :aria-current="day.current ? 'true' : undefined"
          :inert="day.outside ? 'true' : undefined"
          @click="onDayClick(day)"
          @keydown.enter="onDayClick(day)"
          @keydown.space.prevent="onDayClick(day)"
        >
          <div class="heatmap__head" aria-hidden="true">
            <span class="heatmap__daynum">{{ day.dayOfMonth }}</span>
            <span v-if="day.count > 0" class="heatmap__value">{{ day.count }}</span>
          </div>
          <div class="heatmap__tasks" aria-hidden="true">
            <span v-for="(task, i) in day.tasks" :key="i" class="heatmap__task">{{ task }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import WeekdayRow from './WeekdayRow.vue';

export interface HeatmapDay {
  date: string;
  dayOfMonth: number;
  count: number;
  outside: boolean;
  current: boolean;
  clickable: boolean;
  label: string;
  tasks: string[];
}

export default {
  name: 'Heatmap',
  components: { WeekdayRow },
  props: {
    rows: { type: Array as () => HeatmapDay[][], required: true },
    weekdayLabels: { type: Array as () => string[], required: true },
  },
  emits: ['open', 'day'],
  setup(props, { emit }) {
    function onDayClick(day: HeatmapDay): void {
      if (!day.clickable) return;
      emit('day', day.date);
      emit('open', day.date);
    }
    function cellClass(day: HeatmapDay): string {
      const classes = ['heatmap__cell--active'];
      const level = Math.min(Math.max(day.count, 0), 3);
      classes.push(`heatmap__cell--lvl-${level}`);
      if (day.outside) classes.push('heatmap__cell--outside');
      if (day.current) classes.push('heatmap__cell--current');
      return classes.join(' ');
    }
    return { onDayClick, cellClass };
  },
};
</script>

<style scoped>
.heatmap {
  width: 100%;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: var(--space-3);
}
.heatmap__rows {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  margin-top: var(--space-1);
}
.heatmap__week {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: var(--space-1);
}
.heatmap__cell {
  width: 100%;
  min-height: 88px;
  border-radius: 2px;
  background: var(--surface);
  border: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  align-items: stretch;
  justify-content: flex-start;
  gap: var(--space-1);
  position: relative;
  cursor: default;
  padding: var(--space-1) var(--space-2);
  overflow: hidden;
}
.heatmap__cell--outside {
  background: var(--bg);
  border: 1px solid var(--border);
  opacity: 0.5;
}
.heatmap__cell--active {
  border-color: var(--primary);
}
.heatmap__cell--lvl-1 {
  background: rgba(47, 111, 79, 0.35);
}
.heatmap__cell--lvl-2 {
  background: rgba(47, 111, 79, 0.65);
  color: var(--primary-contrast);
}
.heatmap__cell--lvl-3 {
  background: var(--primary);
  color: var(--primary-contrast);
}
.heatmap__cell--current {
  outline: 2px solid var(--primary);
  outline-offset: 1px;
}
.heatmap__cell[tabindex='0'] {
  cursor: pointer;
}
.heatmap__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-1);
}
.heatmap__daynum {
  font-size: 13px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  line-height: 1.2;
}
.heatmap__value {
  font-size: 11px;
  line-height: 1;
  padding: 1px 5px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.75);
  color: var(--text);
}
.heatmap__tasks {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}
.heatmap__task {
  font-size: 11px;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 100%;
}
</style>
