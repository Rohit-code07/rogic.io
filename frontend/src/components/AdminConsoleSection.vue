<template>
  <div class="container-fluid py-4 min-vh-100 bg-light text-dark admin-backoffice-view" style="font-family: 'Outfit', sans-serif;">
    <!-- Show login screen if not logged in -->
    <div v-if="!logged" class="d-flex justify-content-center align-items-center" style="min-height: 80vh;">
      <div class="card shadow-lg border-0 p-4 admin-login-card" style="max-width: 400px; width: 100%; border-radius: 12px; background: rgba(255, 255, 255, 0.95);">
        <div class="text-center mb-4">
          <span class="fs-1">🔑</span>
          <h3 class="fw-bold mt-2 text-dark">Admin Console</h3>
          <p class="text-muted">Please log in to continue</p>
        </div>
        <form @submit.prevent="handleAdminLogin" class="admin-login-form">
          <div class="mb-3">
            <label class="form-label fw-bold text-dark">Username</label>
            <input type="text" v-model="adminUsernameInput" class="form-control admin-username-input" placeholder="Enter username" required />
          </div>
          <div class="mb-3">
            <label class="form-label fw-bold text-dark">Password</label>
            <input type="password" v-model="adminPasswordInput" class="form-control admin-password-input" placeholder="Enter password" required />
          </div>
          <div v-if="loginError" class="alert alert-danger text-center py-2 admin-login-error" role="alert">
            {{ loginError }}
          </div>
          <button type="submit" class="btn btn-dark w-100 py-2 fw-bold mt-2 admin-login-submit-btn">Login</button>
        </form>
      </div>
    </div>

    <!-- If logged in, show the admin console -->
    <div v-else class="admin-console-content">
      <!-- Bootstrap Navbar -->
      <nav class="navbar navbar-dark bg-dark rounded shadow-sm mb-4 px-4 py-3 d-flex justify-content-between align-items-center">
        <span class="navbar-brand mb-0 h1 d-flex align-items-center gap-2">
          <span class="fs-4">🔑</span> rogic.io Admin Console
        </span>
        <div class="d-flex align-items-center gap-3 text-light">
          <span class="badge bg-secondary p-2">Back Office</span>
          <div class="d-flex align-items-center gap-1 admin-ai-size-selectors">
            <span class="small text-light" style="font-size: 0.8rem;">AI Size:</span>
            <select v-model.number="adminAiWidth" class="form-select form-select-sm text-dark admin-ai-width-select" style="width: 65px; padding: 2px 5px; font-size: 0.8rem;">
              <option value="5">5</option>
              <option value="10">10</option>
              <option value="15">15</option>
              <option value="20">20</option>
              <option value="30">30</option>
            </select>
            <span class="small text-light" style="font-size: 0.8rem;">x</span>
            <select v-model.number="adminAiHeight" class="form-select form-select-sm text-dark admin-ai-height-select" style="width: 65px; padding: 2px 5px; font-size: 0.8rem;">
              <option value="5">5</option>
              <option value="10">10</option>
              <option value="15">15</option>
              <option value="20">20</option>
              <option value="30">30</option>
            </select>
          </div>
          <button class="btn btn-outline-light btn-sm admin-ai-gen-btn" @click="handleAdminAiGenerate">
            ✨ Auto-Generate Stage
          </button>
          <button class="btn btn-danger btn-sm admin-logout-btn" @click="handleAdminLogout">
            🚪 Logout
          </button>
        </div>
      </nav>

      <!-- Main Back-office Layout Grid -->
      <div class="row g-4">
        <!-- Left Column: Manage Puzzles -->
        <div class="col-lg-7">
          <div class="card shadow-sm border-0 h-100">
            <div class="card-header bg-dark text-white py-3">
              <h5 class="card-title mb-0 d-flex justify-content-between align-items-center">
                <span>📋 Manage Puzzles</span>
                <span class="badge bg-primary">{{ filteredAndSortedAdminStages.length }} / {{ adminStages.length }}</span>
              </h5>
            </div>
            <div class="card-body p-0">
              <!-- Filtering Controls -->
              <div class="p-3 bg-light border-bottom d-flex flex-wrap gap-3 align-items-center">
                <div class="flex-grow-1" style="min-width: 200px;">
                  <input 
                    type="text" 
                    v-model="adminSearchQuery" 
                    class="form-control form-control-sm admin-search-input" 
                    placeholder="🔍 Search by name..." 
                  />
                </div>
                <div style="width: 130px;">
                  <select v-model="adminSizeFilter" class="form-select form-select-sm admin-size-filter">
                    <option value="All">All Sizes</option>
                    <option value="5">5 x 5</option>
                    <option value="10">10 x 10</option>
                    <option value="15">15 x 15</option>
                    <option value="20">20 x 20</option>
                    <option value="30">30 x 30</option>
                  </select>
                </div>
                <div style="width: 150px;">
                  <select v-model="adminStatusFilter" class="form-select form-select-sm admin-status-filter">
                    <option value="All">All Statuses</option>
                    <option value="Active">Active</option>
                    <option value="Pending">Pending Approval</option>
                    <option value="Inactive">Inactive</option>
                  </select>
                </div>
              </div>

              <div class="table-responsive" style="max-height: 600px; overflow-y: auto;">
                <table class="table table-striped table-hover align-middle mb-0">
                  <thead class="table-dark position-sticky top-0" style="z-index: 10;">
                    <tr>
                      <th scope="col" class="ps-3 admin-th-id" @click="toggleAdminSort('id')" style="cursor: pointer; user-select: none;">
                        ID <span v-if="adminSortKey === 'id'">{{ adminSortOrder === 'asc' ? '▲' : '▼' }}</span>
                      </th>
                      <th scope="col" class="admin-th-name" @click="toggleAdminSort('name')" style="cursor: pointer; user-select: none;">
                        Name <span v-if="adminSortKey === 'name'">{{ adminSortOrder === 'asc' ? '▲' : '▼' }}</span>
                      </th>
                      <th scope="col" class="admin-th-size" @click="toggleAdminSort('size')" style="cursor: pointer; user-select: none;">
                        Size <span v-if="adminSortKey === 'size'">{{ adminSortOrder === 'asc' ? '▲' : '▼' }}</span>
                      </th>
                      <th scope="col" class="admin-th-status" @click="toggleAdminSort('status')" style="cursor: pointer; user-select: none;">
                        Status <span v-if="adminSortKey === 'status'">{{ adminSortOrder === 'asc' ? '▲' : '▼' }}</span>
                      </th>
                      <th scope="col" class="admin-th-feedback">Feedback</th>
                      <th scope="col" class="text-end pe-3">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="s in filteredAndSortedAdminStages" :key="s.id" class="admin-stage-item">
                      <td class="ps-3 font-monospace text-muted">{{ s.id }}</td>
                      <td>
                        <span class="fw-bold text-dark">{{ s.name }}</span>
                      </td>
                      <td>
                        <span class="badge bg-light text-dark border">{{ s.width }} x {{ s.height }}</span>
                      </td>
                      <td>
                        <span v-if="s.approved && s.active" class="badge bg-success badge-active">Active</span>
                        <span v-else-if="!s.approved" class="badge bg-warning text-dark badge-pending">Pending Approval</span>
                        <span v-else class="badge bg-danger badge-inactive">Inactive</span>
                      </td>
                      <td style="white-space: nowrap; vertical-align: middle;">
                        <div style="display: inline-flex; align-items: center; gap: 0.5rem; justify-content: center; width: 100%;">
                          <span style="display: inline-flex; align-items: center; gap: 0.25rem; color: #10b981; font-weight: 600;" title="Upvotes">
                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="width: 14px; height: 14px;">
                              <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3" />
                            </svg>
                            {{ s.upvotes || 0 }}
                          </span>
                          <span style="color: #64748b;">/</span>
                          <span style="display: inline-flex; align-items: center; gap: 0.25rem; color: #ef4444; font-weight: 600;" title="Downvotes">
                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" style="width: 14px; height: 14px;">
                              <path d="M10 15v4a3 3 0 0 0 3 3l4-9V2H5.72a2 2 0 0 0-2 1.7l-1.38 9a2 2 0 0 0 2 2.3zm7-13h3a2 2 0 0 1 2 2v7a2 2 0 0 1-2 2h-3" />
                            </svg>
                            {{ s.downvotes || 0 }}
                          </span>
                        </div>
                      </td>
                      <td class="text-end pe-3">
                        <div class="btn-group btn-group-sm" role="group">
                          <button class="btn btn-outline-dark btn-preview" @click="openHistoryModal({ stageId: s.id, stageName: s.name })">
                            👁️ Preview
                          </button>
                          <button v-if="!s.approved" class="btn btn-success btn-approve" @click="handleApproveStage(s.id)">
                            ✓ Approve
                          </button>
                          <button class="btn btn-danger btn-delete" @click="handleDeleteStage(s.id)">
                            🗑️ Delete
                          </button>
                          <button v-if="!s.active && s.approved" class="btn btn-primary btn-restore" @click="handleRestoreStage(s.id)">
                            ↺ Restore
                          </button>
                        </div>
                      </td>
                    </tr>
                    <tr v-if="adminStages.length === 0">
                      <td colspan="6" class="text-center py-5 text-muted">
                        No stages found in database.
                      </td>
                    </tr>
                  </tbody>
                </table>
                <!-- Admin Stages Pagination -->
                <div class="d-flex justify-content-between align-items-center mt-3 px-3" v-if="adminStagesTotalPages > 1">
                  <button 
                    class="btn btn-sm btn-outline-dark" 
                    :disabled="adminStagesCurrentPage === 0"
                    @click="loadAdminStagesList(adminStagesCurrentPage - 1)"
                  >
                    ◀ Previous
                  </button>
                  <span class="text-muted small">
                    Page {{ adminStagesCurrentPage + 1 }} of {{ adminStagesTotalPages }}
                  </span>
                  <button 
                    class="btn btn-sm btn-outline-dark" 
                    :disabled="adminStagesCurrentPage >= adminStagesTotalPages - 1"
                    @click="loadAdminStagesList(adminStagesCurrentPage + 1)"
                  >
                    Next ▶
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Right Column: Add Custom Puzzle -->
        <div class="col-lg-5">
          <div class="card shadow-sm border-0">
            <div class="card-header bg-dark text-white py-3">
              <h5 class="card-title mb-0">✍️ Add Custom Puzzle</h5>
            </div>
            <div class="card-body">
              <div class="mb-3">
                <label class="form-label fw-bold text-dark">Puzzle Name</label>
                <input 
                  type="text" 
                  v-model="creatorName" 
                  class="form-control admin-input" 
                  placeholder="e.g. Tree, Anchor, Diamond" 
                />
              </div>
              
              <div class="row g-3 mb-4">
                <div class="col-6">
                  <label class="form-label fw-bold text-dark">Width</label>
                  <select v-model="creatorWidth" @change="initCreatorGrid" class="form-select">
                    <option :value="5">5</option>
                    <option :value="10">10</option>
                    <option :value="15">15</option>
                    <option :value="20">20</option>
                    <option :value="30">30</option>
                  </select>
                </div>
                <div class="col-6">
                  <label class="form-label fw-bold text-dark">Height</label>
                  <select v-model="creatorHeight" @change="initCreatorGrid" class="form-select">
                    <option :value="5">5</option>
                    <option :value="10">10</option>
                    <option :value="15">15</option>
                    <option :value="20">20</option>
                    <option :value="30">30</option>
                  </select>
                </div>
              </div>

              <!-- HTML5 Canvas Creator Grid Wrapper -->
              <div class="mb-4">
                <label class="form-label fw-bold d-block text-center mb-2 text-dark">Draw Puzzle Solution</label>
                <div class="d-flex justify-content-center align-items-center p-3 bg-dark rounded border" style="min-height: 260px;">
                  <canvas 
                    ref="creatorCanvasRef" 
                    @mousedown="handleCreatorMouseDown" 
                    @touchstart="handleCreatorTouchStart"
                    @contextmenu.prevent
                    style="display: block; cursor: pointer; border: 1px solid rgba(255, 255, 255, 0.2);"
                  ></canvas>
                </div>
                <div class="form-text text-center mt-2 text-muted">
                  Left-click / drag to fill cells. Right-click or drag again to clear.
                </div>
              </div>

              <button 
                class="btn btn-dark w-100 py-2 fw-bold admin-save-btn" 
                @click="handleSaveCustomStage"
              >
                💾 Save Custom Stage
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Reused Modal for History Review / Preview inside Back Office -->
    <div v-if="isPreviewOpen && previewBoard" class="modal show d-block" tabindex="-1" style="background: rgba(0, 0, 0, 0.7); backdrop-filter: blur(4px); z-index: 1050;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg bg-light text-dark">
          <div class="modal-header bg-dark text-white border-0">
            <h5 class="modal-title">👁️ Stage Solution Preview</h5>
            <button type="button" class="btn-close btn-close-white" @click="closeModal" aria-label="Close"></button>
          </div>
          <div class="modal-body text-center bg-light">
            <p class="text-muted mb-3">Stage: <strong class="text-dark">{{ previewHistory?.stageName }}</strong></p>
            <div class="modal-canvas-wrapper mx-auto" style="width: 320px; max-width: 100%; aspect-ratio: 1; background-color: #0f172a; border-radius: 12px; overflow: hidden; border: 1px solid rgba(255, 255, 255, 0.1); display: flex; justify-content: center; align-items: center; position: relative;">
              <NonogramCanvas :board="previewBoard" :readOnly="true" :initialAngle="0" />
            </div>
          </div>
          <div class="modal-footer border-0 bg-light">
            <button type="button" class="btn btn-secondary modal-close-btn" @click="closeModal">Close</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue';
