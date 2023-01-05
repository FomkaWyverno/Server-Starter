package com.wyverno.ngrok.config;

import com.sun.istack.internal.Nullable;
import com.wyverno.ngrok.ErrorInNgrokProcessException;
import com.wyverno.ngrok.Ngrok;
import com.wyverno.ngrok.NgrokTypeError;
import com.wyverno.ngrok.websocket.ResponseConfigUI;
import com.wyverno.ngrok.websocket.WebSocketNgrokConfig;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ConfigHandler {

    protected Properties properties;
    private final Path path;
    private final boolean isHasConfigFile;

    private final Class<?>[] classesWithConfigFields;


    public ConfigHandler(Path pathConfig, Class<?>... classesWithConfigFields) throws IOException {
        this.path = pathConfig;
        this.properties = new Properties();
        this.classesWithConfigFields = classesWithConfigFields;
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

    @Nullable
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
                createConfigFile();
            } else {
                Files.createDirectory(path);
            }
        }
    }

    private void createConfigFile() throws IOException {
        this.properties = new Properties();
        for (Class<?> clazz : this.classesWithConfigFields) {
            for (Field configField : clazz.getDeclaredFields()) { // Create empty keys
                if (configField.isAnnotationPresent(Config.class)) {
                    this.properties.put(configField.getName().toLowerCase(),"");
                }
            }
        }
        saveConfig();
    }

    public void saveConfig() throws IOException {
        StringBuilder comments = new StringBuilder("Information for variables\n\n");
        for (Class<?> clazz : this.classesWithConfigFields) {
            for (Field configField : clazz.getDeclaredFields()) { // Add comments
                if (configField.isAnnotationPresent(Config.class)) {
                    Config configFieldAnnotation = configField.getAnnotation(Config.class);
                    comments.append(configField.getName().toLowerCase())
                            .append(" = ")
                            .append(configFieldAnnotation.comment())
                            .append("\n");
                }
            }
        }
        comments.append("\n");

        // Validate all variables is has?
        for (Class<?> clazz : this.classesWithConfigFields) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Config.class)) {
                    if (!this.properties.containsKey(field.getName().toLowerCase())) {
                        this.properties.put(field.getName().toLowerCase(),"");
                        System.out.println("The \"" + field.getName().toLowerCase() + "\" field was removed and restored to the property");
                    }
                }
            }
        }

        FileWriter fileWriter = new FileWriter(this.path.toFile());
        this.properties.store(fileWriter, comments.toString());
        fileWriter.flush();
        fileWriter.close();

        System.out.println("Properties: " + this.properties.toString());
    }

    public void put(String key, String value) {
        for (Class<?> clazz : this.classesWithConfigFields) {
            for (Field configField : clazz.getDeclaredFields()) {
                if (configField.isAnnotationPresent(Config.class)) {
                    this.properties.put(key,value);
                    return;
                }
            }
            System.out.println("This key - \"" + key + "\" don't has in " + clazz.getName() +".class");
        }
    }
    private static boolean isFile(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.matches(".+\\..+");
    }

    public synchronized void fixConfig(String authToken, String apiKey, int port, NgrokTypeError... ngrokTypeErrors) {
        WebSocketNgrokConfig wsServer = new WebSocketNgrokConfig(3535,authToken, apiKey, port, ngrokTypeErrors);
        wsServer.run();
        ResponseConfigUI responseConfigUI = wsServer.getResponseConfigUI();

        if (responseConfigUI != null) {
            this.put("api_key",responseConfigUI.getApiKey());
            this.put("auth_token", responseConfigUI.getAuthToken());
            this.put("port", String.valueOf(responseConfigUI.getNgrokPort()));
            try {
                this.saveConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
