package com.wyverno.config.websocket;

public class ResponseConfigUI {
    private String authToken;
    private String apiKey;

    private int ngrokPort;

    public int getNgrokPort() {
        return ngrokPort;
    }

    public void setNgrokPort(int ngrokPort) {
        this.ngrokPort = ngrokPort;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
