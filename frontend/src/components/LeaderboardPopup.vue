<template>
  <!-- Popup for Leaderboard (Semi-transparent absolute modal) -->
  <div v-show="open" class="leaderboard-popup-overlay" @click.self="open = false">
    <div class="leaderboard-popup-content">
      <div class="leaderboard-popup-header">
        <h3 class="leaderboard-popup-title">🏆 Global Leaderboard</h3>
        <button class="leaderboard-popup-close" @click="open = false">&times;</button>
      </div>
      <div class="leaderboard-scrollable">
        <ul class="leaderboard-list">
          <li v-for="(user, index) in rankings" :key="user.id" class="leaderboard-item">
            <span class="rank">{{ index + 1 }}</span>
            <span class="username">{{ user.username }}</span>
            <span class="level">Lv.{{ user.level }}</span>
            <span class="xp">{{ user.xp }} XP</span>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { User } from '../api/userApi';

const open = defineModel<boolean>('open', { required: true });

defineProps<{
  rankings: User[];
}>();
</script>

<style scoped>
.leaderboard-popup-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(15, 23, 42, 0.6);
  backdrop-filter: blur(4px);
  animation: fade-in 0.25s ease-out;
}

.leaderboard-popup-content {
  background: rgba(30, 41, 59, 0.85);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 1.5rem;
  width: 340px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.55);
  animation: slide-up-anim 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.leaderboard-popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  padding-bottom: 0.75rem;
  margin-bottom: 1rem;
}

.leaderboard-popup-title {
  margin: 0;
  color: #fbbf24;
  font-size: 1.2rem;
  font-weight: 700;
}

.leaderboard-popup-close {
  background: transparent;
  border: none;
  color: #94a3b8;
  font-size: 1.5rem;
  cursor: pointer;
  transition: color 0.15s ease;
}

.leaderboard-popup-close:hover {
  color: #f8fafc;
}

.leaderboard-scrollable {
  flex-grow: 1;
  overflow-y: auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding-right: 4px;
}

.leaderboard-scrollable::-webkit-scrollbar {
  width: 6px;
}

.leaderboard-scrollable::-webkit-scrollbar-track {
  background: transparent;
}

.leaderboard-scrollable::-webkit-scrollbar-thumb {
  background: #334155;
  border-radius: 3px;
}

.leaderboard-scrollable::-webkit-scrollbar-thumb:hover {
  background: #475569;
}

.leaderboard-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.leaderboard-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.4rem 0.6rem;
  background-color: #0f172a;
  border: 1px solid #1e293b;
  border-radius: 8px;
  font-weight: 600;
  font-size: 0.85rem;
}

.leaderboard-item .rank {
  color: #64748b;
  width: 16px;
  text-align: center;
}

.leaderboard-item:nth-child(1) .rank { color: #fbbf24; }
.leaderboard-item:nth-child(2) .rank { color: #94a3b8; }
.leaderboard-item:nth-child(3) .rank { color: #b45309; }

.leaderboard-item .username {
  flex-grow: 1;
  color: #f8fafc;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.leaderboard-item .level {
  color: #38bdf8;
  font-size: 0.75rem;
  background-color: rgba(56, 189, 248, 0.1);
  padding: 0.05rem 0.3rem;
  border-radius: 4px;
}

.leaderboard-item .xp {
  color: #818cf8;
  font-size: 0.75rem;
}

@keyframes fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slide-up-anim {
  from { transform: translateY(20px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}
</style>
