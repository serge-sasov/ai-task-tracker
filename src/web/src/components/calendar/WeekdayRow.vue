<template>
  <div class="heatmap__weekdays" role="rowgroup">
    <div class="heatmap__corner" aria-hidden="true"></div>
    <div
      v-for="label in weekdayLabels"
      :key="label"
      class="heatmap__weekday"
      role="columnheader"
      :aria-label="`День недели: ${label}`"
    >
      {{ label }}
    </div>
  </div>
</template>

<script lang="ts">
export default {
  name: 'WeekdayRow',
  props: {
    weekdayLabels: { type: Array as () => string[], required: true },
  },
};
</script>

<style scoped>
.heatmap__weekdays {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: var(--space-1);
  margin-bottom: var(--space-1);
}
.heatmap__corner {
  display: none;
}
.heatmap__weekday {
  text-align: center;
  color: var(--muted);
  font-size: 12px;
  font-weight: 600;
}
</style>
