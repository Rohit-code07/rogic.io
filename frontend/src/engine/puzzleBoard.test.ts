import { describe, it, expect } from 'vitest';
import { PuzzleBoard } from './puzzleBoard';

describe('PuzzleBoard Auto-Fill with X', () => {
  it('should initialize empty rows and columns with Xs (2) on construction', () => {
    // 3x3 board where:
    // row 0: [1, 1, 1] (hint: [3])
    // row 1: [0, 0, 0] (hint: [0])
    // row 2: [1, 0, 1] (hint: [1, 1])
    // col 1: [1, 0, 0] (hint: [1])
    const solution = [
      [1, 1, 1],
      [0, 0, 0],
      [1, 0, 1]
    ];
    const board = new PuzzleBoard(solution);

    // Row 1 target hint is [0], so it should be fully filled with Xs on construction
    expect(board.currentGrid[1]).toEqual([2, 2, 2]);

    // Other rows are not empty hints, so they should remain empty (0)
    expect(board.currentGrid[0]).toEqual([0, 0, 0]);
    expect(board.currentGrid[2]).toEqual([0, 0, 0]);
  });

  it('should auto-fill other empty cells with X in a row when its hint is satisfied', () => {
    const solution = [
      [1, 1, 0],
      [0, 1, 1],
      [1, 0, 1]
    ];
    const board = new PuzzleBoard(solution); // row 0 hint: [2], row 1 hint: [2], row 2 hint: [1, 1]

    // Fill row 0 cells to satisfy hint [2] (indices 0 and 1)
    board.setCell(0, 0, 1);
    expect(board.currentGrid[0]).toEqual([1, 0, 0]); // Not satisfied yet

    board.setCell(0, 1, 1);
    // Now row 0 is satisfied, so index 2 should be auto-filled with X (2)
    expect(board.currentGrid[0]).toEqual([1, 1, 2]);
  });

  it('should auto-fill other empty cells with X in a column when its hint is satisfied', () => {
    const solution = [
      [1, 0, 1],
      [1, 1, 0],
      [0, 1, 1]
    ];
    const board = new PuzzleBoard(solution); // col 0 hint: [2], col 1 hint: [2], col 2 hint: [1, 1]

    // Fill column 0 cells to satisfy hint [2] (rows 0 and 1)
    board.setCell(0, 0, 1);
    expect(board.currentGrid[0][0]).toBe(1);
    expect(board.currentGrid[2][0]).toBe(0);

    board.setCell(1, 0, 1);
    // Now col 0 is satisfied, so row 2 col 0 should be auto-filled with X (2)
    expect(board.currentGrid[0][0]).toBe(1);
    expect(board.currentGrid[1][0]).toBe(1);
    expect(board.currentGrid[2][0]).toBe(2);
  });

  it('should not auto-fill empty cells if row or column hint is not satisfied', () => {
    const solution = [
      [1, 1, 1],
      [1, 0, 1],
      [1, 1, 1]
    ];
    const board = new PuzzleBoard(solution); // row 0 hint: [3]

    // Partially satisfy row 0 (fill indices 0 and 1, leaving index 2 empty)
    board.setCell(0, 0, 1);
    board.setCell(0, 1, 1);

    // Remaining cell (index 2) should still be empty (0), not auto-filled with X
    expect(board.currentGrid[0]).toEqual([1, 1, 0]);
  });

  it('should capture auto-filled cells in undo/redo history stack', () => {
    const solution = [
      [1, 1, 0],
      [0, 1, 1],
      [1, 0, 1]
    ];
    const board = new PuzzleBoard(solution);

    // Save state before user sets the satisfying cell
    board.saveState();
    board.setCell(0, 0, 1);

    board.saveState();
    board.setCell(0, 1, 1); // Satisfies row 0 [2], index 2 turns to 2 (X)
    expect(board.currentGrid[0]).toEqual([1, 1, 2]);

    // Undo should revert the entire step including auto-filled Xs
    const undoSuccess = board.undo();
    expect(undoSuccess).toBe(true);
    expect(board.currentGrid[0]).toEqual([1, 0, 0]);

    // Redo should restore both the filled cell and the auto-filled Xs
    const redoSuccess = board.redo();
    expect(redoSuccess).toBe(true);
    expect(board.currentGrid[0]).toEqual([1, 1, 2]);
  });
});
