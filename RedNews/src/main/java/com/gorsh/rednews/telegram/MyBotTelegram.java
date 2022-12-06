package com.gorsh.rednews.telegram;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gorsh.rednews.entities.ChannelReddit;
import com.gorsh.rednews.entities.Person;
import com.gorsh.rednews.entities.TelegramMessage;
import com.gorsh.rednews.handlers.TelegramMessageHandler;
import com.gorsh.rednews.service.ChannelRedditService;
import com.gorsh.rednews.service.PersonService;
import com.gorsh.rednews.reddit.RedditService;
import com.gorsh.rednews.service.TelegramMessageService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Log4j
@Component
public class MyBotTelegram extends TelegramLongPollingBot {

    private String subreddit;

    private String filter;

    private Person person;

    private ChannelReddit channelReddit;

    private TelegramMessage telegramMessage;

    private TelegramStatus telegramStatus;

    @Autowired
    private RedditService redditService;
    @Autowired
    PersonService personService;

    @Autowired
    ChannelRedditService channelRedditService;

    @Autowired
    TelegramMessageHandler telegramMessageHandler;

    @Autowired
    TelegramMessageService telegramMessageService;

    @Autowired
    UserStatusCache userStatusCache;

    private List<ChannelReddit> subreddits = new ArrayList<>();

    public MyBotTelegram(DefaultBotOptions options) {
        super(options);
    }

    @Override
    public String getBotUsername() {
        return "RedditNewsChannelBot";
    }

    @Override
    public String getBotToken() {
        return "5636275218:AAGij5CRWKFgOJW5BJ4inMxn5VuepfZb--g";
    }

    @Override
    public void onUpdateReceived(Update update) {
        SendMessage message = new SendMessage();
        String chatId = "";
        String userName;
        String text = "";

        if (update.hasMessage()) {
            person = new Person();
            chatId = update.getMessage().getChatId().toString();
            userName = update.getMessage().getFrom().getUserName();
            person.setChatId(chatId);
            person.setUserName(userName);
            log.debug(person.getChatId() + " " + person.getUserName());
            message.setChatId(chatId);
            text = update.getMessage().getText();

//            if(text.equals("/reset")){
//                userStatusCache.setUsersCurrentTelegramStatus(chatId, TelegramStatus.RUN);
//                message.setText("Прогресс чата сброшен, бот остановлен, для начала работы нажмите /run ");
//            }
        }
        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId().toString();
            message.setChatId(chatId);
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            TelegramStatus telegramStatus = userStatusCache.getUsersCurrentTelegramStatus(chatId);

            switch (telegramStatus) {
                case DEFAULT:
                    if (text.equals("/start")) {
                        message.setText("Введите отслеживаемый subreddit ");
                        userStatusCache.setUsersCurrentTelegramStatus(chatId, TelegramStatus.START);
                    } else {
                        message.setText("Неверная команда " + text);
                    }
                    log.debug(chatId + " default");
                    break;

                case START:
                    subreddit = update.getMessage().getText();
                    if (isSubreddit(redditService, subreddit) && isExistSubreddit(chatId, subreddit)) {
                        message.setText("Выберите фильтр для " + update.getMessage().getText());
                        message.setReplyMarkup(getInlineMessageButtonFilter());
                        userStatusCache.setUsersCurrentTelegramStatus(chatId, TelegramStatus.FILTER);
                        log.debug(chatId + " start");
                    } else {
                        message.setText("Такого subreddit не существует (либо вы уже подписаны)! \nВведите другой subreddit!");
                    }

                    break;

                case RUN:
                    if (text.equals("/run")) {
                        person = personService.getByChatId(chatId);
                        person.setDistribution(true);
                        personService.save(person);
                        message.setText("Бот запущен! \nДля остановки ленты введите команду /stop " +
                                "\nДля добавления новых подписок введите команду /update" +
                                "\nДля получения списка подписок введите команду /list" +
                                "\nДля удаления подписки введите команду /delete" +
                                "\nДля сброса статуса чата введите команду /reset");
                        userStatusCache.setUsersCurrentTelegramStatus(chatId, TelegramStatus.RUNNING);
                        log.debug(chatId + " run");
                    } else {
                        message.setText("Неверная команда " + text);
                    }
                    break;

                case RUNNING:
                    if (text.equals("/stop")) {
                        person = personService.getByChatId(chatId);
                        person.setDistribution(false);
                        personService.save(person);
                        userStatusCache.setUsersCurrentTelegramStatus(chatId, TelegramStatus.RUN);
                        message.setText("Бот остановлен! \n Для запуска введите команду /run");
                        log.debug(chatId + " stop");
                    }
                    else if (text.equals("/update")) {
                        message.setText("Введите отслеживаемый subreddit ");
                        userStatusCache.setUsersCurrentTelegramStatus(chatId, TelegramStatus.START);
                        log.debug(chatId + " update");
                    }
                    else if (text.equals("/list")){
                        message.setText(getListSubreddit(chatId));
                        log.debug(chatId + " list");
                    }
                    else if (text.equals("/delete")){
                        message.setText("Введите subreddit для удаления из вашей ленты!");
                        userStatusCache.setUsersCurrentTelegramStatus(chatId, TelegramStatus.DELETE);
                        log.debug(chatId + " delete");
                    }
                    else {
                        message.setText("Неверная команда " + text);
                    }
                    break;

                case DELETE:
                    try{
                        ChannelReddit channelReddit = channelRedditService.getChannelRedditBySubreddit(text);
                        channelRedditService.deleteChannelRedditById(channelReddit.getId());
                        message.setText("Subreddit " + text + " удален из вашей ленты!");
                        userStatusCache.setUsersCurrentTelegramStatus(chatId, TelegramStatus.RUNNING);
                    } catch (Exception e){
                        message.setText("Вы не были подписаны на " + text + " \nВведите другой subreddit!");
                    }
            }
        }

