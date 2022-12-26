package com.wyverno.ngrok.config;

import com.wyverno.ngrok.Ngrok;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ConfigHandler extends AbstractConfig {

    public ConfigHandler(Path pathConfig) throws ConfigNotExistsException, IOException {
        super(pathConfig);
    }

    public static void createPathAndConfigFile(Path path) throws IOException {
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

    private static void createConfigFile(Path path) throws IOException {
        Properties p = new Properties();

        StringBuilder comments = new StringBuilder("Information for variables\n\n");
        for (Field configField : Ngrok.class.getDeclaredFields()) {
            if (configField.isAnnotationPresent(Config.class)) {
                Config configFieldAnnotation = configField.getAnnotation(Config.class);
                p.put(configField.getName().toLowerCase(),"");
                comments.append(configField.getName().toLowerCase())
                        .append(" = ")
                        .append(configFieldAnnotation.comment())
                        .append("\n");
            }
        }
        comments.append("\n");

        p.store(new FileWriter(path.toFile()), comments.toString());
    }
    private static boolean isFile(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.matches(".+\\..+");
    }
}
