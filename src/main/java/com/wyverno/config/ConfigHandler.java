package com.wyverno.config;

import com.sun.istack.internal.Nullable;
import com.wyverno.config.websocket.RequestConfigUI;
import com.wyverno.config.websocket.RequestConfigUIBuilder;
import com.wyverno.discord.bot.DiscordBotError;
import com.wyverno.ngrok.NgrokTypeError;
import com.wyverno.config.websocket.ResponseConfigUI;
import com.wyverno.config.websocket.WebSocketConfig;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Properties;

public class ConfigHandler {

    protected volatile Properties properties;
    private final Path path;

    private final Class<?>[] classesWithConfigFields;


    public ConfigHandler(Path pathConfig, Class<?>... classesWithConfigFields) throws IOException {
        this.path = pathConfig;
        this.properties = new Properties();
        this.classesWithConfigFields = classesWithConfigFields;
        if (Files.notExists(pathConfig)) {
            this.createPathAndConfigFile(this.path);
        } else {
            this.properties.load(new BufferedReader(new FileReader(pathConfig.toFile())));
            this.correctAllField();
        }
    }

    public boolean isHasConfigFile() {
        return Files.exists(this.path);
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

    private void correctAllField() throws IOException {
        boolean allHasField = true;
        boolean hasUnnecessaryFields = false;

        HashSet<String> configFields = new HashSet<>();
        for (Class<?> clazz : this.classesWithConfigFields) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Config.class)) {
                    configFields.add(field.getName().toLowerCase());
                    if (!this.properties.containsKey(field.getName().toLowerCase())) {
                        this.properties.put(field.getName().toLowerCase(),"");
                        allHasField = false;
                    }
                }
            }
        }

        for (String property : this.properties.stringPropertyNames()) {
            if (!configFields.contains(property)) {
                this.properties.remove(property);
                hasUnnecessaryFields = true;
            }
        }
        if (!allHasField || hasUnnecessaryFields) {
            this.saveConfig();
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

    public synchronized void fixConfig(DiscordBotError[] discordBotErrors, NgrokTypeError... ngrokTypeErrors) {
        int port;

        try {
            port = Integer.parseInt(properties.getProperty("port"));
        } catch (NumberFormatException e) {
            port = -1;
        }

        RequestConfigUI requestConfigUI = new RequestConfigUIBuilder()
                .needAuthToken(containsNgrokError(NgrokTypeError.NotHasAuthToken,ngrokTypeErrors))
                .needApiKey(containsNgrokError(NgrokTypeError.NotHasApiKey,ngrokTypeErrors))
                .needPort(containsNgrokError(NgrokTypeError.NotCorrectPort,ngrokTypeErrors))
                .authToken(properties.getProperty("auth_token"))
                .apiKey(properties.getProperty("api_key"))
                .ngrokPort(port)

                .needToken(containsDiscordError(DiscordBotError.InvalidToken, discordBotErrors))
                .needChannelID(containsDiscordError(DiscordBotError.InvalidIDTextChat, discordBotErrors))
                .token(this.properties.getProperty("token"))
                .channelID(this.properties.getProperty("text_channel_id"))
                .build();

        WebSocketConfig wsServer = new WebSocketConfig(3535, requestConfigUI);
        wsServer.run();
        ResponseConfigUI responseConfigUI = wsServer.getResponseConfigUI();

        if (responseConfigUI != null) {
            this.put("api_key",responseConfigUI.getApiKey());
            this.put("auth_token", responseConfigUI.getAuthToken());
            this.put("port", String.valueOf(responseConfigUI.getNgrokPort()));
            this.put("token", responseConfigUI.getToken());
            this.put("text_channel_id", responseConfigUI.getChannelID());
            try {
                this.saveConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean containsNgrokError(NgrokTypeError typeError, NgrokTypeError[] errors) {
        for (NgrokTypeError error : errors) {
            if (typeError.equals(error)) return true;
        }
        return false;
    }

    private boolean containsDiscordError(DiscordBotError typeError, DiscordBotError[] errors) {
        for (DiscordBotError error: errors) {
            if (typeError.equals(error)) return true;
        }
        return false;
    }
    public void printProperties() {
        System.out.println("Properties: " + this.properties.toString());
    }
}
