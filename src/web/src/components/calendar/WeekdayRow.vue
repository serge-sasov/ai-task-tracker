<template>
  <div class="heatmap__weekdays" role="rowgroup">
    <div class="heatmap__corner" aria-hidden="true"></div>
    <div
      v-for="label in weekdayLabels"
      :key="label"
      class="heatmap__weekday"
      role="columnheader"
      :aria-label="`Неделя: ${label}`"
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
.heatmap__corner {
  width: var(--space-3);
}
.heatmap__weekday {
  text-align: center;
  color: var(--muted);
  font-size: 12px;
  font-weight: 600;
  width: var(--space-5);
}
</style>
