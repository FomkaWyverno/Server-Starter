package com.wyverno.ngrok.config;

import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigHandler {
    public ConfigHandler() {
        System.out.println(Files.exists(Paths.get("config")));
    }
}
