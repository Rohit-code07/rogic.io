<template>
  <div class="nonogram-canvas-container">
    <div 
      class="canvas-frame" 
      ref="frameRef"
      @mousedown="handleMouseDown"
      @touchstart="handleTouchStart"
      @wheel="handleWheel"
      @contextmenu.prevent
    >
      <div class="canvas-anim-wrapper">
        <canvas 
          ref="canvasRef" 
          data-testid="nonogram-canvas" 
          :style="canvasStyle"
        ></canvas>
      </div>
    </div>

    <!-- Floating Draw Mode Toggle -->
    <div v-if="!readOnly" class="draw-mode-hud" @click="toggleDrawMode" title="Toggle Draw Mode" style="cursor: pointer;">
      <div class="draw-mode-slider" :class="drawMode"></div>
      <button 
        class="draw-mode-btn" 
        :class="{ active: drawMode === 'fill' }" 
        @click.stop="toggleDrawMode"
        title="Fill Mode"
        type="button"
      >
        <span class="mode-icon fill-icon"></span>
      </button>
      <button 
        class="draw-mode-btn" 
        :class="{ active: drawMode === 'x' }" 
        @click.stop="toggleDrawMode"
        title="X Mark Mode"
        type="button"
      >
        <svg class="mode-icon x-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <path d="M18 6L6 18M6 6l12 12" stroke-width="3.5" stroke-linecap="round"/>
        </svg>
      </button>
    </div>

    <!-- Floating History (Undo/Redo) HUD -->
    <div v-if="!readOnly" class="history-hud">
      <button 
        class="history-btn" 
        @click="handleUndo" 
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
        @click="handleRedo" 
        :disabled="!canRedo" 
        title="Redo (Ctrl+Y)" 
        type="button"
      >
        <svg class="history-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M21 7v6h-6M3 17a9 9 0 0 1 9-9 9 9 0 0 1 6 2.3l3 2.7" />
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, onUnmounted, computed } from 'vue';
import { PuzzleBoard } from '../engine/puzzleBoard';
import { getGridCoordinates } from '../engine/coordinateMapper';
import { calculateLineHints } from '../engine/hintCalculator';

const props = defineProps<{
  board: PuzzleBoard;
  readOnly?: boolean;
  initialAngle?: number;
  rotationSteps?: number;
}>();

const emit = defineEmits<{
  (e: 'cell-click'): void;
  (e: 'solve-animation-complete'): void;
}>();

const canvasRef = ref<HTMLCanvasElement | null>(null);
const drawMode = ref<'fill' | 'x'>('fill');

function toggleDrawMode() {
  drawMode.value = drawMode.value === 'fill' ? 'x' : 'fill';
}

// Standard grid layout dimensions
const getCellSize = (maxCount: number) => {
  if (maxCount <= 10) return 30;
  if (maxCount <= 15) return 24;
  if (maxCount <= 20) return 20;
  if (maxCount <= 25) return 18;
  return 16; // 30x30 or larger
};

function isArrayEqual(a: number[], b: number[]) {
  if (a.length !== b.length) return false;
  for (let i = 0; i < a.length; i++) {
    if (a[i] !== b[i]) return false;
  }
  return true;
}

const CELL_SIZE = computed(() => getCellSize(Math.max(props.board.colCount, props.board.rowCount)));

const getHintParams = (cellSize: number) => {
  const fontSize = Math.max(8, Math.min(12, Math.floor(cellSize * 0.5) + 2));
  const spacing = Math.max(10, Math.min(16, Math.floor(cellSize * 0.6) + 4));
  const offset = Math.max(6, Math.min(10, Math.floor(cellSize * 0.3) + 1));
  return { fontSize, spacing, offset };
};

const playAngle = props.initialAngle !== undefined ? props.initialAngle : 0;
const targetOrthogonalAngle = computed(() => {
  return (props.initialAngle !== undefined ? props.initialAngle : 0) - (props.rotationSteps || 0) * Math.PI / 2;
});

// Function to select starting angle
const getStartingAngle = () => {
  if (props.board.isSolved()) {
    return targetOrthogonalAngle.value;
  }
  return playAngle;
};

