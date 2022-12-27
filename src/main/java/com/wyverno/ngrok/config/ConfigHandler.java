package com.wyverno.ngrok.config;

import com.wyverno.ngrok.Ngrok;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ConfigHandler {

    protected Properties properties;
    private final Path path;
    private boolean isHasConfigFile;
    public ConfigHandler(Path pathConfig) throws IOException {
        this.path = pathConfig;
        this.properties = new Properties();
        if (Files.notExists(pathConfig)) {
            this.isHasConfigFile = false;
            this.createPathAndConfigFile(this.path);
        } else {
            this.isHasConfigFile = true;
            this.properties.load(new BufferedReader(new FileReader(pathConfig.toFile())));
        }
    }

    public boolean isHasConfigFile() {
        return isHasConfigFile;
    }

    public String getProperty(String key) {
        return this.properties.getProperty(key);
    }

    public void createPathAndConfigFile(Path path) throws IOException {
        if (path.getParent() != null) {
            if (Files.notExists(path.getParent())) {
                createPathAndConfigFile(path.getParent());
            }
        }
        if (Files.notExists(path)) {
            if (isFile(path)) {
                createConfigFile(path);
            } else {
                Files.createDirectory(path);
            }
        }
    }

    private void createConfigFile(Path path) throws IOException {
        this.properties = new Properties();
        for (Field configField : Ngrok.class.getDeclaredFields()) { // Create empty keys
            if (configField.isAnnotationPresent(Config.class)) {
                this.properties.put(configField.getName().toLowerCase(),"");
            }
        }
        saveConfig();
    }

    public void saveConfig() throws IOException {
        StringBuilder comments = new StringBuilder("Information for variables\n\n");
        for (Field configField : Ngrok.class.getDeclaredFields()) { // Add comments
            if (configField.isAnnotationPresent(Config.class)) {
                Config configFieldAnnotation = configField.getAnnotation(Config.class);
                comments.append(configField.getName().toLowerCase())
                        .append(" = ")
                        .append(configFieldAnnotation.comment())
                        .append("\n");
            }
        }
        comments.append("\n");

        FileWriter fileWriter = new FileWriter(this.path.toFile());
        this.properties.store(fileWriter, comments.toString());
        fileWriter.flush();
        fileWriter.close();
    }

    public void put(String key, String value) {
        for (Field configField : Ngrok.class.getDeclaredFields()) {
            if (configField.isAnnotationPresent(Config.class)) {
                this.properties.put(key,value);
                return;
            }
        }
        System.out.println("This key - \"" + key + "\" don't has in Ngrok.class");
    }
    private static boolean isFile(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.matches(".+\\..+");
    }
}
