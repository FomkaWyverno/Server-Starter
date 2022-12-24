package com.wyverno.ngrok.websocket;

import com.wyverno.ngrok.NgrokTypeError;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

public class WebSocketNgrokConfig extends WebSocketServer {

    private volatile boolean isNeedStop = false;
    private NgrokTypeError ngrokTypeError;

    public WebSocketNgrokConfig(int port, NgrokTypeError ngrokTypeError) {
        super(new InetSocketAddress(port));
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
        System.out.println("connect client");
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        this.isNeedStop = true;
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {

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
}