const isTestEnv = typeof window !== 'undefined' && (
  (globalThis as any).process?.env?.NODE_ENV === 'test' ||
  (globalThis as any).vitest !== undefined ||
  (globalThis as any).__vitest_worker__ !== undefined ||
  navigator.userAgent.includes('jsdom')
);

const currentAngle = ref(getStartingAngle());

// Dynamic calculations for bounds
const getDimensions = () => {
  const cellSizeVal = CELL_SIZE.value;
  const { spacing } = getHintParams(cellSizeVal);
  const boardWidth = props.board.colCount * cellSizeVal;
  const boardHeight = props.board.rowCount * cellSizeVal;
  const boardDiag = Math.sqrt(boardWidth * boardWidth + boardHeight * boardHeight);

  const maxRowHintsLength = Math.max(...props.board.rowHints.map(h => h.length), 1);
  const maxColHintsLength = Math.max(...props.board.colHints.map(h => h.length), 1);
  const hintPadding = Math.max(maxRowHintsLength, maxColHintsLength) * spacing + 40;

  const size = Math.ceil(boardDiag + hintPadding * 2);
  return {
    width: size,
    height: size,
    halfW: boardWidth / 2,
    halfH: boardHeight / 2
  };
};

const scale = ref(1.0);
const isDragging = ref(false);
const showSolveImpact = ref(false);
const glowIntensity = ref(0.0);
const glowBlur = ref(20);
let glowAnimationId: any = null;

const frameRef = ref<HTMLElement | null>(null);
const frameWidth = ref(600);
const frameHeight = ref(600);

const updateFrameSize = () => {
  if (frameRef.value) {
    frameWidth.value = frameRef.value.clientWidth || 600;
    frameHeight.value = frameRef.value.clientHeight || 600;
  }
};

const fitScale = computed(() => {
  if (isTestEnv) return 1.0;
  const { width: canvasSize } = getDimensions();
  const scaleX = frameWidth.value / canvasSize;
  const scaleY = frameHeight.value / canvasSize;
  return Math.min(scaleX, scaleY);
});

const offsetX = ref(0);
const offsetY = ref(0);
const isPanning = ref(false);
let panStartX = 0;
let panStartY = 0;

const canvasStyle = computed(() => {
  const transitionTime = props.board.isSolved() ? '0.3s' : '0.15s';
  const transitionStyle = ((isDragging.value || isPanning.value) && !props.board.isSolved()) 
    ? 'none' 
    : `transform ${transitionTime} cubic-bezier(0.2, 0.8, 0.2, 1)`;
  return {
    transform: `translate(${offsetX.value}px, ${offsetY.value}px) scale(${scale.value})`,
    transformOrigin: 'center center',
    transition: transitionStyle
  };
});

function clampOffsets() {
  const visibleWidth = props.board.colCount * CELL_SIZE.value;
  const visibleHeight = props.board.rowCount * CELL_SIZE.value;
  const scaledWidth = visibleWidth * scale.value;
  const scaledHeight = visibleHeight * scale.value;
  
  // Keep at least 80 pixels overlap with the frame
  const minOverlap = 80;
  
  const maxOffsetX = Math.max(0, frameWidth.value / 2 + scaledWidth / 2 - minOverlap);
  const maxOffsetY = Math.max(0, frameHeight.value / 2 + scaledHeight / 2 - minOverlap);
  
  offsetX.value = Math.max(-maxOffsetX, Math.min(maxOffsetX, offsetX.value));
  offsetY.value = Math.max(-maxOffsetY, Math.min(maxOffsetY, offsetY.value));
}

function handleWheel(event: WheelEvent) {
  event.preventDefault();
  const zoomFactor = event.deltaY < 0 ? 1.05 : 0.95;
  scale.value = Math.max(0.2, Math.min(4.0, scale.value * zoomFactor));
  clampOffsets();
}

const initialDims = getDimensions();
const config = {
  centerX: initialDims.width / 2,
  centerY: initialDims.height / 2,
  cellSize: CELL_SIZE.value,
  rowCount: props.board.rowCount,
  colCount: props.board.colCount,
  angle: currentAngle.value
};

