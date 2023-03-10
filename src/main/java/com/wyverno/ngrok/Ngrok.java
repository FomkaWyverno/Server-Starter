package com.wyverno.ngrok;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wyverno.config.Config;
import com.wyverno.config.ConfigHandler;
import com.wyverno.discord.bot.DiscordBotError;
import com.wyverno.ngrok.tunnel.Tunnel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
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

    private volatile boolean isOtherProcessLaunched = false;

    private Tunnel tunnel;
    private final Object lockTunnel = new Object();

    private volatile boolean isAlive = false;

    private volatile boolean wasRequestFix = false;
    public Ngrok(ConfigHandler configHandler) throws IOException {
        super("Thread Ngrok");
        this.configHandler = configHandler;

        if (!this.configHandler.isHasConfigFile()) {
            this.fixSelfConfig(NgrokTypeError.NotHasAuthToken);
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
                        value = Integer.parseInt(property);
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
        this.isAlive = true;
        initConfigNgrok();
        startNgrok();
        try {
            Thread.sleep(2000);
            this.tunnel = getInformationAboutTunnel();
            System.out.println("Got Tunnel");
            synchronized (lockTunnel) {
                Thread.sleep(200);
                lockTunnel.notifyAll();
            }
            System.out.println("Join to Threads");
            if (this.threadErrorNgrok != null) {
                System.out.println("Join to threadErrorNgrok");
                this.threadErrorNgrok.join();
                System.out.println("Leave from threadErrorNgrok");
            }
            if (this.threadErrorApi != null) {
                System.out.println("Join to threadErrorApi");
                this.threadErrorApi.join();
                System.out.println("Leave from threadErrorApi");
            }
            if (this.processNgrok != null) {
                System.out.println("Join to processNgrok");
                this.processNgrok.waitFor();
                System.out.println("Leave from processNgrok");
            }
        } catch (InterruptedException e) {
            System.out.println("ngrok.run() interrupted");
        } finally {
            this.close();
        }
    }

    private void initConfigNgrok() {
        System.out.println("Initialize config");

        List<String> commandAddAuthToken = new ArrayList<>();

        List<NgrokTypeError> errorList = new ArrayList<>();

        commandAddAuthToken.add("ngrok");
        commandAddAuthToken.add("config");
        commandAddAuthToken.add("add-authtoken");
        if (this.AUTH_TOKEN != null) {
            commandAddAuthToken.add(this.AUTH_TOKEN);
        } else {
            errorList.add(NgrokTypeError.NotHasAuthToken);
        }

        List<String> commandAddApiKey = new ArrayList<>();

        commandAddApiKey.add("ngrok");
        commandAddApiKey.add("config");
        commandAddApiKey.add("add-api-key");
        if (this.API_KEY != null) {
            commandAddApiKey.add(this.API_KEY);
        } else {
            errorList.add(NgrokTypeError.NotHasApiKey);
        }

        if (!errorList.isEmpty()) {
            this.fixSelfConfig(errorList.toArray(new NgrokTypeError[0]));
            return;
        }

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
        if (this.REGION != null && !this.REGION.isEmpty()) {
            command.add("--region");
            command.add(this.REGION);
        } else if (this.REGION == null) {
            try {
                this.configHandler.saveConfig(); // re-save config file
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (this.PORT <= 0 || this.PORT > 65535) {
            this.fixSelfConfig(NgrokTypeError.NotCorrectPort);
            return;
        }
        command.add(String.valueOf(PORT));

        System.out.println("Command = " + command);
        this.processBuilder = new ProcessBuilder(command);


        try {
            this.processNgrok = this.processBuilder.start();
            this.threadErrorNgrok = new Thread(() -> {
                try {
                    listeningError(this.processNgrok.getErrorStream(), command);
                } catch (ErrorInNgrokProcessException ignored) {} catch (ProccesNgrokIsLaunchedException e) {
                    this.isOtherProcessLaunched = true;
                    this.close();
                }
            },"Thread Error Ngrok");
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
                } catch (ErrorInNgrokProcessException ignored) {} catch (ProccesNgrokIsLaunchedException e) {
                    this.isOtherProcessLaunched = true;
                    this.close();
                }
            }, "Thread Error Api");
            threadErrorApi.setDaemon(true);
            threadErrorApi.start();
            StringBuilder stringBuilder = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(pAPI.getInputStream()))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
            }

            if (!stringBuilder.toString().isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();

                JsonNode jsonNode = mapper.readTree(stringBuilder.toString());

                return mapper.treeToValue(jsonNode.get("tunnels").get(0),Tunnel.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Tunnel getTunnel() {
        System.out.println("getTunnel()");

        synchronized (lockTunnel) {
            while (this.tunnel == null) {
                try {
                    System.out.println("wait");
                    lockTunnel.wait();
                    System.out.println("NOTIFY");
                    if (!this.isAlive) {
                        System.out.println("Break from cycle so ngrok no longer alive");
                        break;
                    }
                } catch (InterruptedException e) {
                    System.out.println("Break from cycle so interrupted");
                    break;
                }
            }
        }
        return this.tunnel;
    }

    private void listeningError(InputStream is, List<String> command) throws ErrorInNgrokProcessException, ProccesNgrokIsLaunchedException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;

            StringBuilder errorMessage = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                errorMessage.append(line).append("\n");
            }

            if (errorMessage.toString().equals("200 OK\n")) {
                System.out.println(errorMessage); //skipped it not ERROR
                return;
            }

            if (errorMessage.toString().contains("ERR_NGROK_108")) {
                System.err.println("NGROK PROCESS ALREADY LAUNCHED");
                System.err.println(errorMessage);
                throw new ProccesNgrokIsLaunchedException();
            }
            NgrokTypeError ngrokTypeError = NgrokTypeError.getTypeError(errorMessage.toString());

            System.err.println(command.toString());
            if (ngrokTypeError == null) {
                System.err.println("Unknown error\n---\n---\n");
                System.err.println(errorMessage);
                return;
            }

            System.err.println(errorMessage);
            this.fixSelfConfig(ngrokTypeError);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isAliveNgrok() {
        return this.isAlive;
    }

    public boolean isOtherProcessLaunched() {
        return isOtherProcessLaunched;
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

    private synchronized void fixSelfConfig(NgrokTypeError... ngrokTypeErrors) {
        if (!wasRequestFix) {
            this.configHandler.fixConfig(new DiscordBotError[]{},ngrokTypeErrors);
            System.out.println("Fix Self Config");
            this.wasRequestFix = true;
            this.close();
        } else {
            System.out.println("Fix is not need");
        }
    }
    private void close() {
        if (this.threadErrorNgrok != null) this.threadErrorNgrok.interrupt();
        if (this.threadErrorApi != null ) this.threadErrorApi.interrupt();
        this.isAlive = false;
        synchronized (lockTunnel) {
            lockTunnel.notifyAll();
        }
        if (this.processNgrok != null) this.processNgrok.destroy();
        System.out.println("Ngrok close");
    }
}
