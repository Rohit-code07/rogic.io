<template>
  <div class="admin-backoffice-view">
    <!-- Show login screen if not logged in -->
    <div v-if="!logged" class="login-screen-wrapper">
      <div class="login-card">
        <div class="login-header">
          <div class="brand-logo-icon">
            <!-- Key Icon -->
            <svg class="icon-key" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" d="M15 7a2 2 0 012 2m-2 4a2 2 0 012 2m-2-4a2 2 0 01-2-2m2 4a2 2 0 01-2 2m4-4h.01M9 16H5v-4h4v4zm0-4h10v4H9v-4zm0 0V8h10v4H9v-4z"></path>
            </svg>
          </div>
          <h2 class="brand-title">rogic.io</h2>
          <p class="brand-subtitle">Administrator Back Office Portal</p>
        </div>
        <form @submit.prevent="handleAdminLogin" class="login-form">
          <div class="form-group">
            <label class="form-label">Username</label>
            <input type="text" v-model="adminUsernameInput" class="admin-input" placeholder="Enter username" required />
          </div>
          <div class="form-group">
            <label class="form-label">Password</label>
            <input type="password" v-model="adminPasswordInput" class="admin-input" placeholder="Enter password" required />
          </div>
          <div v-if="loginError" class="login-error-alert" role="alert">
            {{ loginError }}
          </div>
          <button type="submit" class="btn-primary w-100 py-3 mt-2">
            Sign In
          </button>
        </form>
      </div>
    </div>

    <!-- If logged in, show the admin console -->
    <div v-else class="admin-content-wrapper">
      <!-- Top Header Bar -->
      <header class="admin-header">
        <div class="header-brand">
          <div class="header-logo-icon">
            <svg class="icon-w-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
            </svg>
          </div>
          <div>
            <h1 class="header-title">rogic.io Control Center</h1>
            <p class="header-subtitle">System Administration Panel</p>
          </div>
        </div>
        <div class="header-actions">
          <button @click="handleAdminLogout" class="btn-logout">
            <svg class="icon-w-3_5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
            </svg>
            Logout
          </button>
        </div>
      </header>

      <!-- Main Dashboard Grid -->
      <div class="admin-grid">
        <!-- Sidebar Navigation -->
        <aside class="admin-sidebar">
          <div class="sidebar-header">
            <span class="sidebar-title">Navigation</span>
          </div>
          <nav class="sidebar-nav">
            <button 
              @click="activeTab = 'dashboard'" 
              class="nav-btn"
              :class="{ active: activeTab === 'dashboard' }"
            >
              <svg class="icon-w-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path stroke-linecap="round" stroke-linejoin="round" d="M4 6a2 2 0 012-2h2a2 2 0 012 2v4a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v4a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z"></path>
              </svg>
              <span>Dashboard Overview</span>
            </button>
            <button 
              @click="activeTab = 'puzzles'" 
              class="nav-btn"
              :class="{ active: activeTab === 'puzzles' }"
            >
              <svg class="icon-w-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path stroke-linecap="round" stroke-linejoin="round" d="M11 4a2 2 0 114 0v1a1 1 0 001 1h3a1 1 0 011 1v3a1 1 0 01-1 1h-1a2 2 0 100 4h1a1 1 0 011 1v3a1 1 0 01-1 1h-3a1 1 0 01-1-1v-1a2 2 0 10-4 0v1a1 1 0 01-1 1H7a1 1 0 01-1-1v-3a1 1 0 00-1-1H4a2 2 0 110-4h1a1 1 0 001-1V7a1 1 0 011-1h3a1 1 0 001-1V4z"></path>
              </svg>
              <span>Puzzle Management</span>
            </button>
            <button 
              @click="activeTab = 'users'" 
              class="nav-btn"
              :class="{ active: activeTab === 'users' }"
            >
              <svg class="icon-w-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path>
              </svg>
              <span>Registered Users</span>
            </button>
          </nav>
          <div class="sidebar-footer">
            <span class="version-label">Version 2.0.0 (V2 Active)</span>
          </div>
        </aside>

        <!-- Main Dashboard Viewport -->
        <main class="admin-main">
          <!-- 1. DASHBOARD OVERVIEW PANEL -->
          <div v-if="activeTab === 'dashboard'" class="dashboard-panel">
            <h2 class="panel-title">
              <svg class="icon-w-5 icon-accent" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path stroke-linecap="round" stroke-linejoin="round" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
              </svg>
              System Status Overview
            </h2>
            
            <!-- Cards Grid -->
            <div class="metrics-grid">
              <!-- Card 1 -->
              <div class="metric-card">
                <span class="metric-label">Total Puzzles</span>
                <span class="metric-value text-white">{{ adminStages.length }}</span>
                <div class="progress-bar-container">
                  <div class="progress-bar bg-blue" style="width: 100%;"></div>
                </div>
              </div>
              <!-- Card 2 -->
              <div class="metric-card">
                <span class="metric-label">Active Puzzles</span>
                <span class="metric-value text-emerald">{{ activePuzzlesCount }}</span>
                <div class="progress-bar-container">
                  <div class="progress-bar bg-emerald" :style="{ width: (activePuzzlesCount / Math.max(1, adminStages.length) * 100) + '%' }"></div>
                </div>
              </div>
              <!-- Card 3 -->
              <div class="metric-card">
                <span class="metric-label">Pending Review</span>
                <span class="metric-value text-amber">{{ pendingPuzzlesCount }}</span>
                <div class="progress-bar-container">
                  <div class="progress-bar bg-amber" :style="{ width: (pendingPuzzlesCount / Math.max(1, adminStages.length) * 100) + '%' }"></div>
                </div>
              </div>
              <!-- Card 4 -->
              <div class="metric-card">
                <span class="metric-label">Registered Users</span>
                <span class="metric-value text-indigo">{{ totalUsersCount }}</span>
                <div class="progress-bar-container">
                  <div class="progress-bar bg-indigo" style="width: 100%;"></div>
                </div>
              </div>
            </div>

            <!-- System Database Analytics -->
            <div class="info-stats-grid">
              <div class="stat-box">
                <span class="stat-box-title">Generator Profile Distribution</span>
                <div class="stat-list">
                  <div class="stat-item border-bottom">
                    <span class="text-slate-400">V2 (Two-Step cached generation):</span>
                    <span class="font-bold text-indigo">{{ v2StagesCount }} puzzles</span>
                  </div>
                  <div class="stat-item">
                    <span class="text-slate-400">V1 (Legacy generation):</span>
                    <span class="font-bold text-slate">{{ v1StagesCount }} puzzles</span>
                  </div>
                </div>
              </div>
              <div class="stat-box">
                <span class="stat-box-title">Puzzle Grid Size Summary</span>
                <div class="stat-list">
                  <div class="stat-item border-bottom">
                    <span class="text-slate-400">5 x 5 Size:</span>
                    <span class="font-semibold text-white">{{ countStagesBySize(5) }}</span>
                  </div>
                  <div class="stat-item border-bottom">
                    <span class="text-slate-400">10 x 10 Size:</span>
                    <span class="font-semibold text-white">{{ countStagesBySize(10) }}</span>
                  </div>
                  <div class="stat-item border-bottom">
                    <span class="text-slate-400">15 x 15 Size:</span>
                    <span class="font-semibold text-white">{{ countStagesBySize(15) }}</span>
                  </div>
                  <div class="stat-item border-bottom">
                    <span class="text-slate-400">20 x 20 Size:</span>
                    <span class="font-semibold text-white">{{ countStagesBySize(20) }}</span>
                  </div>
                  <div class="stat-item border-bottom">
                    <span class="text-slate-400">25 x 25 Size:</span>
                    <span class="font-semibold text-white">{{ countStagesBySize(25) }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="text-slate-400">30 x 30 Size:</span>
                    <span class="font-semibold text-white">{{ countStagesBySize(30) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 2. PUZZLE MANAGEMENT PANEL -->
          <div v-if="activeTab === 'puzzles'" class="table-panel">
            <!-- Table Header Section -->
            <div class="table-header-section">
              <h2 class="panel-title">
                <svg class="icon-w-4 icon-accent" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path>
                </svg>
                Puzzle Inventory Manager
              </h2>
              <span class="table-count-badge">
                {{ filteredAndSortedAdminStages.length }} / {{ adminStages.length }} Puzzles
              </span>
            </div>

            <!-- Filters Toolbar -->
            <div class="table-toolbar">
              <div class="flex-grow min-w-240">
                <input 
                  type="text" 
                  v-model="adminSearchQuery" 
                  class="admin-input toolbar-input" 
                  placeholder="🔍 Search puzzle by name..." 
                />
              </div>
              <div class="w-144">
                <select v-model="adminSizeFilter" class="admin-select toolbar-input">
                  <option value="All">All Sizes</option>
                  <option value="5">5 x 5</option>
                  <option value="10">10 x 10</option>
                  <option value="15">15 x 15</option>
                  <option value="20">20 x 20</option>
                  <option value="25">25 x 25</option>
                  <option value="30">30 x 30</option>
                </select>
              </div>
              <div class="w-160">
                <select v-model="adminStatusFilter" class="admin-select toolbar-input">
                  <option value="All">All Statuses</option>
                  <option value="Active">Active</option>
                  <option value="Pending">Pending Approval</option>
                  <option value="Inactive">Inactive</option>
                </select>
              </div>
            </div>

            <!-- Grid Table Container -->
            <div class="table-wrapper">
              <table class="admin-table">
                <thead>
                  <tr>
                    <th scope="col" class="cursor-pointer" @click="toggleAdminSort('id')">
                      ID <span v-if="adminSortKey === 'id'">{{ adminSortOrder === 'asc' ? '▲' : '▼' }}</span>
                    </th>
                    <th scope="col" class="cursor-pointer" @click="toggleAdminSort('name')">
                      Name <span v-if="adminSortKey === 'name'">{{ adminSortOrder === 'asc' ? '▲' : '▼' }}</span>
                    </th>
                    <th scope="col" class="cursor-pointer" @click="toggleAdminSort('size')">
                      Size <span v-if="adminSortKey === 'size'">{{ adminSortOrder === 'asc' ? '▲' : '▼' }}</span>
                    </th>
                    <th scope="col">Version</th>
                    <th scope="col">Created At</th>
                    <th scope="col" class="cursor-pointer" @click="toggleAdminSort('status')">
                      Status <span v-if="adminSortKey === 'status'">{{ adminSortOrder === 'asc' ? '▲' : '▼' }}</span>
                    </th>
                    <th scope="col" class="text-center">Feedback</th>
                    <th scope="col" class="text-right">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="s in filteredAndSortedAdminStages" :key="s.id">
                    <td class="font-mono text-slate-500">{{ s.id }}</td>
                    <td class="font-bold text-white text-sm">{{ s.name }}</td>
                    <td>
                      <span class="size-badge">{{ s.width }} x {{ s.height }}</span>
                    </td>
                    <td>
                      <span class="version-badge" :class="{ 'v2-badge': s.generatorVersion === 'V2' }">
                        {{ s.generatorVersion || 'V1' }}
                      </span>
                    </td>
                    <td class="text-slate-400">{{ formatDate(s.createdAt) }}</td>
                    <td>
                      <span v-if="s.approved && s.active" class="status-badge bg-emerald-light text-emerald border-emerald-border">Active</span>
                      <span v-else-if="!s.approved" class="status-badge bg-amber-light text-amber border-amber-border">Pending Approval</span>
                      <span v-else class="status-badge bg-rose-light text-rose border-rose-border">Inactive</span>
                    </td>
                    <td class="text-center">
                      <div class="feedback-score-container" :title="`👍 ${s.upvotes || 0} / 👎 ${s.downvotes || 0}`">
                        <span :class="getFeedbackClass(s)">
                          {{ getFeedbackScoreFormatted(s) }}
                        </span>
                      </div>
                    </td>
                    <td class="text-right">
                      <div class="btn-group" role="group">
                        <button @click="openHistoryModal({ stageId: s.id, stageName: s.name })" class="flex-center">
                          <svg class="icon-w-3_5 icon-margin-r" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                            <path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                          </svg>
                          Preview
                        </button>
                        <button v-if="!s.approved" @click="handleApproveStage(s.id)" class="text-emerald flex-center border-left">
                          <svg class="icon-w-3_5 icon-margin-r" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"></path>
                          </svg>
                          Approve
                        </button>
                        <button @click="handleDeleteStage(s.id)" class="text-rose flex-center border-left">
                          <svg class="icon-w-3_5 icon-margin-r" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                          </svg>
                          Delete
                        </button>
                        <button v-if="!s.active && s.approved" @click="handleRestoreStage(s.id)" class="text-indigo flex-center border-left">
                          <svg class="icon-w-3_5 icon-margin-r" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M4 4v5h.582m15.356 2A8.001 8.001 0 1121.21 7.89M9 11l3-3 3 3"></path>
                          </svg>
                          Restore
                        </button>
                      </div>
                    </td>
                  </tr>
                  <tr v-if="adminStages.length === 0">
                    <td colspan="8" class="text-center py-10 text-slate-500">
                      No stages found in database.
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- Footer Pagination -->
            <div class="table-footer" v-if="adminStagesTotalPages > 1">
              <button 
                class="btn-nav-pagination" 
                :disabled="adminStagesCurrentPage === 0"
                @click="loadAdminStagesList(adminStagesCurrentPage - 1)"
              >
                ◀ Previous
              </button>
              <span class="text-slate-400">
                Page {{ adminStagesCurrentPage + 1 }} of {{ adminStagesTotalPages }}
              </span>
              <button 
                class="btn-nav-pagination" 
                :disabled="adminStagesCurrentPage >= adminStagesTotalPages - 1"
                @click="loadAdminStagesList(adminStagesCurrentPage + 1)"
              >
                Next ▶
              </button>
            </div>
          </div>

          <!-- 3. REGISTERED USERS PANEL -->
          <div v-if="activeTab === 'users'" class="table-panel">
            <!-- Table Header Section -->
            <div class="table-header-section">
              <h2 class="panel-title">
                <svg class="icon-w-4 icon-accent" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z"></path>
                </svg>
                Registered User Database
              </h2>
              <span class="table-count-badge">
                Total Users: {{ totalUsersCount }}
              </span>
            </div>

            <!-- Grid Table Container -->
            <div class="table-wrapper">
              <table class="admin-table">
                <thead>
                  <tr>
                    <th scope="col">User ID</th>
                    <th scope="col">Avatar</th>
                    <th scope="col">Username</th>
                    <th scope="col">Email Address</th>
                    <th scope="col">Level</th>
                    <th scope="col" class="text-right">XP Points</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="u in adminUsers" :key="u.id">
                    <td class="font-mono text-slate-500">{{ u.id }}</td>
                    <td>
                      <div class="avatar-placeholder">
                        <img v-if="u.profileImageUrl" :src="u.profileImageUrl" alt="Avatar" class="avatar-img" />
                        <span v-else class="text-slate-500 font-bold uppercase">{{ u.username.charAt(0) }}</span>
                      </div>
                    </td>
                    <td class="font-bold text-white text-sm">{{ u.username }}</td>
                    <td class="text-slate-300">{{ u.email || '-' }}</td>
                    <td>
                      <span class="level-badge">Lv. {{ u.level }}</span>
                    </td>
                    <td class="text-right font-mono text-slate-400 font-semibold">{{ u.xp.toLocaleString() }} XP</td>
                  </tr>
                  <tr v-if="adminUsers.length === 0">
                    <td colspan="6" class="text-center py-10 text-slate-500">
                      No registered users found.
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- Footer Pagination -->
            <div class="table-footer" v-if="adminUsersTotalPages > 1">
              <button 
                class="btn-nav-pagination" 
                :disabled="adminUsersCurrentPage === 0"
                @click="loadAdminUsersList(adminUsersCurrentPage - 1)"
              >
                ◀ Previous
              </button>
              <span class="text-slate-400">
                Page {{ adminUsersCurrentPage + 1 }} of {{ adminUsersTotalPages }}
              </span>
              <button 
                class="btn-nav-pagination" 
                :disabled="adminUsersCurrentPage >= adminUsersTotalPages - 1"
                @click="loadAdminUsersList(adminUsersCurrentPage + 1)"
              >
                Next ▶
              </button>
            </div>
          </div>
        </main>
      </div>
    </div>

    <!-- Reused Modal for History Review / Preview inside Back Office -->
    <div v-if="isPreviewOpen && previewBoard" class="modal-overlay">
      <div class="modal-card">
        <div class="modal-header">
          <h5 class="modal-title">👁️ Puzzle Solution Preview</h5>
          <button @click="closeModal" class="btn-close-modal">
            <svg class="icon-w-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
              <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <p class="modal-desc">Previewing Stage: <strong class="text-white">{{ previewHistory?.stageName }}</strong></p>
          <div class="modal-canvas-frame">
            <NonogramCanvas :board="previewBoard" :readOnly="true" :initialAngle="0" />
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeModal" class="btn-close-action">
            Close Preview
          </button>
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
  fetchAdminUsers,
  approveStage, 
  deleteStage, 
  restoreStage, 
  loginAdmin, 
  logoutAdmin 
} from '../api/adminApi';
import type { AdminStageInfo } from '../api/adminApi';
import type { User } from '../api/userApi';

const logged = defineModel<boolean>('logged', { required: true });

const emit = defineEmits<{
  (e: 'stage-updated'): void;
}>();

const adminUsernameInput = ref('');
const adminPasswordInput = ref('');
const loginError = ref('');

// Tabs Configuration
const activeTab = ref<'dashboard' | 'puzzles' | 'users'>('dashboard');

// Stage List States
const adminStages = ref<AdminStageInfo[]>([]);
const adminStagesCurrentPage = ref(0);
const adminStagesTotalPages = ref(1);

const adminSearchQuery = ref('');
const adminSizeFilter = ref('All');
const adminStatusFilter = ref('All');
const adminSortKey = ref<'id' | 'name' | 'size' | 'status'>('id');
const adminSortOrder = ref<'asc' | 'desc'>('asc');

// User List States
const adminUsers = ref<User[]>([]);
const adminUsersCurrentPage = ref(0);
const adminUsersTotalPages = ref(1);
const totalUsersCount = ref(0);

// Preview States
const isPreviewOpen = ref(false);
const previewBoard = ref<PuzzleBoard | null>(null);
const previewHistory = ref<any>(null);

// Dashboard Metric Computations
const activePuzzlesCount = computed(() => {
  return adminStages.value.filter(s => s.approved && s.active).length;
});

const pendingPuzzlesCount = computed(() => {
  return adminStages.value.filter(s => !s.approved).length;
});

const v2StagesCount = computed(() => {
  return adminStages.value.filter(s => s.generatorVersion === 'V2').length;
});

const v1StagesCount = computed(() => {
  return adminStages.value.filter(s => !s.generatorVersion || s.generatorVersion === 'V1').length;
});

function countStagesBySize(size: number): number {
  return adminStages.value.filter(s => s.width === size && s.height === size).length;
}

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

async function loadAdminUsersList(page: number = 0) {
  try {
    const res = await fetchAdminUsers(page, 20);
    adminUsers.value = res.content || [];
    adminUsersTotalPages.value = res.totalPages || 1;
    adminUsersCurrentPage.value = res.number || 0;
    totalUsersCount.value = res.totalElements || res.content?.length || 0;
  } catch (error) {
    console.error('Failed to load admin users:', error);
  }
}

async function loadAllAdminData() {
  await loadAdminStagesList();
  await loadAdminUsersList();
}

function formatDate(dateString?: string) {
  if (!dateString) return '-';
  try {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch (e) {
    return dateString;
  }
}

function getFeedbackScore(s: AdminStageInfo) {
  return (s.upvotes || 0) - (s.downvotes || 0);
}

function getFeedbackScoreFormatted(s: AdminStageInfo) {
  const score = getFeedbackScore(s);
  if (score > 0) return `+${score}`;
  return String(score);
}

function getFeedbackClass(s: AdminStageInfo) {
  const score = getFeedbackScore(s);
  if (score > 0) return 'score-positive';
  if (score < 0) return 'score-negative';
  return 'score-neutral';
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

async function handleAdminLogin() {
  try {
    loginError.value = '';
    await loginAdmin(adminUsernameInput.value, adminPasswordInput.value);
    logged.value = true;
    adminUsernameInput.value = '';
    adminPasswordInput.value = '';
    await loadAllAdminData();
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
    loadAllAdminData();
  }
});

let tailwindLink: HTMLLinkElement | null = null;

onMounted(async () => {
  // Load Tailwind CSS CDN dynamically in the admin console ONLY
  tailwindLink = document.createElement('link');
  tailwindLink.rel = 'stylesheet';
  tailwindLink.href = 'https://cdn.jsdelivr.net/npm/tailwindcss@3.4.1/dist/tailwind.min.css';
  document.head.appendChild(tailwindLink);

  // Force #app to be full width in admin mode to eliminate gutters
  const appEl = document.getElementById('app');
  if (appEl) {
    appEl.style.width = '100%';
    appEl.style.maxWidth = '100%';
    appEl.style.padding = '0';
    appEl.style.margin = '0';
  }

  if (logged.value) {
    await loadAllAdminData();
  }
});

onUnmounted(() => {
  // Clean up Tailwind Play CSS when leaving admin page to avoid polluting other page styles
  if (tailwindLink && tailwindLink.parentNode) {
    tailwindLink.parentNode.removeChild(tailwindLink);
  }

  // Restore #app defaults for game mode
  const appEl = document.getElementById('app');
  if (appEl) {
    appEl.style.width = '';
    appEl.style.maxWidth = '';
    appEl.style.padding = '';
    appEl.style.margin = '';
  }
});
</script>

<style scoped>
/* Scoped Professional CSS Reset & Tailwind-friendly styles to prevent leaks */
.admin-backoffice-view {
  font-family: ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  background-color: #0b0f19;
  color: #f1f5f9;
  min-height: 100vh;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

/* Force CSS Overrides to block global style pollution on this view */
.admin-backoffice-view h1,
.admin-backoffice-view h2,
.admin-backoffice-view h3,
.admin-backoffice-view h4,
.admin-backoffice-view h5 {
  font-family: inherit !important;
  font-weight: 700 !important;
  color: #ffffff !important;
  margin: 0 !important;
  text-align: left !important;
  letter-spacing: -0.025em !important;
}

.admin-backoffice-view h1 {
  font-size: 1.125rem !important; /* 18px */
  line-height: 1.75rem !important;
}

.admin-backoffice-view h2 {
  font-size: 1.125rem !important;
  line-height: 1.75rem !important;
}

.admin-backoffice-view h3 {
  font-size: 1rem !important;
  line-height: 1.5rem !important;
}

.admin-backoffice-view p {
  margin: 0 !important;
}

.admin-backoffice-view select,
.admin-backoffice-view input {
  font-family: inherit !important;
  outline: none !important;
  box-sizing: border-box !important;
}

.admin-backoffice-view button {
  font-family: inherit !important;
  cursor: pointer !important;
  border: none !important;
  box-sizing: border-box !important;
}

/* Custom Styled Layout Classes that map to modern back-office views */
.login-screen-wrapper {
  display: flex;
  flex-grow: 1;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
  padding: 16px;
}

.login-card {
  width: 100%;
  max-width: 400px;
  padding: 32px;
  border-radius: 16px;
  background-color: #0f172a;
  border: 1px solid #1e293b;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
}

.login-header {
  text-align: center;
  margin-bottom: 24px;
}

.brand-logo-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background-color: #4f46e5;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  box-shadow: 0 10px 15px -3px rgba(79, 70, 229, 0.4);
}

.icon-key {
  width: 24px;
  height: 24px;
  color: #ffffff;
}

.brand-title {
  font-size: 1.5rem !important;
  margin-top: 16px !important;
  text-align: center !important;
}

.brand-subtitle {
  color: #94a3b8;
  font-size: 0.875rem;
  margin-top: 4px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #94a3b8;
  margin: 0;
}

.admin-input {
  width: 100%;
  padding: 10px 14px;
  border-radius: 8px;
  border: 1px solid #1e293b;
  background-color: #020617;
  color: #ffffff;
  font-size: 0.875rem;
  transition: border-color 0.15s;
}

.admin-input:focus {
  border-color: #4f46e5;
}

.admin-select {
  padding: 10px 14px;
  border-radius: 8px;
  border: 1px solid #1e293b;
  background-color: #020617;
  color: #ffffff;
  font-size: 0.875rem;
}

.login-error-alert {
  padding: 10px;
  border-radius: 8px;
  background-color: rgba(127, 29, 29, 0.4);
  border: 1px solid rgba(220, 38, 38, 0.5);
  color: #f87171;
  font-size: 0.75rem;
  text-align: center;
}

.btn-primary {
  background: linear-gradient(135deg, #4f46e5, #2563eb);
  color: #ffffff;
  font-weight: 600;
  border-radius: 8px;
  box-shadow: 0 4px 6px -1px rgba(79, 70, 229, 0.2);
  transition: opacity 0.15s;
}

.btn-primary:hover {
  opacity: 0.9;
}

.w-100 {
  width: 100%;
}

.py-3 {
  padding-top: 12px;
  padding-bottom: 12px;
}

.mt-2 {
  margin-top: 8px;
}

/* Console Layout */
.admin-content-wrapper {
  display: flex;
  flex-direction: column;
  flex-grow: 1;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  margin-bottom: 24px;
  border-radius: 16px;
  background-color: #0f172a;
  border: 1px solid #1e293b;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.header-brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-logo-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background-color: #4f46e5;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 6px -1px rgba(79, 70, 229, 0.3);
}

.icon-w-5 {
  width: 20px;
  height: 20px;
  color: #ffffff;
}

.header-title {
  font-size: 1.125rem !important;
}

.header-subtitle {
  color: #94a3b8;
  font-size: 0.75rem;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.session-badge {
  font-size: 0.75rem;
  background-color: rgba(79, 70, 229, 0.1);
  color: #818cf8;
  border: 1px solid rgba(79, 70, 229, 0.2);
  padding: 6px 12px;
  border-radius: 8px;
  font-weight: 600;
}

.btn-logout {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.75rem;
  background-color: rgba(239, 68, 68, 0.1);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.2);
  padding: 6px 12px;
  border-radius: 8px;
  font-weight: 600;
  transition: background-color 0.2s;
}

.btn-logout:hover {
  background-color: rgba(239, 68, 68, 0.2);
}

.icon-w-3_5 {
  width: 14px;
  height: 14px;
}

/* Dashboard Grid */
.admin-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
  align-items: stretch;
  flex-grow: 1;
}

@media (min-width: 1024px) {
  .admin-grid {
    grid-template-columns: 280px 1fr;
  }
}

/* Sidebar styling */
.admin-sidebar {
  display: flex;
  flex-direction: column;
  padding: 16px;
  border-radius: 16px;
  background-color: #0f172a;
  border: 1px solid #1e293b;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.sidebar-header {
  padding: 8px;
  margin-bottom: 16px;
  border-bottom: 1px solid rgba(30, 41, 59, 0.5);
}

.sidebar-title {
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #94a3b8;
}

.sidebar-nav {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-grow: 1;
}

.nav-btn {
  width: 100%;
  text-align: left;
  padding: 10px 16px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #94a3b8;
  background-color: transparent;
  transition: all 0.15s;
}

.nav-btn:hover {
  color: #e2e8f0;
  background-color: rgba(255, 255, 255, 0.05);
}

.nav-btn.active {
  color: #ffffff;
  background: linear-gradient(135deg, #4f46e5, #2563eb);
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.25);
  font-weight: 600;
}

.icon-w-4 {
  width: 16px;
  height: 16px;
}

.sidebar-footer {
  margin-top: auto;
  padding-top: 16px;
  border-top: 1px solid rgba(30, 41, 59, 0.5);
  text-align: center;
}

.version-label {
  font-size: 10px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: #64748b;
  font-weight: 600;
}

/* Main content viewport */
.admin-main {
  display: flex;
  flex-direction: column;
  flex-grow: 1;
}

/* Dashboard Panel */
.dashboard-panel {
  display: flex;
  flex-direction: column;
  gap: 24px;
  flex-grow: 1;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.125rem !important;
}

.icon-accent {
  color: #818cf8;
}

/* Metrics Cards Grid */
.metrics-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
}

@media (min-width: 768px) {
  .metrics-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

.metric-card {
  padding: 20px;
  border-radius: 16px;
  background-color: #0f172a;
  border: 1px solid #1e293b;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  transition: border-color 0.2s, transform 0.2s;
}

.metric-card:hover {
  border-color: #475569;
  transform: translateY(-2px);
}

.metric-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.metric-value {
  font-size: 1.875rem;
  font-weight: 800;
  margin-top: 12px;
}

.text-emerald {
  color: #34d399;
}

.text-amber {
  color: #fbbf24;
}

.text-indigo {
  color: #818cf8;
}

.progress-bar-container {
  width: 100%;
  background-color: #1e293b;
  height: 4px;
  border-radius: 9999px;
  margin-top: 16px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
}

.bg-blue {
  background-color: #3b82f6;
}

.bg-emerald {
  background-color: #10b981;
}

.bg-amber {
  background-color: #f59e0b;
}

.bg-indigo {
  background-color: #6366f1;
}

/* Info Panel */
.info-panel {
  padding: 24px;
  border-radius: 16px;
  background-color: #0f172a;
  border: 1px solid #1e293b;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-title {
  font-size: 1rem !important;
}

.info-desc {
  font-size: 0.875rem;
  color: #cbd5e1;
  line-height: 1.6;
}

.info-stats-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
  margin-top: 8px;
}

@media (min-width: 768px) {
  .info-stats-grid {
    grid-template-columns: 1fr 1fr;
  }
}

.stat-box {
  padding: 16px;
  border-radius: 12px;
  background-color: #020617;
  border: 1px solid rgba(30, 41, 59, 0.5);
}

.stat-box-title {
  display: block;
  font-size: 0.75rem;
  font-weight: 700;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 12px;
}

.stat-list {
  display: flex;
  flex-direction: column;
  font-size: 0.875rem;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
}

.stat-item.border-bottom {
  border-bottom: 1px solid rgba(30, 41, 59, 0.4);
}

/* Table Panel styling */
.table-panel {
  border-radius: 16px;
  background-color: #0f172a;
  border: 1px solid #1e293b;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  flex-grow: 1;
}

.table-header-section {
  padding: 20px;
  border-bottom: 1px solid #1e293b;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #0f172a;
}

.table-count-badge {
  font-size: 0.75rem;
  background-color: rgba(79, 70, 229, 0.1);
  color: #818cf8;
  border: 1px solid rgba(79, 70, 229, 0.2);
  padding: 4px 12px;
  border-radius: 9999px;
  font-weight: 600;
}

.table-toolbar {
  padding: 16px;
  background-color: rgba(2, 6, 23, 0.3);
  border-bottom: 1px solid #1e293b;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
}

.flex-grow {
  flex-grow: 1;
}

.min-w-240 {
  min-width: 240px;
}

.w-144 {
  width: 144px;
}

.w-160 {
  width: 160px;
}

.toolbar-input {
  padding: 8px 12px !important;
  font-size: 0.75rem !important;
}

/* Table wrapper and actual table design */
.table-wrapper {
  overflow-x: auto;
  max-height: 550px;
  flex-grow: 1;
}

.admin-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
  font-size: 0.75rem;
}

.admin-table th {
  background-color: #020617;
  color: #94a3b8;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding: 14px 20px;
  border-bottom: 1px solid #1e293b;
  position: sticky;
  top: 0;
  z-index: 1;
}

.admin-table th.cursor-pointer {
  cursor: pointer;
  user-select: none;
}

.admin-table td {
  padding: 14px 20px;
  border-bottom: 1px solid rgba(30, 41, 59, 0.6);
  color: #e2e8f0;
}

.admin-table tbody tr {
  transition: background-color 0.15s;
}

.admin-table tbody tr:hover {
  background-color: rgba(255, 255, 255, 0.02);
}

.font-mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
}

