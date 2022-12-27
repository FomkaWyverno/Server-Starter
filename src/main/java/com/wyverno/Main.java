package com.wyverno;

import com.wyverno.ngrok.ErrorInNgrokProcessException;
import com.wyverno.ngrok.Ngrok;
import java.io.IOException;
import java.nio.file.Paths;

public class Main {


    public static void main(String[] args) throws IOException, InterruptedException {
        while (true) {
            try {
                Ngrok ngrok = new Ngrok(Paths.get("config\\config.properties"));
                ngrok.start();
                System.out.println(ngrok);
                ngrok.join();
            } catch (ErrorInNgrokProcessException e) {
                System.out.println("Ngrok restart");
            }
        }
    }
}