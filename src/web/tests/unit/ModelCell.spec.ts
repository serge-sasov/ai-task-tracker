import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import ModelCell from '@/components/ModelCell.vue';

describe('ModelCell (shared S-01/S-04 model cell)', () => {
  it('shows «Нет данных» and «Модель: Нет данных» aria-label when empty', () => {
    const wrapper = mount(ModelCell, { props: { models: [] } });
    expect(wrapper.text()).toContain('Нет данных');
    expect(wrapper.text()).not.toContain('+');
    expect(wrapper.attributes('title')).toBe('Модель: Нет данных');
    expect(wrapper.attributes('aria-label')).toBe('Модель: Нет данных');
  });

  it('shows primary model for a single entry', () => {
    const wrapper = mount(ModelCell, { props: { models: ['qwen3.8:27b'] } });
    expect(wrapper.text()).toContain('qwen3.8:27b');
    expect(wrapper.find('.model-cell__more').exists()).toBe(false);
    expect(wrapper.attributes('title')).toBe('Модель: qwen3.8:27b');
    expect(wrapper.attributes('aria-label')).toBe('Модель: qwen3.8:27b');
  });

  it('shows primary + «+N» badge with full ordered list in title/aria-label', () => {
    const wrapper = mount(ModelCell, {
      props: { models: ['qwen3.8:27b', 'llama3.1:8b'] },
    });
    expect(wrapper.text()).toContain('qwen3.8:27b');
    expect(wrapper.find('.model-cell__more').text()).toContain('+1');
    expect(wrapper.find('.model-cell__more').exists()).toBe(true);
    expect(wrapper.attributes('title')).toBe('Модель: qwen3.8:27b, llama3.1:8b');
    expect(wrapper.attributes('aria-label')).toBe('Модель: qwen3.8:27b, llama3.1:8b');
  });

  it('supports custom label prefix (e.g. S-04 "Модели")', () => {
    const wrapper = mount(ModelCell, {
      props: { models: ['a', 'b'], modelLabelPrefix: 'Модели' },
    });
    expect(wrapper.attributes('title')).toBe('Модели: a, b');
    expect(wrapper.attributes('aria-label')).toBe('Модели: a, b');
  });

  it('handles a very long primary name with ellipsis class but full title', () => {
    const longName = 'x'.repeat(200);
    const wrapper = mount(ModelCell, { props: { models: [longName] } });
    expect(wrapper.attributes('title')).toBe(`Модель: ${longName}`);
    expect(wrapper.find('.model-cell__primary').attributes('title')).toBe(
      `Модель: ${longName}`,
    );
  });
});