.size-badge {
  padding: 4px 8px;
  border-radius: 4px;
  background-color: #020617;
  color: #cbd5e1;
  border: 1px solid #1e293b;
}

.version-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 700;
  background-color: #1e293b;
  color: #94a3b8;
  border: 1px solid rgba(148, 163, 184, 0.2);
}

.version-badge.v2-badge {
  background-color: rgba(79, 70, 229, 0.1);
  color: #818cf8;
  border-color: rgba(79, 70, 229, 0.25);
}

.status-badge {
  padding: 4px 10px;
  border-radius: 9999px;
  font-size: 10px;
  font-weight: 700;
  border: 1px solid transparent;
}

.bg-emerald-light {
  background-color: rgba(16, 185, 129, 0.1);
}

.bg-amber-light {
  background-color: rgba(245, 158, 11, 0.1);
}

.bg-rose-light {
  background-color: rgba(244, 63, 94, 0.1);
}

.border-emerald-border {
  border-color: rgba(16, 185, 129, 0.2);
}

.border-amber-border {
  border-color: rgba(245, 158, 11, 0.2);
}

.border-rose-border {
  border-color: rgba(244, 63, 94, 0.2);
}

.feedback-values {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.text-right {
  text-align: right;
}

.btn-group {
  display: inline-flex;
  border-radius: 8px;
  border: 1px solid #1e293b;
  background-color: rgba(2, 6, 23, 0.6);
  overflow: hidden;
}

.btn-group button {
  padding: 6px 12px;
  color: #cbd5e1;
  background-color: transparent;
  transition: background-color 0.15s, color 0.15s;
}

.btn-group button:hover {
  background-color: #1e293b;
  color: #ffffff;
}

.border-left {
  border-left: 1px solid #1e293b;
}

.flex-center {
  display: flex;
  align-items: center;
}

.icon-margin-r {
  margin-right: 4px;
}

/* User management specific avatar */
.avatar-placeholder {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #020617;
  border: 1px solid #1e293b;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.level-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 700;
  background-color: rgba(79, 70, 229, 0.1);
  color: #818cf8;
  border: 1px solid rgba(79, 70, 229, 0.2);
}

/* Footer Pagination */
.table-footer {
  padding: 16px;
  border-top: 1px solid #1e293b;
  background-color: #0f172a;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.btn-nav-pagination {
  padding: 6px 12px;
  border-radius: 8px;
  border: 1px solid #1e293b;
  background-color: #020617;
  color: #cbd5e1;
  transition: background-color 0.15s;
}

.btn-nav-pagination:hover:not(:disabled) {
  background-color: #1e293b;
  color: #ffffff;
}

.btn-nav-pagination:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* Preview Modal Design */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(2, 6, 23, 0.8);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;
  padding: 16px;
}