function drawBoard() {
  const canvas = canvasRef.value;
  if (!canvas) return;

  const { width, height, halfW, halfH } = getDimensions();
  const cellSizeVal = CELL_SIZE.value;
  if (canvas.width !== width) {
    canvas.width = width;
  }
  if (canvas.height !== height) {
    canvas.height = height;
  }

  const ctx = canvas.getContext('2d');
  if (!ctx) return;

  const isSolved = props.board.isSolved();

  config.centerX = width / 2;
  config.centerY = height / 2;
  config.angle = currentAngle.value;
  config.rowCount = props.board.rowCount;
  config.colCount = props.board.colCount;
  config.cellSize = cellSizeVal;

  // Clear canvas (sleek dark themed layout)
  ctx.fillStyle = '#0f172a';
  ctx.fillRect(0, 0, width, height);

  ctx.save();
  ctx.translate(config.centerX, config.centerY);
  ctx.rotate(config.angle);

  // Draw background for overall active board area
  if (isSolved && glowIntensity.value > 0) {
    ctx.save();
    ctx.shadowColor = `rgba(56, 189, 248, ${glowIntensity.value})`;
    ctx.shadowBlur = glowBlur.value;
    ctx.fillStyle = '#1e293b'; // slate-800
    ctx.fillRect(-halfW, -halfH, props.board.colCount * cellSizeVal, props.board.rowCount * cellSizeVal);
    ctx.restore();
  } else {
    ctx.fillStyle = '#1e293b'; // slate-800
    ctx.fillRect(-halfW, -halfH, props.board.colCount * cellSizeVal, props.board.rowCount * cellSizeVal);
  }

  // Draw grid cells
  for (let r = 0; r < props.board.rowCount; r++) {
    for (let c = 0; c < props.board.colCount; c++) {
      const x = -halfW + c * cellSizeVal;
      const y = -halfH + r * cellSizeVal;

      if (!isSolved) {
        ctx.strokeStyle = '#334155'; // slate-700
        ctx.lineWidth = 1;
        ctx.strokeRect(x, y, cellSizeVal, cellSizeVal);
      }

      const cellState = props.board.currentGrid[r][c];
      if (cellState === 1) {
        // Filled with premium gem gradient
        const grad = ctx.createLinearGradient(x, y, x + cellSizeVal, y + cellSizeVal);
        grad.addColorStop(0, '#38bdf8'); // sky-400
        grad.addColorStop(1, '#818cf8'); // indigo-400
        ctx.fillStyle = grad;

        if (isSolved) {
          // Seamless full cell fill for clean pixel art when solved
          ctx.fillRect(x, y, cellSizeVal, cellSizeVal);
        } else {
          // Play mode: cell margin and border stroke
          ctx.fillRect(x + 1.5, y + 1.5, cellSizeVal - 3, cellSizeVal - 3);
          ctx.strokeStyle = '#6366f1';
          ctx.lineWidth = 1.5;
          ctx.strokeRect(x + 1.5, y + 1.5, cellSizeVal - 3, cellSizeVal - 3);
        }
      } else if (cellState === 2 && !isSolved) {
        // Marked (X) - Translucent slate grey
        ctx.strokeStyle = 'rgba(148, 163, 184, 0.45)';
        ctx.lineWidth = 2.0;
        ctx.beginPath();
        ctx.moveTo(x + cellSizeVal / 4, y + cellSizeVal / 4);
        ctx.lineTo(x + 3 * cellSizeVal / 4, y + 3 * cellSizeVal / 4);
        ctx.moveTo(x + 3 * cellSizeVal / 4, y + cellSizeVal / 4);
        ctx.lineTo(x + cellSizeVal / 4, y + 3 * cellSizeVal / 4);
        ctx.stroke();
      }
    }
  }

  // Draw bold line markers every 5 lines only when active (not solved)
  if (!isSolved) {
    ctx.strokeStyle = '#64748b'; // slate-500
    ctx.lineWidth = 2.5;
    for (let r = 0; r <= props.board.rowCount; r += 5) {
      if (r > 0 && r < props.board.rowCount) {
        const y = -halfH + r * cellSizeVal;
        ctx.beginPath();
        ctx.moveTo(-halfW, y);
        ctx.lineTo(halfW, y);
        ctx.stroke();
      }
    }
    for (let c = 0; c <= props.board.colCount; c += 5) {
      if (c > 0 && c < props.board.colCount) {
        const x = -halfW + c * cellSizeVal;
        ctx.beginPath();
        ctx.moveTo(x, -halfH);
        ctx.lineTo(x, halfH);
        ctx.stroke();
      }
    }
  }

  // Draw hints ONLY if not solved
  if (!isSolved) {
    const { fontSize, spacing, offset } = getHintParams(cellSizeVal);

    // Draw row hints (on the left side)
    ctx.font = `bold ${fontSize}px sans-serif`;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';

    for (let r = 0; r < props.board.rowCount; r++) {
      const hints = props.board.rowHints[r] || [0];
      const y = -halfH + r * cellSizeVal + cellSizeVal / 2;

      // Check if row hints are matched by player's current cells
      const rowCells = props.board.currentGrid[r];
      const rowLine = rowCells.map(val => val === 1 ? 1 : 0);
      const rowCurrentHints = calculateLineHints(rowLine);
      const isRowMatching = isArrayEqual(rowCurrentHints, hints);

      ctx.fillStyle = isRowMatching ? '#475569' : '#94a3b8'; // Fade to slate-600 if completed correctly

      for (let h = 0; h < hints.length; h++) {
        const hintVal = hints[hints.length - 1 - h];
        const hx = -halfW - offset - h * spacing;
        
        ctx.save();
        ctx.translate(hx, y);
        ctx.rotate(-config.angle);
        ctx.fillText(hintVal.toString(), 0, 0);
        ctx.restore();
      }
    }

    // Draw col hints (above the grid)
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';

    for (let c = 0; c < props.board.colCount; c++) {
      const hints = props.board.colHints[c] || [0];
      const x = -halfW + c * cellSizeVal + cellSizeVal / 2;

      // Check if column hints are matched by player's current cells
      const colCells: number[] = [];
      for (let r = 0; r < props.board.rowCount; r++) {
        colCells.push(props.board.currentGrid[r][c]);
      }
      const colLine = colCells.map(val => val === 1 ? 1 : 0);
      const colCurrentHints = calculateLineHints(colLine);
      const isColMatching = isArrayEqual(colCurrentHints, hints);

      ctx.fillStyle = isColMatching ? '#475569' : '#94a3b8'; // Fade to slate-600 if completed correctly

      for (let h = 0; h < hints.length; h++) {
        const hintVal = hints[hints.length - 1 - h];
        const hy = -halfH - offset - h * spacing;
        
        ctx.save();
        ctx.translate(x, hy);
        ctx.rotate(-config.angle);
        ctx.fillText(hintVal.toString(), 0, 0);
        ctx.restore();
      }
    }
  }

  ctx.restore();
}

