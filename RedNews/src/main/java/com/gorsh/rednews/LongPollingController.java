package com.gorsh.rednews;

import com.gorsh.rednews.service.ChannelRedditService;
import com.gorsh.rednews.service.PersonService;
import com.gorsh.rednews.service.TelegramMessageService;
import com.gorsh.rednews.telegram.MyBotTelegram;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
public class LongPollingController {

    @Autowired
    PersonService personService;

    @Autowired
    ChannelRedditService channelRedditService;

    @Autowired
    TelegramMessageService telegramMessageService;

    private final MyBotTelegram myBotTelegram;

    public LongPollingController(MyBotTelegram myBotTelegram) {
        this.myBotTelegram = myBotTelegram;
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public void onUpdateReceived(@RequestBody Update update) {
       myBotTelegram.onUpdateReceived(update);
    }

}
