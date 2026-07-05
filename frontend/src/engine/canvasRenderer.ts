import { PuzzleBoard } from './puzzleBoard';
import type { CanvasConfig } from './coordinateMapper';
import { calculateLineHints } from './hintCalculator';

export interface RenderOptions {
  glowIntensity: number;
  glowBlur: number;
  cellSize: number;
}

function isArrayEqual(a: number[], b: number[]) {
  if (a.length !== b.length) return false;
  for (let i = 0; i < a.length; i++) {
    if (a[i] !== b[i]) return false;
  }
  return true;
}

const getHintParams = (cellSize: number) => {
  const fontSize = Math.max(8, Math.min(12, Math.floor(cellSize * 0.5) + 2));
  const spacing = Math.max(10, Math.min(16, Math.floor(cellSize * 0.6) + 4));
  const offset = Math.max(6, Math.min(10, Math.floor(cellSize * 0.3) + 1));
  return { fontSize, spacing, offset };
};

export function getBoardDimensions(board: PuzzleBoard, cellSize: number) {
  const { spacing } = getHintParams(cellSize);
  const boardWidth = board.colCount * cellSize;
  const boardHeight = board.rowCount * cellSize;
  const boardDiag = Math.sqrt(boardWidth * boardWidth + boardHeight * boardHeight);

  const maxRowHintsLength = Math.max(...board.rowHints.map(h => h.length), 1);
  const maxColHintsLength = Math.max(...board.colHints.map(h => h.length), 1);
  const hintPadding = Math.max(maxRowHintsLength, maxColHintsLength) * spacing + 40;

  const size = Math.ceil(boardDiag + hintPadding * 2);
  return {
    width: size,
    height: size,
    halfW: boardWidth / 2,
    halfH: boardHeight / 2
  };
}

export function drawNonogramBoard(
  ctx: CanvasRenderingContext2D,
  board: PuzzleBoard,
  config: CanvasConfig,
  options: RenderOptions
) {
  const isSolved = board.isSolved();
  const cellSizeVal = options.cellSize;
  const { width, height, halfW, halfH } = getBoardDimensions(board, cellSizeVal);

  // Clear canvas (sleek dark themed layout)
  ctx.fillStyle = '#0f172a';
  ctx.fillRect(0, 0, width, height);

  ctx.save();
  ctx.translate(config.centerX, config.centerY);
  ctx.rotate(config.angle);

  // Draw background for overall active board area
  if (isSolved && options.glowIntensity > 0) {
    ctx.save();
    ctx.shadowColor = `rgba(56, 189, 248, ${options.glowIntensity})`;
    ctx.shadowBlur = options.glowBlur;
    ctx.fillStyle = '#1e293b'; // slate-800
    ctx.fillRect(-halfW, -halfH, board.colCount * cellSizeVal, board.rowCount * cellSizeVal);
    ctx.restore();
  } else {
    ctx.fillStyle = '#1e293b'; // slate-800
    ctx.fillRect(-halfW, -halfH, board.colCount * cellSizeVal, board.rowCount * cellSizeVal);
  }

  // Draw grid cells
  for (let r = 0; r < board.rowCount; r++) {
    for (let c = 0; c < board.colCount; c++) {
      const x = -halfW + c * cellSizeVal;
      const y = -halfH + r * cellSizeVal;

      if (!isSolved) {
        ctx.strokeStyle = '#334155'; // slate-700
        ctx.lineWidth = 1;
        ctx.strokeRect(x, y, cellSizeVal, cellSizeVal);
      }

      const cellState = board.currentGrid[r][c];
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
    for (let r = 0; r <= board.rowCount; r += 5) {
      if (r > 0 && r < board.rowCount) {
        const y = -halfH + r * cellSizeVal;
        ctx.beginPath();
        ctx.moveTo(-halfW, y);
        ctx.lineTo(halfW, y);
        ctx.stroke();
      }
    }
    for (let c = 0; c <= board.colCount; c += 5) {
      if (c > 0 && c < board.colCount) {
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
    ctx.font = `bold ${fontSize}px sans-serif`;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';

    // Draw row hints (on the left side)
    for (let r = 0; r < board.rowCount; r++) {
      const hints = board.rowHints[r] || [0];
      const y = -halfH + r * cellSizeVal + cellSizeVal / 2;

      // Check if row hints are matched by player's current cells
      const rowCells = board.currentGrid[r];
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
    for (let c = 0; c < board.colCount; c++) {
      const hints = board.colHints[c] || [0];
      const x = -halfW + c * cellSizeVal + cellSizeVal / 2;

      // Check if column hints are matched by player's current cells
      const colCells: number[] = [];
      for (let r = 0; r < board.rowCount; r++) {
        colCells.push(board.currentGrid[r][c]);
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