import { PuzzleBoard } from '../engine/puzzleBoard';
import NonogramCanvas from './NonogramCanvas.vue';
import { fetchStageById } from '../api/stageApi';
import { 
  fetchAdminStages, 
  createStage, 
  approveStage, 
  deleteStage, 
  restoreStage, 
  generateAiStage, 
  loginAdmin, 
  logoutAdmin 
} from '../api/adminApi';
import type { AdminStageInfo } from '../api/adminApi';

const logged = defineModel<boolean>('logged', { required: true });

const emit = defineEmits<{
  (e: 'stage-updated'): void;
}>();

const adminUsernameInput = ref('');
const adminPasswordInput = ref('');
const loginError = ref('');
const adminStages = ref<AdminStageInfo[]>([]);

const adminStagesCurrentPage = ref(0);
const adminStagesTotalPages = ref(1);

const adminSearchQuery = ref('');
const adminSizeFilter = ref('All');
const adminStatusFilter = ref('All');
const adminSortKey = ref<'id' | 'name' | 'size' | 'status'>('id');
const adminSortOrder = ref<'asc' | 'desc'>('asc');

const creatorName = ref('');
const creatorWidth = ref(5);
const creatorHeight = ref(5);
const adminAiWidth = ref(5);
const adminAiHeight = ref(5);

