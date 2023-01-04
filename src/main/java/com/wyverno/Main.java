package com.wyverno;

import com.wyverno.ngrok.ErrorInNgrokProcessException;
import com.wyverno.ngrok.Ngrok;
import com.wyverno.ngrok.config.ConfigHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

public class Main {


    private volatile static String line = "";
    private volatile static Ngrok ngrok;
    public static void main(String[] args) throws IOException, InterruptedException {
        ConfigHandler configHandler = new ConfigHandler(Paths.get("config\\config.properties"), Ngrok.class);
         Thread threadNgrok = new Thread(() -> {
             while (!line.equals("/stop")) {
                 try {
                     ngrok = new Ngrok(configHandler);
                     ngrok.start();
                     System.out.println(ngrok);
                     ngrok.join();
                 } catch (ErrorInNgrokProcessException e) {
                     System.out.println("Ngrok restart");
                 } catch (IOException | InterruptedException e) {
                     e.printStackTrace();
                 }
             }
         });
         threadNgrok.start();
         try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
             while ((line = reader.readLine()) != null) {
                 if (line.equals("/stop")) {
                     ngrok.interrupt();
                     break;
                 }
             }
         }
    }
}