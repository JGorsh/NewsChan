package com.gorsh.rednews;

import com.gorsh.rednews.reddit.RedditService;
import com.gorsh.rednews.telegram.MessagesDistribution;
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
    public void run(String... args){

        System.setProperty("https.proxyHost", "proxy.orb.ru");
        System.setProperty("https.proxyPort", "3128");

        Thread redditThread = new Thread(redditService);
        Thread messagesDistributionThread = new Thread(messagesDistribution);
        redditThread.start();
        messagesDistributionThread.start();
    }
}
