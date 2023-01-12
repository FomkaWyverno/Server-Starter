package com.wyverno.discord.bot;

import com.wyverno.config.Config;
import com.wyverno.config.ConfigHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class DiscordBot extends Thread {

    private JDA jda;

    @Config(comment = "Token bot for authorization")
    private String TOKEN;

    @Config(comment = "Id text channel where bot will be send message with ip")
    private String TEXT_CHANNEL_ID;

    private final ConfigHandler configHandler;
    private final String TEXT_MESSAGE;

    private boolean isMessageSent = false;
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
        this.runProcess();
    }

    private void runProcess() {
        try {
            System.out.println(this);
            runDiscordBot();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Text ID Channel may be only Number!");
            this.fixSelfConfig(DiscordBotError.InvalidIDTextChat);
            this.shutdown();
        } catch (IllegalArgumentException e) {
            if (e.getMessage().equals("Token may not be empty")) {
                System.err.println("Discord Bot token is missing exception");
                this.fixSelfConfig(DiscordBotError.InvalidToken);
            } else if (e.getMessage().equals("ID may not be empty")) {
                System.err.println("Discord Bot Id Text Channel not be empty");
                this.fixSelfConfig(DiscordBotError.InvalidIDTextChat);
            } else {
                System.err.println("Other problem");
                e.printStackTrace();
            }
            this.shutdown();
        }
    }

    private void runDiscordBot() throws InterruptedException {
        this.jda = JDABuilder.createLight(this.TOKEN).build();
        TextChannel textChannel = jda.awaitReady().getTextChannelById(TEXT_CHANNEL_ID);
        if (textChannel != null) {
            textChannel.sendMessage(TEXT_MESSAGE).queue();
            this.isMessageSent = true;
        } else {
            this.fixSelfConfig(DiscordBotError.InvalidIDTextChat);
        }

        this.shutdown();
    }

    private synchronized void fixSelfConfig(DiscordBotError... discordBotErrors) {
        this.configHandler.fixConfig(discordBotErrors);
    }

    public boolean isMessageSent() {
        return isMessageSent;
    }

    public void shutdown() {
        this.jda.shutdown();
    }

    @Override
    public String toString() {
        return "DiscordBot {\n" +
                "TOKEN='" + TOKEN + '\'' +
                ",\nTEXT_CHANNEL_ID='" + TEXT_CHANNEL_ID + '\'' +
                ",\nTEXT_MESSAGE='" + TEXT_MESSAGE + '\'' +
                '}';
    }
}
