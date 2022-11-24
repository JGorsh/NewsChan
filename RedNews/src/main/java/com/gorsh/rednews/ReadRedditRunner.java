package com.gorsh.rednews;

import com.gorsh.rednews.reddit.RedditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ReadRedditRunner implements CommandLineRunner{

    @Autowired
    RedditService redditService;
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

        Thread thread = new Thread(redditService);
        thread.start();
    }
}
