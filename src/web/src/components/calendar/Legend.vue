<template>
  <div class="legend" role="group" aria-label="Условные обозначения">
    <span class="legend__title">Задачи:</span>
    <div class="legend__items">
      <span class="legend__item">
        <span class="legend__cell legend__cell--0" aria-hidden="true"></span>
        Нет
      </span>
      <span class="legend__item">
        <span class="legend__cell legend__cell--1" aria-hidden="true"></span>
        1
      </span>
      <span class="legend__item">
        <span class="legend__cell legend__cell--2" aria-hidden="true"></span>
        2
      </span>
      <span class="legend__item">
        <span class="legend__cell legend__cell--3" aria-hidden="true"></span>
        3+
      </span>
    </div>
  </div>
</template>

<script lang="ts">
export default {
  name: 'Legend',
};
</script>

<style scoped>
.legend {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  color: var(--muted);
  font-size: 12px;
  margin-top: var(--space-3);
}
.legend__title {
  color: var(--muted);
}
.legend__items {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}
.legend__item {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}
.legend__cell {
  width: var(--space-3);
  height: var(--space-3);
  border-radius: 2px;
}
.legend__cell--0 {
  background: var(--surface);
  border: 1px solid var(--border);
}
.legend__cell--1 {
  background: var(--primary);
  opacity: 0.35;
}
.legend__cell--2 {
  background: var(--primary);
  opacity: 0.65;
}
.legend__cell--3 {
  background: var(--primary);
  opacity: 1;
}
</style>
