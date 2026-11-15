import { registerView } from '../router';
registerView('CalendarView', () => import('./CalendarView.vue'));
registerView('HistoryView', () => import('./HistoryView.vue'));
registerView('SettingsView', () => import('./SettingsView.vue'));
