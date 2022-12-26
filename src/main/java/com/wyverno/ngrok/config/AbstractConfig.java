package com.wyverno.ngrok.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public abstract class AbstractConfig {

    protected Properties properties;
    public AbstractConfig(Path configPath) throws ConfigNotExistsException, IOException {
        if (Files.notExists(configPath)) {
            throw new ConfigNotExistsException();
        }

        this.properties = new Properties();
        this.properties.load(new BufferedReader(new FileReader(configPath.toFile())));
    }

    public String getProperty(String key) {
        return this.properties.getProperty(key);
    }
}
