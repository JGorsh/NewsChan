package com.gorsh.rednews;

import com.gorsh.rednews.entities.Person;
import com.gorsh.rednews.service.PersonService;
import com.gorsh.rednews.telegram.WriteReadBot;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
public class LongPollingController {

    @Autowired
    PersonService personService;

    private final WriteReadBot writeReadBot;

    public LongPollingController(WriteReadBot writeReadBot) {
        this.writeReadBot = writeReadBot;
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public void onUpdateReceived(@RequestBody Update update) {
       writeReadBot.onUpdateReceived(update);
    }

}
