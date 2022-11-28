package com.gorsh.rednews.telegram;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class UserStatusCache {

    private Map<String, TelegramStatus> usersTelegramStatus = new HashMap<>();

    public void setUsersCurrentTelegramStatus(String userId, TelegramStatus telegramStatus) {
        usersTelegramStatus.put(userId, telegramStatus);
    }

    public TelegramStatus getUsersCurrentTelegramStatus(String userId) {
        TelegramStatus telegramStatus = usersTelegramStatus.get(userId);
        if (telegramStatus == null) {
            telegramStatus = TelegramStatus.START;
        }
        return telegramStatus;
    }
}