.modal-card {
  width: 100%;
  max-width: 440px;
  background-color: #0f172a;
  border: 1px solid #1e293b;
  border-radius: 16px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  overflow: hidden;
}

.modal-header {
  padding: 16px 24px;
  border-bottom: 1px solid #1e293b;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.btn-close-modal {
  background-color: transparent;
  color: #94a3b8;
  transition: color 0.15s;
}

.btn-close-modal:hover {
  color: #ffffff;
}

.modal-body {
  padding: 24px;
  text-align: center;
}

.modal-desc {
  color: #94a3b8;
  font-size: 0.75rem;
  margin-bottom: 12px !important;
}

.modal-canvas-frame {
  width: 300px;
  height: 300px;
  background-color: #020617;
  border: 1px solid rgba(30, 41, 59, 0.8);
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #1e293b;
  background-color: rgba(2, 6, 23, 0.2);
  display: flex;
  justify-content: flex-end;
}

.btn-close-action {
  padding: 8px 16px;
  background-color: #334155;
  color: #e2e8f0;
  font-weight: 600;
  border-radius: 8px;
  transition: background-color 0.15s;
}

.btn-close-action:hover {
  background-color: #475569;
}

/* Custom helper alignment rules */
.justify-between {
  justify-content: space-between;
}

/* Feedback Net Score Badges */
.score-positive {
  color: #10b981 !important;
  font-weight: 700;
  background-color: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.2);
  padding: 2px 8px;
  border-radius: 9999px;
  display: inline-block;
}
.score-negative {
  color: #f43f5e !important;
  font-weight: 700;
  background-color: rgba(244, 63, 94, 0.1);
  border: 1px solid rgba(244, 63, 94, 0.2);
  padding: 2px 8px;
  border-radius: 9999px;
  display: inline-block;
}
.score-neutral {
  color: #94a3b8 !important;
  font-weight: 600;
  background-color: #1e293b;
  border: 1px solid rgba(148, 163, 184, 0.2);
  padding: 2px 8px;
  border-radius: 9999px;
  display: inline-block;
}

/* Scrollbar styling override */
.table-wrapper::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
.table-wrapper::-webkit-scrollbar-track {
  background: #0f172a;
}
.table-wrapper::-webkit-scrollbar-thumb {
  background: #334155;
  border-radius: 3px;
}
.table-wrapper::-webkit-scrollbar-thumb:hover {
  background: #475569;
}
</style>
