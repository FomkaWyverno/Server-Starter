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
import java.util.Arrays;

public class WebSocketNgrokConfig extends WebSocketServer {

    private ResponseConfigUI responseConfigUI;

    private volatile boolean isNeedStop = false;
    private final NgrokTypeError[] ngrokTypeErrors;
    private final String AUTH_TOKEN;
    private final String API_KEY;

    private final int NGROK_PORT;

    public WebSocketNgrokConfig(int port, String authToken, String apiKey, int ngrokPort, NgrokTypeError... ngrokTypeErrors) {
        super(new InetSocketAddress(port));
        this.ngrokTypeErrors = ngrokTypeErrors;
        this.AUTH_TOKEN = authToken;
        this.API_KEY = apiKey;
        this.NGROK_PORT = ngrokPort;
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
        boolean needPort = false;

        for (NgrokTypeError ngrokTypeError : this.ngrokTypeErrors) {
            switch (ngrokTypeError) {
                case NotHasAuthToken:
                    needAuthToken = true;
                    break;
                case NotHasApiKey:
                    needApiKey = true;
                    break;
                case NotCorrectPort:
                    needPort = true;
                    break;
            }
        }

        System.out.println(Arrays.toString(this.ngrokTypeErrors));

        try {
            webSocket.send(new ObjectMapper().writeValueAsString(new RequestConfigUI(needAuthToken,this.AUTH_TOKEN,needApiKey,this.API_KEY, needPort, this.NGROK_PORT)));
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