        if (update.hasCallbackQuery() && userStatusCache.getUsersCurrentTelegramStatus(chatId) == TelegramStatus.FILTER) {
            Person personData = personService.getByChatId(person.getChatId());
            filter = update.getCallbackQuery().getData();

            setTextMessageFilterAndSubreddit(subreddit, filter, message);
            userStatusCache.setUsersCurrentTelegramStatus(chatId, TelegramStatus.RUN);
            channelReddit = new ChannelReddit();
            channelReddit.setSubreddit(subreddit);
            channelReddit.setChannelFilter(filter);

            // добавление нового пользователя (разобраться)
            if (personData == null) {
                person.getSubreddits().add(channelReddit);
                personService.save(person);
                log.debug(chatId + channelReddit.getSubreddit());
            } else {
                personData.getSubreddits().add(channelReddit);
                personService.save(personData);
                log.debug(chatId + channelReddit.getSubreddit());
            }
        }
        try {
            if(message.getText()==null){
                message.setText("Некорректный ввод");
            }
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private InlineKeyboardMarkup getInlineMessageButtonFilter() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton buttonNew = new InlineKeyboardButton();
        InlineKeyboardButton buttonTop = new InlineKeyboardButton();
        InlineKeyboardButton buttonHot = new InlineKeyboardButton();
        buttonHot.setText("hot");
        buttonNew.setText("new");
        buttonTop.setText("top");

        //чтобы понять отклик от кнопки обязательно заполнить 1-16
        buttonNew.setCallbackData("new");
        buttonTop.setCallbackData("top");
        buttonHot.setCallbackData("hot");

        List<InlineKeyboardButton> keyboardButtons = new ArrayList<>();
        keyboardButtons.add(buttonNew);
        keyboardButtons.add(buttonTop);
        keyboardButtons.add(buttonHot);

        //для маркап нужен List<List<>>
        List<List<InlineKeyboardButton>> keyboardButtons1 = new ArrayList<>();
        keyboardButtons1.add(keyboardButtons);

        inlineKeyboardMarkup.setKeyboard(keyboardButtons1);

        return inlineKeyboardMarkup;
    }

//    private ReplyKeyboardMarkup getInlineMessageButtonGreeting() {
//        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
//        KeyboardButton button = new KeyboardButton();
//        button.setText("\uD83D\uDC49 Start \uD83D\uDCE3");
//        List<KeyboardRow> keyboardRowList = new ArrayList<>();
//        KeyboardRow keyboardRow = new KeyboardRow();
//        keyboardRow.add(button);
//        keyboardRowList.add(keyboardRow);
//        markup.setKeyboard(keyboardRowList);
//        markup.setOneTimeKeyboard(true);
//        return markup;
//    }

    private void setTextMessageFilterAndSubreddit(String subreddit, String filter, SendMessage message) {
        message.setText("Ваш отслеживаемый subreddit " + subreddit + " с фильтром " + filter + "\n" +
                "Для включения подписки в ленту введите команду /run");
    }

    private boolean isSubreddit(RedditService redditService, String subreddit) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(redditService.getAuthToken());
        headers.put("User-Agent", Collections.singletonList("myApp:V0.1"));
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
        String url = "https://oauth.reddit.com/r/" + subreddit + "/" + "?limit=1";

        try {
            restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private String getListSubreddit(String chatId){
        Person person = personService.getByChatId(chatId);
        List<ChannelReddit> channelRedditList = person.getSubreddits();
        StringBuilder str = new StringBuilder();
        for(ChannelReddit ch : channelRedditList){
            String subreddit = ch.getSubreddit();
            String filter = ch.getChannelFilter();
            str.append(subreddit + " (" + filter + ")\n");
        }
        return str.toString();
    }

    private boolean isExistSubreddit (String chatId, String subreddit){
        Person person = personService.getByChatId(chatId);
        try {
            List<ChannelReddit> channelRedditList = person.getSubreddits();
            for(ChannelReddit ch : channelRedditList){
                if(ch.getSubreddit().equals(subreddit)){
                    return false;
                }
            }
        } catch (Exception e){
            return true;
        }
        return true;
    }
}

