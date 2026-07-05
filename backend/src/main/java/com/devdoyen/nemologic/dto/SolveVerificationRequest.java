package com.devdoyen.nemologic.dto;

public class SolveVerificationRequest {
    private int[][] gridState;
    private int elapsedTime;
    private int rotationSteps;

    public SolveVerificationRequest() {
    }

    public SolveVerificationRequest(int[][] gridState, int elapsedTime, int rotationSteps) {
        this.gridState = gridState;
        this.elapsedTime = elapsedTime;
        this.rotationSteps = rotationSteps;
    }

    public int[][] getGridState() {
        return gridState;
    }

    public void setGridState(int[][] gridState) {
        this.gridState = gridState;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(int elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public int getRotationSteps() {
        return rotationSteps;
    }

    public void setRotationSteps(int rotationSteps) {
        this.rotationSteps = rotationSteps;
    }
}