const creatorGrid = ref<number[][]>([]);
const creatorCanvasRef = ref<HTMLCanvasElement | null>(null);
const isCreatorDragging = ref(false);
const creatorDragVal = ref(0);
const lastCreatorRow = ref(-1);
const lastCreatorCol = ref(-1);

const isPreviewOpen = ref(false);
const previewBoard = ref<PuzzleBoard | null>(null);
const previewHistory = ref<any>(null);

const filteredAndSortedAdminStages = computed(() => {
  let list = [...adminStages.value];

  // 1. Filter by search query (Name case-insensitive)
  if (adminSearchQuery.value.trim() !== '') {
    const q = adminSearchQuery.value.toLowerCase();
    list = list.filter(s => s.name.toLowerCase().includes(q));
  }

  // 2. Filter by size (width x height)
  if (adminSizeFilter.value !== 'All') {
    const size = parseInt(adminSizeFilter.value);
    list = list.filter(s => s.width === size || s.height === size);
  }

  // 3. Filter by status
  if (adminStatusFilter.value !== 'All') {
    if (adminStatusFilter.value === 'Active') {
      list = list.filter(s => s.approved && s.active);
    } else if (adminStatusFilter.value === 'Pending') {
      list = list.filter(s => !s.approved);
    } else if (adminStatusFilter.value === 'Inactive') {
      list = list.filter(s => s.approved && !s.active);
    }
  }

  // 4. Sort
  list.sort((a, b) => {
    let valA: any;
    let valB: any;

    if (adminSortKey.value === 'id') {
      valA = a.id;
      valB = b.id;
    } else if (adminSortKey.value === 'name') {
      valA = a.name.toLowerCase();
      valB = b.name.toLowerCase();
    } else if (adminSortKey.value === 'size') {
      valA = a.width * a.height;
      valB = b.width * b.height;
    } else if (adminSortKey.value === 'status') {
      valA = a.approved && a.active ? 3 : (!a.approved ? 2 : 1);
      valB = b.approved && b.active ? 3 : (!b.approved ? 2 : 1);
    }

    if (valA < valB) return adminSortOrder.value === 'asc' ? -1 : 1;
    if (valA > valB) return adminSortOrder.value === 'asc' ? 1 : -1;
    return 0;
  });

  return list;
});

