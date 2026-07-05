package com.devdoyen.nemologic.dto;

public class SolveVerificationResponse {
    private String token;

    public SolveVerificationResponse() {
    }

    public SolveVerificationResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
