package com.wyverno;

import com.wyverno.ngrok.Ngrok;
import java.io.*;

public class Main {


    public static void main(String[] args) throws IOException {
        Ngrok ngrok = new Ngrok(25565,"eu");
        ngrok.start();
//        ProcessBuilder pb = new ProcessBuilder("ngrok","tcp","80");
//        pb.redirectError(new File("auth_token_error.txt"));
//        Process process = pb.start();
//
//        process.waitFor();
    }
}