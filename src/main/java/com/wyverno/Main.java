package com.wyverno;

import com.wyverno.ngrok.Ngrok;
import com.wyverno.ngrok.websocket.WebSocketNgrokConfig;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {


    public static void main(String[] args) throws IOException, InterruptedException {
        Ngrok ngrok = new Ngrok(25565,"eu");
        ngrok.start();
    }
}