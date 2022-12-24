package com.wyverno.ngrok;

import com.wyverno.ngrok.websocket.WebSocketNgrokConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Ngrok extends Thread {

    private final int port;
    private final String region;

    private ProcessBuilder processBuilder;

    public Ngrok(int port) {
        this.port = port;
        region = null;
    }

    public Ngrok(int port, String region) {
        this.port = port;
        this.region = region;
    }

    public void run() {
        if (this.processBuilder != null) {
            System.out.println("Ngrok is launched");
        }
        List<String> command = new ArrayList<>();

        command.add("ngrok");
        command.add("tcp");
        if (this.region != null) {
            command.add("--region");
            command.add(this.region);
        }
        command.add(String.valueOf(port));

        this.processBuilder = new ProcessBuilder(command);

        Process process = null;
        try {
            process = this.processBuilder.start();
            listeningError(process.getErrorStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listeningError(InputStream is) {
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

    private void fixConfig(NgrokTypeError ngrokTypeError) {
        WebSocketNgrokConfig wsServerNgrokConfig = new WebSocketNgrokConfig(3535);

        wsServerNgrokConfig.start();
    }
}
