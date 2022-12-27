package com.wyverno.ngrok.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wyverno.ngrok.NgrokTypeError;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

public class WebSocketNgrokConfig extends WebSocketServer {

    private ResponseConfigUI responseConfigUI;

    private volatile boolean isNeedStop = false;
    private NgrokTypeError ngrokTypeError;
    private final String AUTH_TOKEN;
    private final String API_KEY;

    public WebSocketNgrokConfig(int port, NgrokTypeError ngrokTypeError, String authToken, String apiKey) {
        super(new InetSocketAddress(port));
        this.ngrokTypeError = ngrokTypeError;
        this.AUTH_TOKEN = authToken;
        this.API_KEY = apiKey;
        Thread closingWebSocketThread = new Thread(() -> {
            while (true) {
                if (this.isNeedStop) {
                    try {
                        System.out.println("Try stop");
                        this.stop();
                        System.out.println("Server is stop");
                        break;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        closingWebSocketThread.setDaemon(true);
        closingWebSocketThread.start();
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        System.out.println("Connect client");

        boolean needAuthToken = false;
        boolean needApiKey = false;

        switch (this.ngrokTypeError) {
            case NotHasAuthToken: {
                needAuthToken = true;
                break;
            }
            case NotHasApiKey: {
                needApiKey = true;
                break;
            }
        }

        try {
            webSocket.send(new ObjectMapper().writeValueAsString(new RequestConfigUI(needAuthToken,this.AUTH_TOKEN,needApiKey,this.API_KEY)));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        this.isNeedStop = true;
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        System.out.println("We get message from client -> " + s);

        try {
            this.responseConfigUI = new ObjectMapper().readValue(s, ResponseConfigUI.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        e.printStackTrace();
        this.isNeedStop = true;
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket is launched");

        try {
            Desktop.getDesktop().browse(new File("HTMLs\\FixConfigNgrok.html").toURI());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ResponseConfigUI getResponseConfigUI() {
        return this.responseConfigUI;
    }
}
