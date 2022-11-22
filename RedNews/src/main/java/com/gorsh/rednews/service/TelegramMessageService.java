package com.gorsh.rednews.service;

import com.gorsh.rednews.entities.TelegramMessage;
import com.gorsh.rednews.repository.TelegramMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TelegramMessageService {

    @Autowired
    TelegramMessageRepository telegramMessageRepository;


    public TelegramMessage save(TelegramMessage telegramMessage) {
        return telegramMessageRepository.save(telegramMessage);
    }
}