function toggleAdminSort(key: 'id' | 'name' | 'size' | 'status') {
  if (adminSortKey.value === key) {
    adminSortOrder.value = adminSortOrder.value === 'asc' ? 'desc' : 'asc';
  } else {
    adminSortKey.value = key;
    adminSortOrder.value = 'asc';
  }
}

async function loadAdminStagesList(page: number = 0) {
  try {
    const res = await fetchAdminStages(page, 100);
    let list: AdminStageInfo[];
    if (res && 'content' in res) {
      list = res.content;
      adminStagesTotalPages.value = res.totalPages;
      adminStagesCurrentPage.value = res.number;
    } else {
      list = res;
      adminStagesTotalPages.value = 1;
      adminStagesCurrentPage.value = 0;
    }
    adminStages.value = list;
  } catch (error) {
    console.error('Failed to load admin stages:', error);
  }
}

function initCreatorGrid() {
  creatorGrid.value = Array.from({ length: creatorHeight.value }, () =>
    Array(creatorWidth.value).fill(0)
  );
  setTimeout(drawCreatorGrid, 20);
}

function drawCreatorGrid() {
  const canvas = creatorCanvasRef.value;
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  if (!ctx) return;

  const maxCells = Math.max(creatorWidth.value, creatorHeight.value);
  const cellPixelSize = maxCells <= 5 ? 30 : (maxCells <= 10 ? 20 : (maxCells <= 15 ? 15 : (maxCells <= 20 ? 12 : 8)));
  
  canvas.width = creatorWidth.value * cellPixelSize;
  canvas.height = creatorHeight.value * cellPixelSize;

  // Background
  ctx.fillStyle = '#0f172a';
  ctx.fillRect(0, 0, canvas.width, canvas.height);

  for (let r = 0; r < creatorHeight.value; r++) {
    for (let c = 0; c < creatorWidth.value; c++) {
      const x = c * cellPixelSize;
      const y = r * cellPixelSize;

      ctx.strokeStyle = '#334155';
      ctx.lineWidth = 1;
      ctx.strokeRect(x, y, cellPixelSize, cellPixelSize);

      const val = creatorGrid.value[r]?.[c] || 0;
      if (val === 1) {
        const grad = ctx.createLinearGradient(x, y, x + cellPixelSize, y + cellPixelSize);
        grad.addColorStop(0, '#38bdf8');
        grad.addColorStop(1, '#818cf8');
        ctx.fillStyle = grad;
        ctx.fillRect(x + 1, y + 1, cellPixelSize - 2, cellPixelSize - 2);
      } else {
        ctx.fillStyle = '#1e293b';
        ctx.fillRect(x + 1, y + 1, cellPixelSize - 2, cellPixelSize - 2);
      }
    }
  }

  // Draw grid helper lines every 5 cells
  ctx.strokeStyle = '#64748b';
  ctx.lineWidth = 2;
  for (let r = 0; r <= creatorHeight.value; r += 5) {
    if (r > 0 && r < creatorHeight.value) {
      ctx.beginPath();
      ctx.moveTo(0, r * cellPixelSize);
      ctx.lineTo(canvas.width, r * cellPixelSize);
      ctx.stroke();
    }
  }
  for (let c = 0; c <= creatorWidth.value; c += 5) {
    if (c > 0 && c < creatorWidth.value) {
      ctx.beginPath();
      ctx.moveTo(c * cellPixelSize, 0);
      ctx.lineTo(c * cellPixelSize, canvas.height);
      ctx.stroke();
    }
  }
}