let dragValue = 0; // 0: empty, 1: filled, 2: marked
let lastRow = -1;
let lastCol = -1;

function getCoordinatesFromEvent(clientX: number, clientY: number) {
  const canvas = canvasRef.value;
  if (!canvas) return null;
  const rect = canvas.getBoundingClientRect();
  const currentScale = isTestEnv ? 1.0 : (rect.width / canvas.width);
  const clickX = (clientX - rect.left) / currentScale;
  const clickY = (clientY - rect.top) / currentScale;
  return getGridCoordinates(clickX, clickY, config);
}

const canUndo = ref(false);
const canRedo = ref(false);

function updateHistoryFlags() {
  canUndo.value = props.board.canUndo();
  canRedo.value = props.board.canRedo();
}

function handleUndo() {
  if (props.readOnly) return;
  const success = props.board.undo();
  if (success) {
    updateHistoryFlags();
    drawBoard();
    emit('cell-click');
  }
}

function handleRedo() {
  if (props.readOnly) return;
  const success = props.board.redo();
  if (success) {
    updateHistoryFlags();
    drawBoard();
    emit('cell-click');
  }
}

function handleKeyDown(event: KeyboardEvent) {
  if (props.readOnly) return;
  if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'z') {
    event.preventDefault();
    if (event.shiftKey) {
      handleRedo();
    } else {
      handleUndo();
    }
  } else if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'y') {
    event.preventDefault();
    handleRedo();
  }
}

function handlePanMouseMove(event: MouseEvent) {
  if (!isPanning.value) return;
  offsetX.value = event.clientX - panStartX;
  offsetY.value = event.clientY - panStartY;
  clampOffsets();
}

function handlePanMouseUp() {
  if (isPanning.value) {
    isPanning.value = false;
    window.removeEventListener('mousemove', handlePanMouseMove);
    window.removeEventListener('mouseup', handlePanMouseUp);
  }
}

