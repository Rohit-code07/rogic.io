<template>
  <!-- Mypage Popup Overlay (Clicking outside the modal card closes it and returns to 'play') -->
  <div class="mypage-popup-overlay" @click.self="$emit('close')" style="position: fixed; top: 0; left: 0; right: 0; bottom: 0; display: flex; justify-content: center; align-items: center; background: rgba(15, 23, 42, 0.75); backdrop-filter: blur(8px); z-index: 9999;">
    <div class="mypage-popup-content" style="position: relative; width: 480px; max-width: 95vw; height: 500px; background: #1e293b; border: 1px solid rgba(255,255,255,0.08); border-radius: 20px; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.5), 0 10px 10px -5px rgba(0, 0, 0, 0.4); padding: 2rem; display: flex; flex-direction: column; justify-content: flex-start; box-sizing: border-box; animation: modalFadeIn 0.25s cubic-bezier(0.16, 1, 0.3, 1);">
      
      <!-- Close Button (X) at Top Right (Always present to escape back to play) -->
      <button 
        @click="$emit('close')" 
        class="mypage-popup-close-btn"
        style="position: absolute; top: 1.25rem; right: 1.25rem; width: 32px; height: 32px; border-radius: 50%; background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.08); color: #94a3b8; font-size: 1.25rem; line-height: 1; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: all 0.2s;"
        onmouseover="this.style.background='rgba(255,255,255,0.08)'; this.style.color='#f8fafc';"
        onmouseout="this.style.background='rgba(255,255,255,0.03)'; this.style.color='#94a3b8';"
      >
        &times;
      </button>

      <!-- VIEW 1: Review Mode (Displaying the selected completed puzzle board) -->
      <div v-if="isReviewMode && modalBoard" class="mypage-review-view" style="display: flex; flex-direction: column; width: 100%; height: 100%; box-sizing: border-box; padding-top: 1.25rem; justify-content: flex-start; align-items: center; animation: modalFadeIn 0.2s ease-out;">
        <!-- Absolute Position Back Button (Perfect symmetry with X button at top 1.25rem) -->
        <button 
          @click="closeReview" 
          class="mypage-popup-back-btn"
          style="position: absolute; top: 1.25rem; left: 1.25rem; width: 32px; height: 32px; border-radius: 50%; background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.08); color: #38bdf8; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: all 0.2s;"
          onmouseover="this.style.background='rgba(255,255,255,0.08)'; this.style.color='#f8fafc';"
          onmouseout="this.style.background='rgba(255,255,255,0.03)'; this.style.color='#38bdf8';"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <line x1="19" y1="12" x2="5" y2="12"></line>
            <polyline points="12 19 5 12 12 5"></polyline>
          </svg>
        </button>
        
        <!-- 2. Middle Row: Completed Puzzle Canvas (Naturally centered) -->
        <div class="modal-canvas-wrapper" style="width: 300px; height: 300px; margin-top: auto; margin-bottom: auto; background-color: #0f172a; border-radius: 12px; overflow: hidden; border: 1px solid rgba(255, 255, 255, 0.08); display: flex; justify-content: center; align-items: center; position: relative; box-shadow: 0 4px 12px rgba(0,0,0,0.25);">
          <NonogramCanvas :board="modalBoard" :readOnly="true" :initialAngle="0" />
        </div>

        <!-- 3. Bottom Row: Date, Stage name, Time badge (Perfect width mapping) -->
        <div style="display: flex; align-items: center; justify-content: space-between; width: 100%; border-top: 1px solid rgba(255, 255, 255, 0.05); padding-top: 0.85rem; margin-top: 0.25rem; margin-bottom: -0.65rem; text-align: left;">
          <div style="display: flex; align-items: center; gap: 0.5rem;">
            <span style="font-size: 0.75rem; font-weight: 500; color: #64748b;">{{ formatClearedAt(selectedHistory?.clearedAt) }}</span>
            <span style="font-weight: 700; color: #f8fafc; font-size: 1.05rem;">{{ selectedHistory?.stageName }}</span>
          </div>
          <span style="font-size: 0.7rem; font-weight: 600; padding: 0.18rem 0.45rem; background: rgba(56, 189, 248, 0.1); border: 1px solid rgba(56, 189, 248, 0.15); border-radius: 5px; color: #38bdf8;">{{ selectedHistory?.elapsedTime }}s</span>
        </div>
      </div>

      <!-- VIEW 2: Default Profile & History List View -->
      <div v-else class="mypage-default-view" style="display: flex; flex-direction: column; gap: 1.25rem; width: 100%; height: 100%; justify-content: flex-start;">
        <!-- Non-login Guest Welcome Section -->
        <div v-if="!currentUser" class="mypage-guest-view" style="display: flex; flex-direction: column; align-items: center; justify-content: center; text-align: center; padding: 1.5rem 1rem; background: rgba(30, 41, 59, 0.3); border: 1px solid rgba(255,255,255,0.05); border-radius: 12px;">
          <div class="guest-icon" style="font-size: 2.5rem; margin-bottom: 0.75rem;">🎮</div>
          <h3 style="font-weight: 700; color: #f8fafc; margin-bottom: 0.5rem; font-size: 1.25rem; margin-top: 0;">Guest Mode Active</h3>
          <p style="color: #94a3b8; max-width: 320px; font-size: 0.85rem; line-height: 1.5; margin-bottom: 1.25rem; margin-top: 0;">
            You are playing as a guest. Clear records and history won't be saved on the server. Sign in to capture your achievements permanently!
          </p>
          <button 
            @click="$emit('login')" 
            class="google-login-btn"
            style="display: flex; align-items: center; justify-content: center; gap: 0.75rem; padding: 0.75rem 1.5rem; background: #ffffff; color: #0f172a; font-weight: 600; border: none; border-radius: 10px; cursor: pointer; transition: all 0.2s; box-shadow: 0 4px 12px rgba(0,0,0,0.15);"
          >
            <svg width="18" height="18" viewBox="0 0 18 18">
              <path fill="#4285F4" d="M17.64 9.2c0-.63-.06-1.25-.16-1.84H9v3.47h4.84c-.21 1.12-.84 2.07-1.79 2.7v2.24h2.9c1.7-1.57 2.69-3.88 2.69-6.57z"/>
              <path fill="#34A853" d="M9 18c2.43 0 4.47-.8 5.96-2.23l-2.9-2.24c-.8.54-1.84.87-3.06.87-2.35 0-4.34-1.59-5.05-3.73H.95v2.3C2.43 15.89 5.5 18 9 18z"/>
              <path fill="#FBBC05" d="M3.95 10.66A5.4 5.4 0 0 1 3.6 9c0-.58.1-1.15.27-1.66V5.04H.95A9.02 9.02 0 0 0 0 9c0 1.45.35 2.82.95 4.04l3-2.38z"/>
              <path fill="#EA4335" d="M9 3.58c1.32 0 2.5.45 3.44 1.35L15 2.4C13.46.96 11.43 0 9 0 5.5 0 2.43 2.11.95 5.04l3 2.38C4.66 5.17 6.65 3.58 9 3.58z"/>
            </svg>
            Sign in with Google
          </button>
        </div>

        <!-- Logged-in User Dashboard -->
        <div v-else class="mypage-dashboard-content" style="display: flex; flex-direction: column; gap: 1.25rem; width: 100%;">
          <!-- Profile Info Card -->
          <div class="mypage-user-profile" style="display: flex; align-items: center; gap: 1.25rem; padding: 1.25rem; background: rgba(30, 41, 59, 0.25); border: 1px solid rgba(255,255,255,0.05); border-radius: 14px;">
            <img 
              v-if="currentUser.profileImageUrl" 
              :src="currentUser.profileImageUrl" 
              alt="Profile" 
              style="width: 54px; height: 54px; border-radius: 50%; border: 2px solid #38bdf8; object-fit: cover;" 
            />
            <div v-else class="profile-avatar" style="width: 54px; height: 54px; border-radius: 50%; background: #38bdf8; display: flex; align-items: center; justify-content: center; font-size: 1.35rem; color: #ffffff;">👤</div>
            <div class="profile-details" style="flex-grow: 1; text-align: left; display: flex; flex-direction: column; justify-content: center;">
              <h2 class="profile-username" style="margin: 0; font-size: 1.2rem; font-weight: 700; color: #f8fafc; line-height: 1.25;">{{ currentUser.username }}</h2>
              <p v-if="currentUser.email" class="profile-email" style="margin: 0.25rem 0 0; font-size: 0.8rem; color: #64748b; line-height: 1.2;">{{ currentUser.email }}</p>
            </div>
            <button 
              @click="$emit('logout')" 
              class="logout-outline-btn"
              title="Logout"
              style="width: 34px; height: 34px; border-radius: 8px; background: rgba(239, 68, 68, 0.08); border: 1px solid rgba(239, 68, 68, 0.3); color: #ef4444; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: all 0.2s; flex-shrink: 0;"
              onmouseover="this.style.background='rgba(239, 68, 68, 0.18)'"
              onmouseout="this.style.background='rgba(239, 68, 68, 0.08)'"
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                <polyline points="16 17 21 12 16 7"></polyline>
                <line x1="21" y1="12" x2="9" y2="12"></line>
              </svg>
            </button>
          </div>

          <!-- History List Section -->
          <div class="mypage-history-section" style="text-align: left; margin-top: 0.25rem;">
            <div class="stage-card-list" style="display: flex; flex-direction: column; gap: 0.65rem; max-height: 210px; overflow-y: auto; padding: 4px 0.25rem 4px 4px;">
              <div 
                v-for="item in histories" 
                :key="item.id" 
                class="history-item" 
                @click="openHistoryModal(item)"
                style="cursor: pointer; display: flex; justify-content: space-between; align-items: center; padding: 0.45rem 0.95rem; background: rgba(255, 255, 255, 0.02); border: 1px solid rgba(255, 255, 255, 0.05); border-radius: 10px; transition: all 0.2s;"
                onmouseover="this.style.background='rgba(255, 255, 255, 0.04)'; this.style.borderColor='rgba(56, 189, 248, 0.2)';"
                onmouseout="this.style.background='rgba(255, 255, 255, 0.02)'; this.style.borderColor='rgba(255, 255, 255, 0.05)';"
              >
                <div style="display: flex; align-items: center; gap: 0.5rem;">
                  <span class="cleared-at-text" style="font-size: 0.75rem; font-weight: 500; color: #64748b;">{{ formatClearedAt(item.clearedAt) }}</span>
                  <span class="stage-name" style="font-weight: 600; color: #f8fafc; font-size: 0.9rem;">{{ item.stageName }}</span>
                </div>
                <span class="elapsed-time-badge" style="font-size: 0.7rem; font-weight: 600; padding: 0.18rem 0.45rem; background: rgba(56, 189, 248, 0.1); border: 1px solid rgba(56, 189, 248, 0.15); border-radius: 5px; color: #38bdf8;">{{ item.elapsedTime }}s</span>
              </div>
              <div v-if="histories.length === 0" class="empty-history" style="text-align: center; padding: 2rem; color: #64748b; font-size: 0.85rem;">
                No history found. Complete puzzles to populate!
              </div>
            </div>

            <!-- History Pagination Controls -->
            <div class="dropdown-pagination-bar" v-if="historyTotalPages > 1" style="margin-top: 0.75rem; display: flex; align-items: center; justify-content: space-between; border-top: 1px solid rgba(255, 255, 255, 0.1); padding-top: 0.75rem;">
              <button 
                class="btn btn-sm btn-outline-light" 
                :disabled="historyCurrentPage === 0"
                @click.stop="$emit('page-change', historyCurrentPage - 1)"
              >
                ◀
              </button>
              <span class="page-info" style="color: #94a3b8; font-size: 0.85rem; font-weight: 500;">
                {{ historyCurrentPage + 1 }} / {{ historyTotalPages }}
              </span>
              <button 
                class="btn btn-sm btn-outline-light" 
                :disabled="historyCurrentPage >= historyTotalPages - 1"
                @click.stop="$emit('page-change', historyCurrentPage + 1)"
              >
                ▶
              </button>
            </div>
          </div>
        </div>

        <!-- GitHub Open Source Contribution Link -->
        <div class="mypage-github-contrib" style="margin-top: auto; padding-top: 0.75rem; border-top: 1px solid rgba(255, 255, 255, 0.05); display: flex; align-items: center; justify-content: center; gap: 0.4rem; width: 100%;">
          <svg style="width: 14px; height: 14px; fill: #64748b;" viewBox="0 0 24 24">
            <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
          </svg>
          <a 
            href="https://github.com/devdoyen/rogic.io" 
            target="_blank" 
            rel="noopener noreferrer" 
            style="color: #64748b; font-size: 0.75rem; text-decoration: none; font-weight: 500; transition: color 0.2s;"
            onmouseover="this.style.color='#38bdf8'"
            onmouseout="this.style.color='#64748b'"
          >
            Want to contribute? View on GitHub
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { PuzzleBoard } from '../engine/puzzleBoard';
import NonogramCanvas from './NonogramCanvas.vue';
import type { HistoryResponse } from '../api/userApi';
import type { UserSession } from '../api/auth';
import { fetchStageById } from '../api/stageApi';

