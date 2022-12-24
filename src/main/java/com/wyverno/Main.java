package com.wyverno;

import com.wyverno.ngrok.websocket.WebSocketNgrokConfig;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {


    public static void main(String[] args) throws IOException {
//        Ngrok ngrok = new Ngrok(25565,"eu");
//        ngrok.start();
//        ProcessBuilder pb = new ProcessBuilder("ngrok","tcp","80");
//        pb.redirectError(new File("auth_token_error.txt"));
//        Process process = pb.start();
//
//        process.waitFor();

        WebSocketNgrokConfig webSocket = new WebSocketNgrokConfig(new InetSocketAddress(3535));

        webSocket.run();
    }
}