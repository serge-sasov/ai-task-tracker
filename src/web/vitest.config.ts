/// <reference types="vitest" />
import { fileURLToPath } from 'node:url';
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

// Isolated frontend component test runner. Uses jsdom for DOM rendering and
// @vue/test-utils for component mounting. Never touches backend, network or
// production endpoints; all test data is inline.
export default defineConfig({
  plugins: [vue()],
  base: '/',
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    include: ['tests/**/*.spec.ts'],
    exclude: ['tests/**/node_modules/**'],
    coverage: {
      provider: 'v8',
      include: ['src/**/*.vue', 'src/**/*.ts'],
      exclude: ['src/**/*.d.ts', 'tests/**'],
    },
  },
});