defineProps<{
  currentUser: UserSession | null;
  histories: HistoryResponse[];
  historyCurrentPage: number;
  historyTotalPages: number;
}>();

defineEmits<{
  (e: 'close'): void;
  (e: 'login'): void;
  (e: 'logout'): void;
  (e: 'page-change', page: number): void;
}>();

const isReviewMode = defineModel<boolean>('isReviewMode', { default: false });
const modalBoard = defineModel<PuzzleBoard | null>('modalBoard', { default: null });
const selectedHistory = ref<any>(null);

function formatClearedAt(clearedAt: any): string {
  if (!clearedAt) return '';
  if (typeof clearedAt === 'string') {
    return clearedAt.split('T')[0];
  }
  if (Array.isArray(clearedAt)) {
    const yyyy = clearedAt[0];
    const mm = String(clearedAt[1]).padStart(2, '0');
    const dd = String(clearedAt[2]).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  }
  return String(clearedAt);
}

async function openHistoryModal(item: any) {
  selectedHistory.value = item;
  try {
    const details = await fetchStageById(item.stageId);
    const board = new PuzzleBoard(details.solutionGrid);
    for (let r = 0; r < board.rowCount; r++) {
      for (let c = 0; c < board.colCount; c++) {
        board.currentGrid[r][c] = details.solutionGrid[r][c];
      }
    }
    modalBoard.value = board;
    isReviewMode.value = true;
  } catch (error) {
    console.error('Failed to load stage details for review:', error);
  }
}

