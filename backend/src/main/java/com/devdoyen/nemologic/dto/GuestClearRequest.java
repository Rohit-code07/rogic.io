package com.devdoyen.nemologic.dto;

public class GuestClearRequest {
    private Long stageId;
    private int elapsedTime;

    public GuestClearRequest() {
    }

    public GuestClearRequest(Long stageId, int elapsedTime) {
        this.stageId = stageId;
        this.elapsedTime = elapsedTime;
    }

    public Long getStageId() {
        return stageId;
    }

    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(int elapsedTime) {
        this.elapsedTime = elapsedTime;
    }
}
