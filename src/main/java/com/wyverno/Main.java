package com.wyverno;

import com.wyverno.ngrok.Ngrok;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Main {


    public static void main(String[] args) throws IOException {
//        Ngrok ngrok = new Ngrok(25565,"eu");
//        ngrok.start();
//        ProcessBuilder pb = new ProcessBuilder("ngrok","tcp","80");
//        pb.redirectError(new File("auth_token_error.txt"));
//        Process process = pb.start();
//
//        process.waitFor();

        Socket socket = new Socket();

        socket.connect(new InetSocketAddress("4.tcp.eu.ngrok.io",13152));

        System.out.println("con");

        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String line;

        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    }
}