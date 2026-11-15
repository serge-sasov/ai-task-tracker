<template>
  <section class="search" aria-labelledby="search-title">
    <header class="search__header">
      <h1 id="search-title">Результаты AI-поиска</h1>
    </header>

    <form class="search__form" @submit.prevent="onSearch" role="search">
      <label for="search-query" class="search__label">Поиск по задачам</label>
      <div class="search__field">
        <input
          id="search-query"
          class="search__input"
          type="search"
          v-model="query"
          :aria-invalid="!!search.validationError"
          :aria-describedby="search.validationError ? 'search-validation' : undefined"
          placeholder="Поиск по задачам"
        />
        <button
          class="search__button"
          type="submit"
          :disabled="!search.isValid || loading"
        >
          Найти
        </button>
      </div>
      <p v-if="search.validationError" id="search-validation" class="search__validation" role="alert">
        {{ search.validationError }}
      </p>
    </form>

    <div v-if="loading" class="search__state" role="status" aria-live="polite">
      <span class="spinner" aria-hidden="true"></span> Ищем по задачам…
    </div>

    <div v-else-if="error" class="search__errors" role="alert">
      <div class="alert alert--error">
        AI-поisk недоступен: LLM не отвечает. Попробуйте позже.
        <button class="alert__retry" @click="onRetry">Повторить</button>
      </div>
    </div>

    <div v-else-if="results.length === 0" class="search__state">
      Ничего не найдено. Попробуйте изменить запрос.
    </div>

    <ul v-else class="results" aria-label="Результаты AI-поиска">
      <li
        v-for="(result, index) in results"
        :key="index"
        class="result"
      >
        <div class="result__head">
          <span class="result__index">#{{ index + 1 }}</span>
          <span class="result__score">Релевантность: {{ formatScore(result.score) }}</span>
        </div>

        <router-link class="result__task" :to="`/tasks/${result.task_id}`">
          Задача {{ result.task_id }}
        </router-link>

        <p class="result__snippet">{{ result.text }}</p>

        <span class="result__kind">{{ result.kind === 'solution' ? 'решение' : 'шаг' }}</span>
      </li>
    </ul>
  </section>
</template>

<script lang="ts">
import { ref, computed } from 'vue';
import { useSearchStore } from '@/stores/search';
import { formatScore } from '@/components/format';
import type { SearchHit } from '@/api/types';

export default {
  name: 'SearchView',
  setup() {
    const search = useSearchStore();
    const loading = ref(search.loading);
    const error = ref(search.error);
    const query = ref(search.query);
    const results = computed<SearchHit[]>(() => search.results);

    function onSearch(): void {
      if (!search.isValid) {
        search.setQuery(query.value);
        return;
      }
      void search.run(query.value);
      loading.value = true;
    }

    function onRetry(): void {
      if (query.value.trim()) void search.run(query.value);
    }

    return { search, loading, error, query, results, formatScore, onSearch, onRetry };
  },
};
</script>

<style scoped>
.search__form {
  margin-bottom: var(--space-4);
}
.search__label {
  display: block;
  font-weight: 600;
  margin-bottom: var(--space-2);
}
.search__field {
  display: flex;
  gap: var(--space-2);
}
.search__input {
  flex: 1;
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--border);
  border-radius: var(--radius);
}
.search__button {
  padding: var(--space-2) var(--space-3);
  border: 0;
  border-radius: var(--radius);
  background: var(--primary);
  color: var(--primary-contrast);
  cursor: pointer;
}
.search__button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
.search__validation {
  color: var(--error);
  margin: var(--space-2) 0 0;
}
.search__state {
  color: var(--muted);
}
.search__errors {
  margin-bottom: var(--space-4);
}
.alert {
  background: var(--error-bg);
  color: var(--error);
  border: 1px solid var(--error);
  border-radius: var(--radius);
  padding: var(--space-3);
}
.alert__retry {
  margin-top: var(--space-2);
  padding: var(--space-2) var(--space-3);
  border: 0;
  border-radius: var(--radius);
  background: var(--error);
  color: var(--surface);
  cursor: pointer;
}
.results {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}
.result {
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: var(--space-3);
}
.result__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-2);
}
.result__index {
  font-weight: 700;
}
.result__score {
  color: var(--primary);
  font-weight: 600;
}
.result__task {
  font-weight: 700;
}
.result__snippet {
  margin: 0 0 var(--space-2);
  white-space: pre-wrap;
}
.result__kind {
  color: var(--muted);
  font-size: 13px;
}
</style>