function handlePanTouchMove(event: TouchEvent) {
  if (!isPanning.value) return;
  event.preventDefault();

  if (event.touches.length === 1) {
    const touch = event.touches[0];
    offsetX.value = touch.clientX - panStartX;
    offsetY.value = touch.clientY - panStartY;
  } else if (event.touches.length > 1) {
    const t1 = event.touches[0];
    const t2 = event.touches[1];
    const midX = (t1.clientX + t2.clientX) / 2;
    const midY = (t1.clientY + t2.clientY) / 2;
    offsetX.value = midX - panStartX;
    offsetY.value = midY - panStartY;
  }
  clampOffsets();
}

function handlePanTouchEnd() {
  if (isPanning.value) {
    isPanning.value = false;
    window.removeEventListener('touchmove', handlePanTouchMove);
    window.removeEventListener('touchend', handlePanTouchEnd);
    window.removeEventListener('touchcancel', handlePanTouchEnd);
  }
}

function handleMouseDown(event: MouseEvent) {
  if (props.readOnly) return;

  // Handle middle click panning
  if (event.button === 1) {
    isPanning.value = true;
    panStartX = event.clientX - offsetX.value;
    panStartY = event.clientY - offsetY.value;
    window.addEventListener('mousemove', handlePanMouseMove);
    window.addEventListener('mouseup', handlePanMouseUp);
    return;
  }

  const coords = getCoordinatesFromEvent(event.clientX, event.clientY);
  
  // If clicked outside the grid (coords is null) and it is left click, start panning!
  if (!coords) {
    if (event.button === 0) {
      isPanning.value = true;
      panStartX = event.clientX - offsetX.value;
      panStartY = event.clientY - offsetY.value;
      window.addEventListener('mousemove', handlePanMouseMove);
      window.addEventListener('mouseup', handlePanMouseUp);
    }
    return;
  }

  const { row, col } = coords;
  const currentValue = props.board.currentGrid[row][col];

  if (event.button === 2) {
    // Right click always acts as Mark toggle
    dragValue = currentValue === 2 ? 0 : 2;
  } else if (event.button === 0) {
    // Left click respects current drawMode
    if (drawMode.value === 'fill') {
      dragValue = currentValue === 1 ? 0 : 1;
    } else {
      dragValue = currentValue === 2 ? 0 : 2;
    }
  } else {
    return;
  }

  // Save undo state before changing cell
  props.board.saveState();
  updateHistoryFlags();

  isDragging.value = true;
  props.board.setCell(row, col, dragValue);
  lastRow = row;
  lastCol = col;

  drawBoard();
  emit('cell-click');

  window.addEventListener('mousemove', handleWindowMouseMove);
  window.addEventListener('mouseup', handleWindowMouseUp);
}

function handleWindowMouseMove(event: MouseEvent) {
  if (!isDragging.value) return;

  const coords = getCoordinatesFromEvent(event.clientX, event.clientY);
  if (!coords) return;

  const { row, col } = coords;
  if (row !== lastRow || col !== lastCol) {
    props.board.setCell(row, col, dragValue);
    lastRow = row;
    lastCol = col;
    drawBoard();
    emit('cell-click');
  }
}

function handleWindowMouseUp() {
  if (isDragging.value) {
    isDragging.value = false;
    window.removeEventListener('mousemove', handleWindowMouseMove);
    window.removeEventListener('mouseup', handleWindowMouseUp);
  }
}

