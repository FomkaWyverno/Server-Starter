package com.wyverno.discord.bot;

import com.wyverno.config.Config;
import com.wyverno.config.ConfigHandler;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class DiscordBot extends Thread {

    @Config(comment = "Token bot for authorization")
    private String TOKEN;

    @Config(comment = "Id text channel where bot will be send message with ip")
    private String TEXT_CHANNEL_ID;

    private final ConfigHandler configHandler;
    private final String TEXT_MESSAGE;

    public DiscordBot(ConfigHandler configHandler, String textMessage) {
        this.configHandler = configHandler;
        this.TEXT_MESSAGE = textMessage;

        List<Field> configFields = new ArrayList<>();

        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Config.class)) {
                configFields.add(field);
            }
        }

        configFields.forEach(field -> {
            String property = this.configHandler.getProperty(field.getName().toLowerCase());
            try {
                field.set(this, property);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void run() {
        System.out.println(this);
    }

    private synchronized void fixSelfConfig(DiscordBotError... discordBotErrors) {
        this.configHandler.fixConfig(discordBotErrors);
    }

    @Override
    public String toString() {
        return "DiscordBot{" +
                "TOKEN='" + TOKEN + '\'' +
                ", TEXT_CHANNEL_ID='" + TEXT_CHANNEL_ID + '\'' +
                ", TEXT_MESSAGE='" + TEXT_MESSAGE + '\'' +
                '}';
    }
}
