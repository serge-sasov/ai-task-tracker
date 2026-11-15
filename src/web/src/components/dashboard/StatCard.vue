<template>
  <div class="stat-card" role="group" :aria-label="title">
    <div class="stat-card__value">{{ value }}</div>
    <div class="stat-card__label">{{ title }}</div>
  </div>
</template>

<script lang="ts">
export default {
  name: 'StatCard',
  props: {
    title: { type: String, required: true },
    value: { type: [String, Number], required: true },
  },
};
</script>
