<template>
  <div class="app-shell">
    <header class="app-topbar" role="banner">
      <div class="app-brand">
        <strong>ai-task-tracker</strong>
      </div>
    </header>

    <div class="app-body">
      <nav class="app-sidebar" aria-label="Основная навигация">
        <ul class="app-nav">
          <li>
            <router-link to="/">Задачи</router-link>
          </li>
          <li>
            <router-link to="/calendar">Календарь</router-link>
          </li>
          <li>
            <router-link to="/search">Поиск</router-link>
          </li>
          <li>
            <router-link to="/tokens">Токены</router-link>
          </li>
          <li>
            <router-link to="/settings">Управление данными</router-link>
          </li>
        </ul>
      </nav>

      <main class="app-main" role="main">
        <div class="app-inner">
          <router-view v-slot="{ Component }">
            <component :is="Component" />
          </router-view>
        </div>
      </main>
    </div>
  </div>
</template>

<script lang="ts">
export default {
  name: 'AppShell',
};
</script>
