<template>
  <div class="draw-mode-hud" @click="toggleDrawMode" title="Toggle Draw Mode" style="cursor: pointer;">
    <div class="draw-mode-slider" :class="modelValue"></div>
    <button 
      class="draw-mode-btn" 
      :class="{ active: modelValue === 'fill' }" 
      @click.stop="setDrawMode('fill')"
      title="Fill Mode"
      type="button"
    >
      <span class="mode-icon fill-icon"></span>
    </button>
    <button 
      class="draw-mode-btn" 
      :class="{ active: modelValue === 'x' }" 
      @click.stop="setDrawMode('x')"
      title="X Mark Mode"
      type="button"
    >
      <svg class="mode-icon x-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
        <path d="M18 6L6 18M6 6l12 12" stroke-width="3.5" stroke-linecap="round"/>
      </svg>
    </button>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  modelValue: 'fill' | 'x';
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: 'fill' | 'x'): void;
}>();

function toggleDrawMode() {
  emit('update:modelValue', props.modelValue === 'fill' ? 'x' : 'fill');
}

function setDrawMode(mode: 'fill' | 'x') {
  emit('update:modelValue', mode);
}
</script>

<style scoped>
.draw-mode-hud {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  background: rgba(15, 23, 42, 0.85);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  padding: 4px;
  border-radius: 9999px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
  z-index: 10;
  width: 80px;
  height: 36px;
  box-sizing: border-box;
  -webkit-tap-highlight-color: transparent;
  -webkit-touch-callout: none;
  user-select: none;
}

.draw-mode-slider {
  position: absolute;
  top: 4px;
  left: 4px;
  width: 36px;
  height: 28px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 9999px;
  transition: transform 0.25s cubic-bezier(0.25, 0.8, 0.25, 1);
  border: 1px solid rgba(255, 255, 255, 0.12);
  z-index: 1;
  box-sizing: border-box;
}

.draw-mode-slider.x {
  transform: translateX(36px);
}

.draw-mode-btn {
  background: none;
  border: none;
  margin: 0;
  padding: 0;
  width: 36px;
  height: 28px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  border-radius: 9999px;
  transition: all 0.2s ease;
  user-select: none;
  -webkit-tap-highlight-color: transparent;
  z-index: 2;
  box-sizing: border-box;
}

.mode-icon {
  transition: transform 0.2s ease;
  display: flex;
  justify-content: center;
  align-items: center;
}

.draw-mode-btn:hover .mode-icon {
  transform: scale(1.15);
}

.fill-icon {
  width: 14px;
  height: 14px;
  background: linear-gradient(135deg, #38bdf8 0%, #818cf8 100%);
  border-radius: 3px;
  display: block;
  box-shadow: 0 1px 3px rgba(56, 189, 248, 0.3);
}

.x-icon {
  width: 14px;
  height: 14px;
  stroke: #f43f5e;
  display: block;
}

@media (max-width: 768px) {
  .draw-mode-hud {
    bottom: 12px;
    left: 50%;
    transform: translateX(-50%);
  }
}
</style>