function handleTouchStart(event: TouchEvent) {
  if (props.readOnly) return;

  // Handle multi-touch panning
  if (event.touches.length > 1) {
    isPanning.value = true;
    const t1 = event.touches[0];
    const t2 = event.touches[1];
    const midX = (t1.clientX + t2.clientX) / 2;
    const midY = (t1.clientY + t2.clientY) / 2;
    panStartX = midX - offsetX.value;
    panStartY = midY - offsetY.value;
    window.addEventListener('touchmove', handlePanTouchMove, { passive: false });
    window.addEventListener('touchend', handlePanTouchEnd);
    window.addEventListener('touchcancel', handlePanTouchEnd);
    return;
  }

  if (event.touches.length !== 1) return;
  const touch = event.touches[0];
  const coords = getCoordinatesFromEvent(touch.clientX, touch.clientY);

  // If touched outside the grid, start panning!
  if (!coords) {
    isPanning.value = true;
    panStartX = touch.clientX - offsetX.value;
    panStartY = touch.clientY - offsetY.value;
    window.addEventListener('touchmove', handlePanTouchMove, { passive: false });
    window.addEventListener('touchend', handlePanTouchEnd);
    window.addEventListener('touchcancel', handlePanTouchEnd);
    return;
  }

  event.preventDefault(); // Prevent page scroll/zoom gestures during drawing

  const { row, col } = coords;
  const currentValue = props.board.currentGrid[row][col];

  if (drawMode.value === 'fill') {
    dragValue = currentValue === 1 ? 0 : 1;
  } else {
    dragValue = currentValue === 2 ? 0 : 2;
  }

  // Save undo state before changing cell
  props.board.saveState();
  updateHistoryFlags();

  isDragging.value = true;
  props.board.setCell(row, col, dragValue);
  lastRow = row;
  lastCol = col;

  drawBoard();
  emit('cell-click');

  window.addEventListener('touchmove', handleWindowTouchMove, { passive: false });
  window.addEventListener('touchend', handleWindowTouchEnd);
  window.addEventListener('touchcancel', handleWindowTouchEnd);
}

function handleWindowTouchMove(event: TouchEvent) {
  if (!isDragging.value || event.touches.length !== 1) return;
  event.preventDefault();

  const touch = event.touches[0];
  const coords = getCoordinatesFromEvent(touch.clientX, touch.clientY);
  if (!coords) return;

  const { row, col } = coords;
  if (row !== lastRow || col !== lastCol) {
    props.board.setCell(row, col, dragValue);
    lastRow = row;
    lastCol = col;
    drawBoard();
    emit('cell-click');
  }
}

function handleWindowTouchEnd() {
  if (isDragging.value) {
    isDragging.value = false;
    window.removeEventListener('touchmove', handleWindowTouchMove);
    window.removeEventListener('touchend', handleWindowTouchEnd);
    window.removeEventListener('touchcancel', handleWindowTouchEnd);
  }
}

function startSolvedGlowAnimation() {
  stopGlowAnimation();
  const startTime = performance.now();

  function tick(now: number) {
    if (!props.board.isSolved()) {
      stopGlowAnimation();
      return;
    }

    const elapsed = now - startTime;
    if (elapsed < 200) {
      // 0 to 200ms: shoot up to 1.0 (flash)
      const progress = elapsed / 200;
      glowIntensity.value = progress * 1.0;
      glowBlur.value = 15 + progress * 20; // 15 to 35 blur
    } else if (elapsed < 1200) {
      // 200ms to 1200ms: decay down to 0.35
      const progress = (elapsed - 200) / 1000;
      const ease = 1 - Math.pow(1 - progress, 2);
      glowIntensity.value = 1.0 - (1.0 - 0.35) * ease;
      glowBlur.value = 35 - (35 - 20) * ease;
    } else {
      // After 1200ms: gentle pulse infinitely
      const pulseElapsed = now - (startTime + 1200);
      const pulse = Math.sin(pulseElapsed / 600) * 0.08;
      glowIntensity.value = 0.35 + pulse;
      glowBlur.value = 20 + Math.sin(pulseElapsed / 600) * 4;
    }

    drawBoard();
    glowAnimationId = requestAnimationFrame(tick);
  }

  glowAnimationId = requestAnimationFrame(tick);
}

function stopGlowAnimation() {
  if (glowAnimationId !== null) {
    cancelAnimationFrame(glowAnimationId);
    glowAnimationId = null;
  }
  glowIntensity.value = 0.0;
}

