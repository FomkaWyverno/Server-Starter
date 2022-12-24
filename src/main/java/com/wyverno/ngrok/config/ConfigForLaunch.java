package com.wyverno.ngrok.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ConfigForLaunch extends AbstractConfig {



    public ConfigForLaunch(Path pathConfig) throws ConfigNotExistsException, IOException {
        super(pathConfig);
    }

    public static void createConfigFile(Path path) {

    }
    public String getAuthToken() {
        return this.properties.getProperty("Auth-Token");
    }

    public String getApiKey() {
        return this.properties.getProperty("API-Key");
    }
}
