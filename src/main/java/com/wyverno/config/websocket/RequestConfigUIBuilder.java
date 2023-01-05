package com.wyverno.config.websocket;

public class RequestConfigUIBuilder {
    private boolean needAuthToken;
    private String authToken;
    private boolean needApiKey;
    private String apiKey;
    private boolean needPort;
    private int ngrokPort;

    public RequestConfigUIBuilder needAuthToken(boolean needAuthToken) {
        this.needAuthToken = needAuthToken;
        return this;
    }

    public RequestConfigUIBuilder authToken(String authToken) {
        this.authToken = authToken;
        return this;
    }

    public RequestConfigUIBuilder needApiKey(boolean needApiKey) {
        this.needApiKey = needApiKey;
        return this;
    }

    public RequestConfigUIBuilder apiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public RequestConfigUIBuilder needPort(boolean needPort) {
        this.needPort = needPort;
        return this;
    }

    public RequestConfigUIBuilder ngrokPort(int port) {
        this.ngrokPort = port;
        return this;
    }

    public RequestConfigUI build() {
        return new RequestConfigUI(this.needAuthToken,this.authToken,this.needApiKey,this.apiKey,this.needPort,this.ngrokPort);
    }
}
