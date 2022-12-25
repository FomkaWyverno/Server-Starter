package com.wyverno.ngrok.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigHandler extends AbstractConfig {



    public ConfigHandler(Path pathConfig) throws ConfigNotExistsException, IOException {
        super(pathConfig);
    }

    public static void createConfigFile(Path path) throws IOException {
        if (path.getParent() != null) {
            createConfigFile(path.getParent());
        }
        if (Files.notExists(path)) {
            createFileOrDirectory(path);
        }
    }

    private static void createFileOrDirectory(Path path) throws IOException {
        if (isFile(path)) {
            Files.createFile(path);
        } else {
            Files.createDirectory(path);
        }
    }

    public String getAuthToken() {
        return this.properties.getProperty("Auth-Token");
    }

    public String getApiKey() {
        return this.properties.getProperty("API-Key");
    }

    private static boolean isFile(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.matches(".+\\..+");
    }
}
