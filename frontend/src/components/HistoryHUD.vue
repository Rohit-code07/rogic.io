<template>
  <div class="history-hud">
    <button 
      class="history-btn" 
      @click="$emit('undo')" 
      :disabled="!canUndo" 
      title="Undo (Ctrl+Z)" 
      type="button"
    >
      <svg class="history-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M3 7v6h6M21 17a9 9 0 0 0-9-9 9 9 0 0 0-6 2.3L3 13" />
      </svg>
    </button>
    <button 
      class="history-btn" 
      @click="$emit('redo')" 
      :disabled="!canRedo" 
      title="Redo (Ctrl+Y)" 
      type="button"
    >
      <svg class="history-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <path d="M21 7v6h-6M3 17a9 9 0 0 1 9-9 9 9 0 0 1 6 2.3l3 2.7" />
      </svg>
    </button>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  canUndo: boolean;
  canRedo: boolean;
}>();

defineEmits<{
  (e: 'undo'): void;
  (e: 'redo'): void;
}>();
</script>

<style scoped>
.history-hud {
  position: absolute;
  bottom: 20px;
  left: 20px;
  display: flex;
  align-items: center;
  gap: 0.25rem;
  background: rgba(15, 23, 42, 0.85);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  padding: 4px;
  border-radius: 9999px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
  z-index: 10;
  height: 36px;
  box-sizing: border-box;
}

.history-btn {
  background: none;
  border: none;
  margin: 0;
  padding: 0;
  width: 28px;
  height: 28px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  border-radius: 9999px;
  transition: all 0.2s ease;
  color: #94a3b8;
}

.history-btn:hover:not(:disabled) {
  background-color: rgba(255, 255, 255, 0.08);
  color: #f8fafc;
}

.history-btn:disabled {
  color: #475569;
  cursor: not-allowed;
}

.history-icon {
  width: 14px;
  height: 14px;
}

@media (max-width: 768px) {
  .history-hud {
    bottom: 12px;
    left: 12px;
  }
}
</style>
