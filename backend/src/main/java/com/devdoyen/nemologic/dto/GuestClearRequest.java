package com.devdoyen.nemologic.dto;

public class GuestClearRequest {
    private Long stageId;
    private int elapsedTime;
    private String proofToken;

    public GuestClearRequest() {
    }

    public GuestClearRequest(Long stageId, int elapsedTime) {
        this.stageId = stageId;
        this.elapsedTime = elapsedTime;
    }

    public GuestClearRequest(Long stageId, int elapsedTime, String proofToken) {
        this.stageId = stageId;
        this.elapsedTime = elapsedTime;
        this.proofToken = proofToken;
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

    public String getProofToken() {
        return proofToken;
    }

    public void setProofToken(String proofToken) {
        this.proofToken = proofToken;
    }
}