function closeReview() {
  isReviewMode.value = false;
  modalBoard.value = null;
  selectedHistory.value = null;
}
</script>

<style scoped>
@keyframes modalFadeIn {
  from {
    opacity: 0;
    transform: scale(0.95) translateY(10px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.mypage-popup-overlay {
  animation: fade-in 0.25s ease-out;
}

.mypage-popup-content {
  box-sizing: border-box;
}

/* Scrollbar styling */
.stage-card-list::-webkit-scrollbar {
  width: 6px;
}

.stage-card-list::-webkit-scrollbar-track {
  background: transparent;
}

.stage-card-list::-webkit-scrollbar-thumb {
  background: #334155;
  border-radius: 3px;
}

.stage-card-list::-webkit-scrollbar-thumb:hover {
  background: #475569;
}

.stage-card-list {
  flex-grow: 1;
  overflow-y: auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding-right: 4px;
}

.btn {
  font-weight: 500;
  padding: 0.25rem 0.5rem;
  font-size: 0.75rem;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  background: transparent;
  color: #f8fafc;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.4);
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@keyframes fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

.mypage-dashboard {
  max-width: 600px;
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  box-sizing: border-box;
}

.mypage-user-profile {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  background-color: #1e293b;
  border: 1px solid #334155;
  border-radius: 16px;
  padding: 1.5rem;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.3);
  box-sizing: border-box;
  width: 100%;
}

.profile-avatar {
  font-size: 3rem;
  background-color: #0f172a;
  width: 4.5rem;
  height: 4.5rem;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 50%;
  border: 2px solid #38bdf8;
}

.profile-username {
  margin: 0 0 0.5rem 0;
  color: #f8fafc;
  font-size: 1.5rem;
}

.profile-stats {
  display: flex;
  gap: 0.75rem;
}

.profile-lv {
  color: #38bdf8;
  background-color: rgba(56, 189, 248, 0.15);
  font-size: 0.85rem;
  font-weight: 700;
  padding: 0.15rem 0.5rem;
  border-radius: 4px;
}

.profile-xp {
  color: #818cf8;
  font-size: 0.85rem;
  font-weight: 600;
  display: flex;
  align-items: center;
}

/* History Card */
.history-item {
  background-color: #1e293b;
  border: 1px solid #334155;
  border-radius: 10px;
  padding: 0.8rem 1rem;
  transition: all 0.2s;
}

.history-item:hover {
  border-color: #38bdf8;
  transform: translateY(-1px);
}

.key-indicator {
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-weight: 600;
  font-size: 0.85rem;
  margin-right: 0.5rem;
}

.left-click {
  background-color: #38bdf8;
  color: #0f172a;
}

.right-click {
  background-color: #f43f5e;
  color: #ffffff;
}

.modal-content {
  background-color: #1e293b;
  border: 1px solid #334155;
  border-radius: 16px;
  padding: 2rem;
  width: 90%;
  text-align: center;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.55);
  animation: pop-in 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  box-sizing: border-box;
}

@keyframes pop-in {
  0% {
    transform: scale(0.8);
    opacity: 0;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