function getCreatorCoords(clientX: number, clientY: number) {
  const canvas = creatorCanvasRef.value;
  if (!canvas) return null;
  const rect = canvas.getBoundingClientRect();
  const scaleX = canvas.width / rect.width;
  const scaleY = canvas.height / rect.height;
  const clickX = (clientX - rect.left) * scaleX;
  const clickY = (clientY - rect.top) * scaleY;

  const maxCells = Math.max(creatorWidth.value, creatorHeight.value);
  const cellPixelSize = maxCells <= 5 ? 30 : (maxCells <= 10 ? 20 : (maxCells <= 15 ? 15 : (maxCells <= 20 ? 12 : 8)));
  
  const col = Math.floor(clickX / cellPixelSize);
  const row = Math.floor(clickY / cellPixelSize);

  if (row < 0 || row >= creatorHeight.value || col < 0 || col >= creatorWidth.value) {
    return null;
  }
  return { row, col };
}

function handleCreatorMouseDown(event: MouseEvent) {
  if (event.button !== 0) return; // Only left click
  const coords = getCreatorCoords(event.clientX, event.clientY);
  if (!coords) return;

  const { row, col } = coords;
  creatorDragVal.value = creatorGrid.value[row][col] === 1 ? 0 : 1;
  creatorGrid.value[row][col] = creatorDragVal.value;
  isCreatorDragging.value = true;
  lastCreatorRow.value = row;
  lastCreatorCol.value = col;
  drawCreatorGrid();

  window.addEventListener('mousemove', handleCreatorWindowMouseMove);
  window.addEventListener('mouseup', handleCreatorWindowMouseUp);
}

