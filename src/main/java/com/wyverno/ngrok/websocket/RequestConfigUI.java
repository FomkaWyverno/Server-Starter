package com.wyverno.ngrok.websocket;

public class RequestConfigUI {

    public RequestConfigUI(boolean needAuthToken, String authToken, boolean needApiKey, String apiKey) {
        this.needAuthToken = needAuthToken;
        this.authToken = authToken;
        this.needApiKey = needApiKey;
        this.apiKey = apiKey;
    }

    private boolean needAuthToken;
    private String authToken;

    private boolean needApiKey;
    private String apiKey;

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
