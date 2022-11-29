package com.gorsh.rednews;

import com.gorsh.rednews.reddit.RedditService;
import com.gorsh.rednews.service.MessagesDistribution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ReadRedditRunner implements CommandLineRunner{

    @Autowired
    RedditService redditService;

    @Autowired
    MessagesDistribution messagesDistribution;

    @Override
    public void run(String... args) throws Exception {

//        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
//        DefaultBotOptions botOptions = new DefaultBotOptions();
//        botOptions.setProxyHost("proxy.orb.ru");
//        botOptions.setProxyPort(3128);
//        botOptions.setProxyType(DefaultBotOptions.ProxyType.HTTP);
//        WriteReadBot bot = new WriteReadBot(botOptions);
//        botsApi.registerBot(bot);

        System.setProperty("https.proxyHost", "proxy.orb.ru");
        System.setProperty("https.proxyPort", "3128");

        Thread redditThread = new Thread(redditService);
        Thread messagesDistributionThread = new Thread(messagesDistribution);
        redditThread.start();
        messagesDistributionThread.start();
    }
}
