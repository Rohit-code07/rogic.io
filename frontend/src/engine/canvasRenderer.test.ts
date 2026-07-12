import { describe, it, expect, vi } from 'vitest';
import { drawNonogramBoard } from './canvasRenderer';
import { PuzzleBoard } from './puzzleBoard';

describe('canvasRenderer - Off-screen Hint Indicators TDD', () => {
  const mockCanvasContext = () => {
    return {
      save: vi.fn(),
      restore: vi.fn(),
      translate: vi.fn(),
      rotate: vi.fn(),
      fillRect: vi.fn(),
      strokeRect: vi.fn(),
      beginPath: vi.fn(),
      moveTo: vi.fn(),
      lineTo: vi.fn(),
      stroke: vi.fn(),
      fillText: vi.fn(),
      arc: vi.fn(),
      fill: vi.fn(),
      createLinearGradient: vi.fn(() => ({
        addColorStop: vi.fn()
      })),
      fillStyle: '',
      strokeStyle: '',
      lineWidth: 1,
      font: '',
      textAlign: '',
      textBaseline: '',
      shadowColor: '',
      shadowBlur: 0
    } as unknown as CanvasRenderingContext2D;
  };

  it('should draw normal hint numbers (fillText) when they are within viewport bounds', () => {
    const solution = [
      [1, 1],
      [1, 0]
    ];
    const board = new PuzzleBoard(solution);
    const config = {
      centerX: 200,
      centerY: 200,
      cellSize: 20,
      rowCount: 2,
      colCount: 2,
      angle: 0
    };
    const ctx = mockCanvasContext();

    // With scale=1.0 and pan=0, everything is on-screen
    drawNonogramBoard(ctx, board, config, {
      glowIntensity: 0,
      glowBlur: 0,
      cellSize: 20,
      scale: 1.0,
      offsetX: 0,
      offsetY: 0,
      frameWidth: 400,
      frameHeight: 400
    });

    // fillText should be called to draw the hint numbers
    expect(ctx.fillText).toHaveBeenCalled();
    // arc should NOT be called since no hints are off-screen
    expect(ctx.arc).not.toHaveBeenCalled();
  });

  it('should draw dot indicators (arc/fill) instead of text (fillText) when hints are scrolled off-screen', () => {
    const solution = [
      [1, 1],
      [1, 0]
    ];
    const board = new PuzzleBoard(solution);
    const config = {
      centerX: 200,
      centerY: 200,
      cellSize: 20,
      rowCount: 2,
      colCount: 2,
      angle: 0
    };
    const ctx = mockCanvasContext();

    // Move the board far to the right and down (offsetX=300, offsetY=300),
    // and zoom in (scale=2.0) so the hints (left/above the grid) go way off-screen
    drawNonogramBoard(ctx, board, config, {
      glowIntensity: 0,
      glowBlur: 0,
      cellSize: 20,
      scale: 2.0,
      offsetX: 300,
      offsetY: 300,
      frameWidth: 200,
      frameHeight: 200
    });

    // Since hints are off-screen, fillText should NOT be called for hint numbers,
    // and arc should be called to draw the dot markers instead.
    expect(ctx.fillText).not.toHaveBeenCalled();
    expect(ctx.arc).toHaveBeenCalled();
  });
});