function animateRotationToTarget() {
  const targetAngle = targetOrthogonalAngle.value;
  if (isTestEnv) {
    currentAngle.value = targetAngle;
    offsetX.value = 0;
    offsetY.value = 0;
    scale.value = fitScale.value;
    glowIntensity.value = 0.35;
    drawBoard();
    showSolveImpact.value = true;
    emit('solve-animation-complete');
    return;
  }

  const duration = 1000; // 1 second
  const startAngle = currentAngle.value;
  const startOffsetX = offsetX.value;
  const startOffsetY = offsetY.value;
  const startScale = scale.value;
  const targetScale = fitScale.value;
  const startTime = performance.now();

  function tick(now: number) {
    const elapsed = now - startTime;
    const progress = Math.min(elapsed / duration, 1);

    // Easing: easeInOutCubic
    const ease = progress < 0.5 
      ? 4 * progress * progress * progress 
      : 1 - Math.pow(-2 * progress + 2, 3) / 2;

    currentAngle.value = startAngle + (targetAngle - startAngle) * ease;
    offsetX.value = startOffsetX + (0 - startOffsetX) * ease;
    offsetY.value = startOffsetY + (0 - startOffsetY) * ease;
    scale.value = startScale + (targetScale - startScale) * ease;
    drawBoard();

    if (progress < 1) {
      requestAnimationFrame(tick);
    } else {
      showSolveImpact.value = true;
      startSolvedGlowAnimation();
      emit('solve-animation-complete');
    }
  }

  requestAnimationFrame(tick);
}

let resizeObserver: ResizeObserver | null = null;

onMounted(() => {
  window.addEventListener('keydown', handleKeyDown);
  if (frameRef.value) {
    updateFrameSize();
    if (typeof ResizeObserver !== 'undefined') {
      resizeObserver = new ResizeObserver(() => {
        updateFrameSize();
      });
      resizeObserver.observe(frameRef.value);
    }
  }
  scale.value = fitScale.value;
  drawBoard();
});

onUnmounted(() => {
  stopGlowAnimation();
  window.removeEventListener('keydown', handleKeyDown);
  window.removeEventListener('mousemove', handleWindowMouseMove);
  window.removeEventListener('mouseup', handleWindowMouseUp);
  window.removeEventListener('touchmove', handleWindowTouchMove);
  window.removeEventListener('touchend', handleWindowTouchEnd);
  window.removeEventListener('touchcancel', handleWindowTouchEnd);
  window.removeEventListener('mousemove', handlePanMouseMove);
  window.removeEventListener('mouseup', handlePanMouseUp);
  window.removeEventListener('touchmove', handlePanTouchMove);
  window.removeEventListener('touchend', handlePanTouchEnd);
  window.removeEventListener('touchcancel', handlePanTouchEnd);
  if (resizeObserver) {
    resizeObserver.disconnect();
  }
});

watch(fitScale, (newFitScale) => {
  scale.value = newFitScale;
});

// Redraw if board changes
watch(() => props.board, (newBoard) => {
  stopGlowAnimation();
  showSolveImpact.value = false;
  currentAngle.value = getStartingAngle();
  scale.value = fitScale.value;
  offsetX.value = 0;
  offsetY.value = 0;
  if (newBoard) {
    newBoard.resetHistory();
    updateHistoryFlags();
  }
  const dims = getDimensions();
  config.centerX = dims.width / 2;
  config.centerY = dims.height / 2;
  config.cellSize = CELL_SIZE.value;
  config.rowCount = props.board.rowCount;
  config.colCount = props.board.colCount;
  config.angle = currentAngle.value;
  drawBoard();
}, { deep: false, immediate: true });

// Watch for solved state to rotate to target
watch(() => props.board.isSolved(), (solved) => {
  if (solved) {
    animateRotationToTarget();
  }
});
</script>

<style scoped>
.nonogram-canvas-container {
  position: relative;
  display: block;
  padding: 0;
  background-color: transparent;
  border-radius: 12px;
  width: 100%;
  height: 100%;
  min-width: 0;
  min-height: 0;
}

.canvas-frame {
  width: 100%;
  height: 100%;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #0f172a;
  border-radius: 0 0 8px 8px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  border-top: none;
  position: relative;
  cursor: pointer;
}

canvas {
  display: block;
  cursor: pointer;
  position: absolute;
  -webkit-tap-highlight-color: transparent;
  -webkit-touch-callout: none;
  user-select: none;
  touch-action: none;
}

/* Floating Draw Mode HUD */
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

/* Floating History (Undo/Redo) HUD */
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
  .draw-mode-hud {
    bottom: 12px;
    left: 50%;
    transform: translateX(-50%);
  }
  .history-hud {
    bottom: 12px;
    left: 12px;
  }
}

.canvas-anim-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 8px;
  overflow: hidden;
}

</style>
