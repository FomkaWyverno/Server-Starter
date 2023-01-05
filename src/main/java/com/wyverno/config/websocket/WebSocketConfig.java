package com.wyverno.config.websocket;

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

public class WebSocketConfig extends WebSocketServer {

    private ResponseConfigUI responseConfigUI;

    private volatile boolean isNeedStop = false;

    private final RequestConfigUI requestConfigUI;
    public WebSocketConfig(int port, RequestConfigUI requestConfigUI) {
        super(new InetSocketAddress(port));
        this.requestConfigUI = requestConfigUI;
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

        try {
            String json = new ObjectMapper().writeValueAsString(this.requestConfigUI);
            System.out.println("JSON REQUEST >>> "+json);

            webSocket.send(json);
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
