<template>
  <span class="status-badge" :class="`status-badge--${status}`">
    <span class="status-badge__dot" aria-hidden="true"></span>
    <span class="status-badge__label">{{ status }}</span>
  </span>
</template>

<script lang="ts">
import type { TaskStatus } from '@/api/types';

export default {
  name: 'StatusBadge',
  props: {
    status: { type: String as () => TaskStatus, required: true },
  },
};
</script>
