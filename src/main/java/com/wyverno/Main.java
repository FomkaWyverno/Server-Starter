package com.wyverno;

import com.wyverno.ngrok.Ngrok;
import com.wyverno.config.ConfigHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

public class Main {


    private volatile static String line = "";
    private volatile static Ngrok ngrok;
    public static void main(String[] args) throws IOException {
        ConfigHandler configHandler = new ConfigHandler(Paths.get("config\\config.properties"), Ngrok.class);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        Thread threadNgrok = new Thread(() -> {
             while (!line.equals("/stop")) {
                 try {
                     ngrok = new Ngrok(configHandler);
                     ngrok.start();
                     System.out.println(ngrok);
                     ngrok.join();

                     if (ngrok.isOtherProcessLaunched()) {
                         reader.close();
                         break;
                     } else if (!line.equals("/stop")) {
                         System.out.println("Ngrok restart");
                     } else {
                         System.out.println("Program is stop");
                     }
                 } catch (IOException | InterruptedException e) {
                     e.printStackTrace();
                 }
             }
         });
         threadNgrok.start();
         try {
             while (!line.equals("/stop")) {
                 if (reader.ready()) {
                     line = reader.readLine();
                     if (line.equals("/stop")) {
                         ngrok.interrupt();
                         break;
                     }
                 }
             }
             reader.close();
         } catch (IOException e) {
             System.out.println("Program is stop");
         }
    }
}