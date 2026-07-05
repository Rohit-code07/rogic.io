import { calculateHints } from './hintCalculator';
import { validateGrid } from './validator';

export class PuzzleBoard {
  public readonly rowCount: number;
  public readonly colCount: number;
  public readonly solutionGrid: number[][];
  public readonly currentGrid: number[][];
  public readonly rowHints: number[][];
  public readonly colHints: number[][];

  public undoStack: number[][][] = [];
  public redoStack: number[][][] = [];

  constructor(solutionGrid: number[][]) {
    if (!solutionGrid || solutionGrid.length === 0) {
      throw new Error('Solution grid cannot be empty');
    }

    this.rowCount = solutionGrid.length;
    this.colCount = solutionGrid[0].length;

    if (this.colCount === 0) {
      throw new Error('Solution grid width cannot be zero');
    }

    // Validate uniform row lengths
    for (let r = 0; r < this.rowCount; r++) {
      if (!solutionGrid[r] || solutionGrid[r].length !== this.colCount) {
        throw new Error('Solution grid must have uniform row lengths');
      }
    }

    this.solutionGrid = solutionGrid;

    // Initialize current board states to empty (0)
    this.currentGrid = Array.from({ length: this.rowCount }, () =>
      Array(this.colCount).fill(0)
    );

    // Calculate row & column hints
    const hints = calculateHints(solutionGrid);
    this.rowHints = hints.rowHints;
    this.colHints = hints.colHints;
  }

  public toggleFill(row: number, col: number): void {
    if (row < 0 || row >= this.rowCount || col < 0 || col >= this.colCount) return;
    const current = this.currentGrid[row][col];
    this.currentGrid[row][col] = current === 1 ? 0 : 1;
  }

  public toggleMark(row: number, col: number): void {
    if (row < 0 || row >= this.rowCount || col < 0 || col >= this.colCount) return;
    const current = this.currentGrid[row][col];
    this.currentGrid[row][col] = current === 2 ? 0 : 2;
  }

  public setCell(row: number, col: number, value: number): void {
    if (row < 0 || row >= this.rowCount || col < 0 || col >= this.colCount) return;
    if (value !== 0 && value !== 1 && value !== 2) return;
    this.currentGrid[row][col] = value;
  }

  public isSolved(): boolean {
    return validateGrid(this.currentGrid, this.rowHints, this.colHints);
  }

  public saveState(): void {
    this.undoStack.push(this.currentGrid.map(row => [...row]));
    this.redoStack = [];
  }

  public undo(): boolean {
    if (this.undoStack.length === 0) return false;
    const previous = this.undoStack.pop()!;
    this.redoStack.push(this.currentGrid.map(row => [...row]));
    for (let r = 0; r < this.rowCount; r++) {
      for (let c = 0; c < this.colCount; c++) {
        this.currentGrid[r][c] = previous[r][c];
      }
    }
    return true;
  }

  public redo(): boolean {
    if (this.redoStack.length === 0) return false;
    const next = this.redoStack.pop()!;
    this.undoStack.push(this.currentGrid.map(row => [...row]));
    for (let r = 0; r < this.rowCount; r++) {
      for (let c = 0; c < this.colCount; c++) {
        this.currentGrid[r][c] = next[r][c];
      }
    }
    return true;
  }

  public canUndo(): boolean {
    return this.undoStack.length > 0;
  }

  public canRedo(): boolean {
    return this.redoStack.length > 0;
  }

  public resetHistory(): void {
    this.undoStack = [];
    this.redoStack = [];
  }
}
