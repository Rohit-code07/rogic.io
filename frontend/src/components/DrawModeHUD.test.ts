import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import DrawModeHUD from './DrawModeHUD.vue';

describe('DrawModeHUD', () => {
  it('should toggle to x when fill button is clicked while modelValue is fill', async () => {
    const wrapper = mount(DrawModeHUD, {
      props: {
        modelValue: 'fill'
      }
    });

    const fillButton = wrapper.findAll('.draw-mode-btn')[0];
    await fillButton.trigger('click');

    const emitted = wrapper.emitted('update:modelValue');
    expect(emitted).toBeTruthy();
    expect(emitted?.[0]).toEqual(['x']);
  });

  it('should update to x when x button is clicked while modelValue is fill', async () => {
    const wrapper = mount(DrawModeHUD, {
      props: {
        modelValue: 'fill'
      }
    });

    const xButton = wrapper.findAll('.draw-mode-btn')[1];
    await xButton.trigger('click');

    const emitted = wrapper.emitted('update:modelValue');
    expect(emitted).toBeTruthy();
    expect(emitted?.[0]).toEqual(['x']);
  });

  it('should toggle to fill when x button is clicked while modelValue is x', async () => {
    const wrapper = mount(DrawModeHUD, {
      props: {
        modelValue: 'x'
      }
    });

    const xButton = wrapper.findAll('.draw-mode-btn')[1];
    await xButton.trigger('click');

    const emitted = wrapper.emitted('update:modelValue');
    expect(emitted).toBeTruthy();
    expect(emitted?.[0]).toEqual(['fill']);
  });

  it('should update to fill when fill button is clicked while modelValue is x', async () => {
    const wrapper = mount(DrawModeHUD, {
      props: {
        modelValue: 'x'
      }
    });

    const fillButton = wrapper.findAll('.draw-mode-btn')[0];
    await fillButton.trigger('click');

    const emitted = wrapper.emitted('update:modelValue');
    expect(emitted).toBeTruthy();
    expect(emitted?.[0]).toEqual(['fill']);
  });
});
