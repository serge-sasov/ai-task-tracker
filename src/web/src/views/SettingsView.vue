<template>
  <section class="settings" aria-labelledby="settings-title">
    <header class="settings__header">
      <h1 id="settings-title">Управление данными</h1>
      <p class="settings__updated" aria-live="polite">{{ updatedLabel }}</p>
    </header>

    <div class="panel">
      <div class="settings__groups">
        <div
          v-for="group in groups"
          :key="group.label"
          class="group"
          role="group"
          :aria-label="`${group.label}: ${group.count}`"
        >
          <span class="group__label">{{ group.label }}</span>
          <span class="group__value">{{ group.count }}</span>
        </div>
      </div>

      <div class="settings__counter">
        <StatCard title="Токенов" :value="formatNumber(tokenTotal)" />
      </div>

      <button
        v-if="deleting"
        class="settings__delete settings__delete--busy"
        type="button"
        aria-busy="true"
        disabled
      >
        <span class="spinner" aria-hidden="true"></span> Удаление…
      </button>
      <button
        v-else
        class="settings__delete"
        type="button"
        @click="onDeleteClick"
      >
        Удалить историю
      </button>

      <div v-if="empty" class="settings__empty" role="status" aria-live="polite">
        Данных для удаления нет.
      </div>

      <div v-else-if="error" class="settings__error alert alert--error" role="alert">
        <div>Не удалось удалить историю. Повторите попытку.</div>
        <button class="alert__retry" type="button" @click="onRetry">Повторить</button>
      </div>

      <div v-else-if="success" class="settings__success alert alert--success" role="status" aria-live="polite">
        История удалена.
      </div>
    </div>

    <Dialog
      :open="dialogOpen"
      :title="dialogTitle"
      @close="dialogOpen = false"
      @confirm="onConfirmDelete"
    >
      <p class="settings__warning alert alert--warn">
        Действие необратимо: все задачи, действия, решения и токены будут удалены.
      </p>
      <p class="settings__body">{{ dialogText }}</p>
    </Dialog>
  </section>
</template>

<script lang="ts">
import { onMounted, ref, computed } from 'vue';
import { useTasksStore } from '@/stores/tasks';
import { useSettingsStore } from '@/stores/settings';
import StatCard from '@/components/dashboard/StatCard.vue';
import Dialog from '@/components/shared/Dialog.vue';

export default {
  name: 'SettingsView',
  components: { StatCard, Dialog },
  setup() {
    const tasks = useTasksStore();
    const settings = useSettingsStore();

    const dialogOpen = ref(false);
    const dialogTitle = ref('Удалить историю');
    const dialogText = ref('');
    const lastConfirmed = ref(false);

    const groups = computed(() => [
      { label: 'Ожидают', count: tasks.items.filter((t) => t.status === 'Не завершена').length },
      { label: 'В процессе', count: tasks.items.filter((t) => t.status === 'Прервана').length },
      { label: 'Завершены', count: tasks.items.filter((t) => t.status === 'Завершена').length },
    ]);

    const tokenTotal = computed(() =>
      tasks.items.reduce((acc, t) => acc + (typeof t.tokens_total === 'number' ? t.tokens_total : 0), 0),
    );

    const deleting = computed(() => settings.deleting);
    const error = computed(() => settings.error);
    const success = computed(() => settings.result?.deleted === true);
    const empty = computed(() => settings.message === 'Данных для удаления нет');

    const updatedLabel = computed(() =>
      settings.result ? 'Обновлено ' + new Date().toLocaleTimeString('ru-RU') : '',
    );

    function onConfirmDelete(): void {
      lastConfirmed.value = true;
      void settings.deleteHistory(true);
      dialogOpen.value = false;
    }
    function onDeleteClick(): void {
      if (deleting.value) return;
      dialogText.value = 'Повторный клик по кнопке «Подтвердить» не запускает вторую операцию.';
      dialogOpen.value = true;
    }
    function onRetry(): void {
      void settings.deleteHistory(true);
    }

    function formatNumber(n: number): string {
      return n.toLocaleString('ru-RU');
    }

    onMounted(() => {
      void tasks.load({ size: 100 });
    });

    return {
      tasks,
      settings,
      groups,
      tokenTotal,
      deleting,
      error,
      success,
      empty,
      updatedLabel,
      dialogOpen,
      dialogTitle,
      dialogText,
      onDeleteClick,
      onConfirmDelete,
      onRetry,
      formatNumber,
    };
  },
};
</script>

<style scoped>
.settings__header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: var(--space-3);
  margin-bottom: var(--space-3);
}
.settings__updated {
  color: var(--muted);
  font-size: 14px;
}
.panel {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: var(--space-3);
}
.settings__groups {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}
.group {
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: var(--space-3);
}
.group__label {
  color: var(--muted);
  font-size: 13px;
}
.group__value {
  font-size: 24px;
  font-weight: 700;
}
.settings__counter {
  margin-bottom: var(--space-4);
}
.settings__delete {
  border: 0;
  border-radius: var(--radius);
  background: var(--error);
  color: var(--surface);
  padding: var(--space-3) var(--space-4);
  cursor: pointer;
  font-size: 14px;
}
.settings__delete--busy {
  opacity: 0.7;
  cursor: progress;
}
.settings__empty,
.settings__error,
.settings__success {
  margin-top: var(--space-3);
  color: var(--muted);
}
.settings__warning {
  margin: 0 0 var(--space-3);
}
.settings__body {
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
