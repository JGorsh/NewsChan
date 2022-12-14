package com.gorsh.rednews.appconfig;

import com.gorsh.rednews.telegram.MyBotTelegram;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Setter
@Getter
@Configuration
@ConfigurationProperties()
@EnableScheduling
public class BotConfig {

    private DefaultBotOptions.ProxyType proxyType;

    @Bean
    public MyBotTelegram myTelegramBot() {

        TelegramBotsApi botsApi = null;
        try {
            botsApi = new TelegramBotsApi(DefaultBotSession.class);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        DefaultBotOptions botOptions = new DefaultBotOptions();

        botOptions.setProxyHost("proxy.orb.ru");
        botOptions.setProxyPort(3128);
        botOptions.setProxyType(DefaultBotOptions.ProxyType.HTTP);

        MyBotTelegram myBot = new MyBotTelegram(botOptions);
        try {
            botsApi.registerBot(myBot);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

        return myBot;
    }
}