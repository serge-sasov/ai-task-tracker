<template>
  <span
    class="model-cell"
    :title="fullLabel"
    :aria-label="fullLabel"
  >
    <span class="model-cell__primary" :title="fullLabel">{{ primary }}</span>
    <span v-if="moreCount" class="model-cell__more">+{{ moreCount }}</span>
  </span>
</template>

<script lang="ts">
import { computed } from 'vue';

export default {
  name: 'ModelCell',
  props: {
    models: { type: Array as () => string[], default: () => [] },
    modelLabelPrefix: { type: String as () => string, default: 'Модель' },
  },
  setup(props) {
    const models = computed(() => props.models ?? []);

    const primary = computed(
      () => models.value.length > 0 ? models.value[0] : 'Нет данных',
    );

    const moreCount = computed(
      () => (models.value.length > 1 ? models.value.length - 1 : 0),
    );

    const fullLabel = computed(() => {
      const list = models.value.join(', ');
      return `${props.modelLabelPrefix}: ${list || 'Нет данных'}`;
    });

    return { primary, moreCount, fullLabel };
  },
};
</script>

<style scoped>
.model-cell {
  display: inline-flex;
  align-items: center;
  min-width: 0;
}
.model-cell__primary {
  display: inline-block;
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.model-cell__more {
  color: var(--muted);
  font-size: 12px;
  margin-left: var(--space-1);
}
</style>
