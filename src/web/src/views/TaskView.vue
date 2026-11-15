<template>
  <section class="task" aria-labelledby="task-title">
    <header class="task__header">
      <router-link class="task__back" :to="'/'" @click.prevent="goBack">Назад к списку</router-link>
    </header>

    <p class="task__title" aria-live="polite">{{ title }}</p>

    <div v-if="loading" class="task__state" role="status" aria-live="polite">
      <span class="spinner" aria-hidden="true"></span> Загрузка…
    </div>

    <div v-else-if="error" class="task__errors" role="alert">
      <div class="alert alert--error">
        <div>Не удалось загрузить детали задачи</div>
        <button class="alert__retry" @click="onRetry">Повторить</button>
      </div>
      <router-link class="task__back" :to="'/'" @click.prevent="goBack">Назад к списку</router-link>
    </div>

    <template v-else-if="task">
      <article class="panel">
        <div class="panel__head">
          <h2 class="panel__title">Продолжительность</h2>
          <StatusBadge :status="task.status" />
        </div>
        <div class="task__duration">{{ formatDuration(task.duration_s) }}</div>
      </article>

      <article class="panel">
        <div class="panel__head">
          <h2 class="panel__title">Прогресс и время</h2>
          <span class="panel__count">{{ task.steps.length }}</span>
        </div>
        <table aria-label="Хронология действий">
          <thead>
            <tr>
              <th scope="col">Шаг</th>
              <th scope="col">Время</th>
              <th scope="col">Действие</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(step, index) in task.steps" :key="step.message_id">
              <td scope="row">{{ index + 1 }}</td>
              <td>{{ formatDateTime(step.at) }}</td>
              <td>{{ step.text }}</td>
            </tr>
          </tbody>
        </table>
      </article>

      <article class="panel">
        <div class="panel__head">
          <h2 class="panel__title">Решения</h2>
          <span class="panel__count">{{ task.solutions.proposed.length }}</span>
        </div>

        <div v-if="task.solutions.proposed.length === 0" class="task__empty">
          Решений не зафиксировано
        </div>

        <ul v-else class="solutions">
          <li
            v-for="(solution, index) in task.solutions.proposed"
            :key="index"
            class="solution"
            :class="{ 'solution--chosen': chosenId === solution.source_message_id }"
          >
            <div class="solution__head">
              <span class="solution__index">#{{ index + 1 }}</span>
              <span v-if="chosenId === solution.source_message_id" class="solution__tag" role="status">
                выбрано
              </span>
            </div>
            <p class="solution__text">{{ solution.text }}</p>
          </li>
        </ul>
      </article>

      <article class="panel">
        <div class="panel__head">
          <h2 class="panel__title">Токены</h2>
          <span class="panel__count">
            Итого: {{ task.tokens.total.total_tokens.toLocaleString('ru-RU') }}
          </span>
        </div>
        <table aria-label="Токены по моделям">
          <thead>
            <tr>
              <th scope="col">Модель</th>
              <th scope="col">Вход</th>
              <th scope="col">Выход</th>
              <th scope="col">Итого</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="model in task.tokens.by_model" :key="model.model">
              <td scope="row">{{ model.model }}</td>
              <td>{{ model.input_tokens.toLocaleString('ru-RU') }}</td>
              <td>{{ model.output_tokens.toLocaleString('ru-RU') }}</td>
              <td>{{ model.total_tokens.toLocaleString('ru-RU') }}</td>
            </tr>
          </tbody>
        </table>
      </article>
    </template>
  </section>
</template>

<script lang="ts">
import { onMounted, ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { tasksApi, ApiError } from '@/api/client';
import type { TaskDetailResponse, TaskStatus } from '@/api/types';
import StatusBadge from '@/components/dashboard/StatusBadge.vue';
import { formatDuration, formatDateTime } from '@/components/format';

export default {
  name: 'TaskView',
  components: { StatusBadge },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const task = ref<TaskDetailResponse | null>(null);
    const loading = ref(true);
    const error = ref<ApiError | null>(null);

    const id = computed(() => String(route.params.id));
    const title = computed(() => task.value?.name ?? '');
    const chosenId = computed(() => task.value?.solutions.chosen ?? null);

    async function load(): Promise<void> {
      loading.value = true;
      error.value = null;
      try {
        const data = await tasksApi.get(id.value);
        task.value = data;
      } catch (err) {
        if (err instanceof ApiError) {
          error.value = err;
        } else {
          error.value = new ApiError(500, 'SERVER_ERROR', 'Не удалось загрузить детали задачи');
        }
      } finally {
        loading.value = false;
      }
    }

    function goBack(): void {
      router.push('/');
    }

    function onRetry(): void {
      void load();
    }

    onMounted(load);

    return { task, loading, error, chosenId, title, formatDuration, formatDateTime, goBack, onRetry };
  },
};
</script>

<style scoped>
.task__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-3);
}
.task__title {
  margin: 0;
  font-size: 24px;
}
.task__back {
  font-weight: 600;
}
.task__errors {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}
.panel {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: var(--space-3);
  margin-bottom: var(--space-4);
}
.panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-3);
}
.panel__count {
  color: var(--muted);
  font-size: 14px;
}
.task__duration {
  font-size: 24px;
  font-weight: 700;
}
.task__state,
.task__empty {
  color: var(--muted);
}
.alert {
  background: var(--error-bg);
  color: var(--error);
  border: 1px solid var(--error);
  border-radius: var(--radius);
  padding: var(--space-3);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
}
.alert__retry {
  padding: var(--space-2) var(--space-3);
  border: 0;
  border-radius: var(--radius);
  background: var(--error);
  color: var(--surface);
  cursor: pointer;
}
.solutions {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}
.solution {
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: var(--space-3);
}
.solution--chosen {
  border-color: var(--primary);
}
.solution__head {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-2);
}
.solution__index {
  font-weight: 700;
}
.solution__tag {
  background: var(--primary);
  color: var(--primary-contrast);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius);
  font-size: 12px;
}
.solution__text {
  margin: 0;
  white-space: pre-wrap;
}
</style>
