package com.wyverno;

import com.wyverno.ngrok.Ngrok;
import java.io.IOException;
import java.nio.file.Paths;

public class Main {


    public static void main(String[] args) throws IOException {
        Ngrok ngrok = new Ngrok(Paths.get("config\\config.properties"));
        System.out.println(ngrok);
    }
}