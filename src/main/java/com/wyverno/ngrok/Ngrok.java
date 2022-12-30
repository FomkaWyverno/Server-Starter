package com.wyverno.ngrok;

import com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl;
import com.wyverno.ngrok.config.Config;
import com.wyverno.ngrok.config.ConfigHandler;
import com.wyverno.ngrok.tunnel.Tunnel;
import com.wyverno.ngrok.websocket.ResponseConfigUI;
import com.wyverno.ngrok.websocket.WebSocketNgrokConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
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
    private Process processNgrok;

    private Thread threadErrorApi;
    private Thread threadErrorNgrok;
    private final ConfigHandler configHandler;

    private Tunnel tunnel;
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
                        value = -1;
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
        initConfigNgrok();
        startNgrok();
        try {
            Thread.sleep(2000);
            this.tunnel = getInformationAboutTunnel();
            this.processNgrok.waitFor();
        } catch (InterruptedException e) {
            this.threadErrorNgrok.interrupt();
            this.threadErrorApi.interrupt();
            this.processNgrok.destroy();
        }
    }

    private void initConfigNgrok() {
        System.out.println("Initialize config");

        List<String> commandAddAuthToken = new ArrayList<>();

        commandAddAuthToken.add("ngrok");
        commandAddAuthToken.add("config");
        commandAddAuthToken.add("add-authtoken");
        commandAddAuthToken.add(this.AUTH_TOKEN);

        List<String> commandAddApiKey = new ArrayList<>();

        commandAddApiKey.add("ngrok");
        commandAddApiKey.add("config");
        commandAddApiKey.add("add-api-key");
        commandAddApiKey.add(this.API_KEY);

        ProcessBuilder pbAuthToken = new ProcessBuilder(commandAddAuthToken);
        ProcessBuilder pbApiKey = new ProcessBuilder(commandAddApiKey);

        pbAuthToken.inheritIO();
        pbApiKey.inheritIO();

        try {
            pbAuthToken.start().waitFor();
            pbApiKey.start().waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void startNgrok() {
        if (this.processBuilder != null) {
            System.out.println("Ngrok is launched");
        }
        List<String> command = new ArrayList<>();

        command.add("ngrok");
        command.add("tcp");
        if (!this.REGION.isEmpty()) {
            command.add("--region");
            command.add(this.REGION);
        }

        if (this.PORT <= 0 || this.PORT > 65535) {
            try {
                fixConfig(NgrokTypeError.NotCorrectPort);
            } catch (ErrorInNgrokProcessException ignored) {
                return;
            }
        }
        command.add(String.valueOf(PORT));

        System.out.println("Command = " + command);
        this.processBuilder = new ProcessBuilder(command);


        try {
            this.processNgrok = this.processBuilder.start();
            this.threadErrorNgrok = new Thread(() -> {
                try {
                    listeningError(this.processNgrok.getErrorStream(), command);
                } catch (ErrorInNgrokProcessException ignored) {}
            });
            this.threadErrorNgrok.setDaemon(true);
            this.threadErrorNgrok.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Tunnel getInformationAboutTunnel() {

        List<String> commandApi = new ArrayList<>();
        Collections.addAll(commandApi,"ngrok","api","tunnels","list");

        ProcessBuilder pbAPI = new ProcessBuilder(commandApi);


        try {
            Process pAPI = pbAPI.start();
            this.threadErrorApi = new Thread(() -> {
                try {
                    listeningError(pAPI.getErrorStream(), commandApi);
                } catch (ErrorInNgrokProcessException ignored) {}
            });
            threadErrorApi.setDaemon(true);
            threadErrorApi.start();
            StringBuilder stringBuilder = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(pAPI.getInputStream()))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
            }

            System.out.println(stringBuilder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void listeningError(InputStream is, List<String> command) throws ErrorInNgrokProcessException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;

            StringBuilder errorMessage = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                errorMessage.append(line).append("\n");
            }

            NgrokTypeError ngrokTypeError = NgrokTypeError.getTypeError(errorMessage.toString());

            System.err.println(command.toString());
            if (ngrokTypeError == null) {
                System.err.println("Unknown error\n\n\n");
                System.err.println(errorMessage);
                return;
            }

            System.err.println(errorMessage);
            this.fixConfig(ngrokTypeError);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fixConfig(NgrokTypeError... ngrokTypeErrors) throws ErrorInNgrokProcessException {
        WebSocketNgrokConfig wsServer = new WebSocketNgrokConfig(3535,this.AUTH_TOKEN,this.API_KEY,this.PORT, ngrokTypeErrors);
        wsServer.run();
        ResponseConfigUI responseConfigUI = wsServer.getResponseConfigUI();

        if (responseConfigUI != null) {
            this.configHandler.put("api_key",responseConfigUI.getApiKey());
            this.configHandler.put("auth_token", responseConfigUI.getAuthToken());
            this.configHandler.put("port", String.valueOf(responseConfigUI.getNgrokPort()));
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
