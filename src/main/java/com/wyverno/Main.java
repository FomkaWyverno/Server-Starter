package com.wyverno;

import com.wyverno.config.Config;
import com.wyverno.discord.bot.DiscordBot;
import com.wyverno.ngrok.Ngrok;
import com.wyverno.config.ConfigHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

public class Main {


    private volatile static String line = "";
    private volatile static Ngrok ngrok;
    private static final Object lockNgrok = new Object();

    @Config(comment = "Need notify text channel in discord? default = true")
    public static boolean notify_Discord = false;

    public static void main(String[] args) throws IOException {
        ConfigHandler configHandler = new ConfigHandler(Paths.get("config\\config.properties"), Ngrok.class, Main.class, DiscordBot.class);
        notify_Discord = Boolean.parseBoolean(configHandler.getProperty("notify_discord"));
        System.out.printf("notify_Discord = %b%n", notify_Discord);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        Thread threadNgrok = new Thread(() -> {
             while (!line.equals("/stop")) {
                 try {
                     ngrok = new Ngrok(configHandler);
                     ngrok.start();
                     System.out.println(ngrok);
                     synchronized (lockNgrok) {
                         lockNgrok.notifyAll();
                     }
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

         Thread threadIP = new Thread(() -> {
             while (!line.equals("/stop")) {
                 Ngrok aliveNgrok =  getNgrok();
                 try {
                     String publicURL = aliveNgrok.getTunnel().getPublic_url();
                     System.out.println("PUBLIC URL >>> " + publicURL);
                     if (notify_Discord) {
                         System.out.println("Notify Discord");
                     } else {
                         System.out.println("Not need notify discord");
                     }
                     break;
                 } catch (NullPointerException e) {
                     System.out.println("Ngrok is null");
                     break;
                 }
             }
         }, "Thread Check ip");

         threadIP.start();
         try {
             while (!line.equals("/stop")) {
                 if (reader.ready()) {
                     line = reader.readLine();
                     if (line.equals("/stop")) {
                         ngrok.interrupt();
                         threadIP.interrupt();
                         break;
                     }
                 }
             }
             reader.close();
         } catch (IOException e) {
             System.out.println("Program is stop");
         }
    }

    private static Ngrok getNgrok() {
        try {
            synchronized (lockNgrok) {
                while (ngrok == null || !ngrok.isAliveNgrok()) {
                    System.out.printf("ngrok == null = %b || !ngrok.isAliveNgrok() = %b%n", ngrok == null, ngrok != null && !ngrok.isAliveNgrok());
                    System.out.println("Wait getNgrok()");
                    lockNgrok.wait();
                    System.out.println("Notify getNgrok()");
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("return Ngrok");
        return ngrok;
    }
}