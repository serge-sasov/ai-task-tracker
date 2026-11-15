import { registerView } from '../router';
registerView('DashboardView', () => import('./DashboardView.vue'));
registerView('TaskView', () => import('./TaskView.vue'));
registerView('SearchView', () => import('./SearchView.vue'));
