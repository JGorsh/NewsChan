package com.gorsh.rednews;

import com.gorsh.rednews.telegram.WriteReadBot;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
public class BotRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {

//        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
//        DefaultBotOptions botOptions = new DefaultBotOptions();
//        botOptions.setProxyHost("proxy.orb.ru");
//        botOptions.setProxyPort(3128);
//        botOptions.setProxyType(DefaultBotOptions.ProxyType.HTTP);
//        WriteReadBot bot = new WriteReadBot(botOptions);
//        botsApi.registerBot(bot);

    }
}
