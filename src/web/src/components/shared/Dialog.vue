<template>
  <div
    v-if="open"
    class="dialog__overlay"
    role="presentation"
    @click.self="emitClose"
  >
    <div
      class="dialog__window"
      role="dialog"
      aria-modal="true"
      :aria-label="title"
      @keyup.escape="emitClose"
    >
      <header class="dialog__head">
        <h2 class="dialog__title">{{ title }}</h2>
        <button
          class="dialog__close"
          type="button"
          aria-label="Закрыть диалог"
          @click="emitClose"
        >
          ×
        </button>
      </header>
      <div class="dialog__body">
        <slot />
      </div>
      <footer class="dialog__foot">
        <button class="dialog__cancel" type="button" @click="emitClose">Отмена</button>
        <button class="dialog__confirm" type="button" @click="emitConfirm">Подтвердить</button>
      </footer>
    </div>
  </div>
</template>

<script lang="ts">
import { ref, onMounted } from 'vue';

export default {
  name: 'Dialog',
  props: {
    open: { type: Boolean, required: true },
    title: { type: String, required: true },
  },
  emits: ['close', 'confirm'],
  setup(props, { emit }) {
    const focusReturn = ref<HTMLElement | null>(null);

    onMounted(() => {
      focusReturn.value = document.activeElement as HTMLElement | null;
    });

    function emitClose(): void {
      emit('close');
      // Return focus to the element that opened the dialog.
      if (focusReturn.value && typeof focusReturn.value.focus === 'function') {
        focusReturn.value.focus();
      }
    }

    function emitConfirm(): void {
      emit('confirm');
    }

    return { emitClose, emitConfirm };
  },
};
</script>

<style scoped>
.dialog__overlay {
  position: fixed;
  inset: 0;
  background: rgba(28, 37, 48, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.dialog__window {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  min-width: min(280px, 90vw);
  padding: var(--space-3);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}
.dialog__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-2);
}
.dialog__title {
  margin: 0;
  font-size: 16px;
}
.dialog__close {
  border: 0;
  background: transparent;
  color: var(--text);
  font-size: 20px;
  cursor: pointer;
  line-height: 1;
}
.dialog__body {
  margin-bottom: var(--space-3);
}
.dialog__foot {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-2);
}
.dialog__cancel,
.dialog__confirm {
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius);
  font-size: 14px;
  cursor: pointer;
}
.dialog__cancel {
  border: 1px solid var(--border);
  background: var(--surface);
  color: var(--text);
}
.dialog__confirm {
  border: 0;
  background: var(--primary);
  color: var(--primary-contrast);
}
</style>
