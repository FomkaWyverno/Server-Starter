package com.wyverno.ngrok;

import com.wyverno.ngrok.config.Config;
import com.wyverno.ngrok.config.ConfigHandler;
import com.wyverno.ngrok.config.ConfigNotExistsException;
import com.wyverno.ngrok.websocket.ResponseConfigUI;
import com.wyverno.ngrok.websocket.WebSocketNgrokConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Ngrok extends Thread {

    @Config(comment = "Port to open for Network")
    private int PORT;

    @Config(comment = "The region in which you prefer to have a tunnel")
    private String REGION;

    @Config(comment = "Authorization token for join to your ngrok, you can get it -> https://dashboard.ngrok.com/get-started/your-authtoken")
    private String AUTH_TOKEN;

    @Config(comment = "API Key for this program can see your ngrok ip, you can get it -> https://dashboard.ngrok.com/api")
    private String API_KEY;

    private ProcessBuilder processBuilder;

    private ConfigHandler configHandler;

    public Ngrok(Path pathConfig) throws IOException, ErrorInNgrokProcessException {


        this.configHandler = new ConfigHandler(pathConfig);

        if (!this.configHandler.isHasConfigFile()) {
            fixConfig(NgrokTypeError.NotHasAuthToken);
        }

        List<Field> configFields = new ArrayList<>();
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Config.class)) {
                configFields.add(field);
            }
        }

        configFields.forEach(configField -> {
            try {
                Object value;
                String property = this.configHandler.getProperty(configField.getName().toLowerCase());
                if (configField.getType().getSimpleName()
                        .equals("int")) {
                    try {
                        value = Integer.parseInt(this.configHandler.getProperty(configField.getName().toLowerCase()));
                    } catch (NumberFormatException e) {
                        value = 25565;
                    }
                } else {
                    value = property;
                }
                configField.set(this, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    public void run() {
        if (this.processBuilder != null) {
            System.out.println("Ngrok is launched");
        }
        List<String> command = new ArrayList<>();

        command.add("ngrok");
        command.add("tcp");
        if (this.REGION != null) {
            command.add("--region");
            command.add(this.REGION);
        }
        command.add(String.valueOf(PORT));

        System.out.println("Command = " + command);
        this.processBuilder = new ProcessBuilder(command);

        Process process = null;
        try {
            process = this.processBuilder.start();
            listeningError(process.getErrorStream());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ErrorInNgrokProcessException ignored) {}
    }

    private void listeningError(InputStream is) throws ErrorInNgrokProcessException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;

            StringBuilder errorMessage = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                errorMessage.append(line).append("\n");
            }

            NgrokTypeError ngrokTypeError = NgrokTypeError.getTypeError(errorMessage.toString());

            if (ngrokTypeError == null) {
                System.err.println("Unknown error\n\n\n");

                System.err.println(errorMessage);

                return;
            }

            this.fixConfig(ngrokTypeError);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fixConfig(NgrokTypeError ngrokTypeError) throws ErrorInNgrokProcessException {
        WebSocketNgrokConfig wsServer = new WebSocketNgrokConfig(3535, ngrokTypeError,this.AUTH_TOKEN,this.API_KEY);
        wsServer.run();
        ResponseConfigUI responseConfigUI = wsServer.getResponseConfigUI();

        if (responseConfigUI != null) {
            this.configHandler.put("api_key",responseConfigUI.getApiKey());
            this.configHandler.put("auth_token", responseConfigUI.getAuthToken());
            try {
                this.configHandler.saveConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        throw new ErrorInNgrokProcessException();
    }

    @Override
    public String toString() {
        return "Ngrok{" +
                "PORT=" + PORT +
                ", REGION='" + REGION + '\'' +
                ", AUTH_TOKEN='" + AUTH_TOKEN + '\'' +
                ", API_KEY='" + API_KEY + '\'' +
                '}';
    }
}