function handleCreatorWindowMouseMove(event: MouseEvent) {
  if (!isCreatorDragging.value) return;
  const coords = getCreatorCoords(event.clientX, event.clientY);
  if (!coords) return;

  const { row, col } = coords;
  if (row !== lastCreatorRow.value || col !== lastCreatorCol.value) {
    creatorGrid.value[row][col] = creatorDragVal.value;
    lastCreatorRow.value = row;
    lastCreatorCol.value = col;
    drawCreatorGrid();
  }
}

function handleCreatorWindowMouseUp() {
  if (isCreatorDragging.value) {
    isCreatorDragging.value = false;
    window.removeEventListener('mousemove', handleCreatorWindowMouseMove);
    window.removeEventListener('mouseup', handleCreatorWindowMouseUp);
  }
}

function handleCreatorTouchStart(event: TouchEvent) {
  if (event.touches.length !== 1) return;
  event.preventDefault();
  const touch = event.touches[0];
  const coords = getCreatorCoords(touch.clientX, touch.clientY);
  if (!coords) return;

  const { row, col } = coords;
  creatorDragVal.value = creatorGrid.value[row][col] === 1 ? 0 : 1;
  creatorGrid.value[row][col] = creatorDragVal.value;
  isCreatorDragging.value = true;
  lastCreatorRow.value = row;
  lastCreatorCol.value = col;
  drawCreatorGrid();

  window.addEventListener('touchmove', handleCreatorWindowTouchMove, { passive: false });
  window.addEventListener('touchend', handleCreatorWindowTouchEnd);
  window.addEventListener('touchcancel', handleCreatorWindowTouchEnd);
}

function handleCreatorWindowTouchMove(event: TouchEvent) {
  if (!isCreatorDragging.value || event.touches.length !== 1) return;
  event.preventDefault();
  const touch = event.touches[0];
  const coords = getCreatorCoords(touch.clientX, touch.clientY);
  if (!coords) return;

  const { row, col } = coords;
  if (row !== lastCreatorRow.value || col !== lastCreatorCol.value) {
    creatorGrid.value[row][col] = creatorDragVal.value;
    lastCreatorRow.value = row;
    lastCreatorCol.value = col;
    drawCreatorGrid();
  }
}

function handleCreatorWindowTouchEnd() {
  if (isCreatorDragging.value) {
    isCreatorDragging.value = false;
    window.removeEventListener('touchmove', handleCreatorWindowTouchMove);
    window.removeEventListener('touchend', handleCreatorWindowTouchEnd);
    window.removeEventListener('touchcancel', handleCreatorWindowTouchEnd);
  }
}

