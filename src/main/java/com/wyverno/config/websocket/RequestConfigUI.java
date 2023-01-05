package com.wyverno.config.websocket;

public class RequestConfigUI {

    public RequestConfigUI(boolean needAuthToken, String authToken, boolean needApiKey, String apiKey, boolean needPort, int ngrokPort) {
        this.needAuthToken = needAuthToken;
        this.authToken = authToken;
        this.needApiKey = needApiKey;
        this.apiKey = apiKey;
        this.needPort = needPort;
        this.ngrokPort = ngrokPort;
    }

    private boolean needAuthToken;
    private String authToken;

    private boolean needApiKey;
    private String apiKey;

    private boolean needPort;

    private int ngrokPort;

    public boolean isNeedPort() {
        return needPort;
    }

    public void setNeedPort(boolean needPort) {
        this.needPort = needPort;
    }

    public int getNgrokPort() {
        return ngrokPort;
    }

    public void setNgrokPort(int ngrokPort) {
        this.ngrokPort = ngrokPort;
    }

    public boolean isNeedAuthToken() {
        return needAuthToken;
    }

    public void setNeedAuthToken(boolean needAuthToken) {
        this.needAuthToken = needAuthToken;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public boolean isNeedApiKey() {
        return needApiKey;
    }

    public void setNeedApiKey(boolean needApiKey) {
        this.needApiKey = needApiKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
