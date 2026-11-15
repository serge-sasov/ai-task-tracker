import {
  createRouter,
  createWebHistory,
  type RouteRecordRaw,
} from 'vue-router';

// Runtime view registry. Later chunks (C6b/C6c) register their views here via
// registerView(); the router resolves routes from this registry at runtime
// without statically referencing view files that don't exist yet, so
// `npm run build` succeeds in this chunk.
const registry: Record<string, () => Promise<unknown>> = {};

export function registerView(
  key: string,
  factory: () => Promise<unknown>,
): void {
  registry[key] = factory;
}

function resolveView(key: string): () => Promise<unknown> {
  return () => {
    const factory = registry[key];
    if (!factory) {
      return Promise.resolve({});
    }
    return Promise.resolve(factory());
  };
}

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'dashboard',
    component: () => resolveView('DashboardView')(),
    meta: { title: 'Задачи' },
  },
  {
    path: '/tasks/:id',
    name: 'task',
    component: () => resolveView('TaskView')(),
    props: true,
    meta: { title: 'Детали задачи' },
  },
  {
    path: '/search',
    name: 'search',
    component: () => resolveView('SearchView')(),
    meta: { title: 'Поиск' },
  },
  {
    path: '/calendar',
    name: 'calendar',
    component: () => resolveView('CalendarView')(),
    meta: { title: 'Календарь' },
  },
  {
    path: '/tokens',
    name: 'tokens',
    component: () => resolveView('HistoryView')(),
    meta: { title: 'Токены' },
  },
  {
    path: '/settings',
    name: 'settings',
    component: () => resolveView('SettingsView')(),
    meta: { title: 'Управление данными' },
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