async function handleSaveCustomStage() {
  if (!creatorName.value || creatorName.value.trim() === '') {
    alert('Please enter a stage name.');
    return;
  }

  // Verify at least one filled cell
  let hasFilled = false;
  for (let r = 0; r < creatorHeight.value; r++) {
    for (let c = 0; c < creatorWidth.value; c++) {
      if (creatorGrid.value[r][c] === 1) {
        hasFilled = true;
        break;
      }
    }
  }

  if (!hasFilled) {
    alert('Please draw at least one filled cell.');
    return;
  }

  try {
    const newStage = {
      name: creatorName.value,
      width: creatorWidth.value,
      height: creatorHeight.value,
      solutionGrid: creatorGrid.value
    };

    await createStage(newStage as any);
    alert('Stage saved successfully!');
    
    // Clear and reload
    creatorName.value = '';
    initCreatorGrid();
    await loadAdminStagesList();
    emit('stage-updated');
  } catch (error: any) {
    console.error('Failed to save stage:', error);
    alert(error.response?.data || 'Failed to save stage. Ensure it has a unique solution.');
  }
}

async function handleApproveStage(id: number) {
  try {
    await approveStage(id);
    await loadAdminStagesList();
    emit('stage-updated');
  } catch (error) {
    console.error('Failed to approve stage:', error);
  }
}

async function handleDeleteStage(id: number) {
  if (!confirm('Are you sure you want to delete this stage?')) return;
  try {
    await deleteStage(id);
    await loadAdminStagesList();
    emit('stage-updated');
  } catch (error) {
    console.error('Failed to delete stage:', error);
  }
}

async function handleRestoreStage(id: number) {
  try {
    await restoreStage(id);
    await loadAdminStagesList();
    emit('stage-updated');
  } catch (error) {
    console.error('Failed to restore stage:', error);
  }
}

async function handleAdminAiGenerate() {
  try {
    const w = Number(adminAiWidth.value) || 5;
    const h = Number(adminAiHeight.value) || 5;
    await generateAiStage(w, h);
    alert('Stage generated as Pending Approval!');
    await loadAdminStagesList();
  } catch (error) {
    console.error('Failed to generate stage:', error);
    alert('Failed to generate stage.');
  }
}

async function handleAdminLogin() {
  try {
    loginError.value = '';
    await loginAdmin(adminUsernameInput.value, adminPasswordInput.value);
    logged.value = true;
    adminUsernameInput.value = '';
    adminPasswordInput.value = '';
    await loadAdminStagesList();
    initCreatorGrid();
  } catch (error: any) {
    console.error('Admin login failed:', error);
    loginError.value = 'Invalid username or password';
  }
}

async function handleAdminLogout() {
  await logoutAdmin();
  logged.value = false;
}

async function openHistoryModal(item: any) {
  previewHistory.value = item;
  try {
    const details = await fetchStageById(item.stageId);
    const board = new PuzzleBoard(details.solutionGrid);
    for (let r = 0; r < board.rowCount; r++) {
      for (let c = 0; c < board.colCount; c++) {
        board.setCell(r, c, details.solutionGrid[r][c]);
      }
    }
    previewBoard.value = board;
    isPreviewOpen.value = true;
  } catch (error) {
    console.error('Failed to load stage details for review:', error);
  }
}

function closeModal() {
  isPreviewOpen.value = false;
  previewBoard.value = null;
  previewHistory.value = null;
}

watch(logged, (newVal) => {
  if (newVal) {
    loadAdminStagesList();
    setTimeout(initCreatorGrid, 50);
  }
});

onMounted(async () => {
  if (logged.value) {
    await loadAdminStagesList();
    initCreatorGrid();
  }
});

onUnmounted(() => {
  window.removeEventListener('mousemove', handleCreatorWindowMouseMove);
  window.removeEventListener('mouseup', handleCreatorWindowMouseUp);
  window.removeEventListener('touchmove', handleCreatorWindowTouchMove);
  window.removeEventListener('touchend', handleCreatorWindowTouchEnd);
  window.removeEventListener('touchcancel', handleCreatorWindowTouchEnd);
});
</script>

<style scoped>
/* Admin CSS styling utilizes Bootstrap CSS loaded globally in header */
.admin-login-card {
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1);
}
.navbar {
  border: 1px solid rgba(255, 255, 255, 0.05);
}
</style>
