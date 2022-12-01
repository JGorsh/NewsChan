package com.gorsh.rednews.telegram;


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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Component
public class MyBotTelegram extends TelegramLongPollingBot{

    private String subreddit;

    private String filter;

    private Person person;

    private ChannelReddit channelReddit;

    private TelegramMessage telegramMessage;

    private RedditService redditService;

    private TelegramStatus telegramStatus;

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
        if (update.hasMessage()) {
            person = new Person();
            chatId = update.getMessage().getChatId().toString();
            userName = update.getMessage().getFrom().getUserName();
            person.setChatId(chatId);
            person.setUserName(userName);
            System.out.println(person.getChatId() + " " + person.getUserName());
            message.setChatId(chatId);
        }
        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId().toString();
            message.setChatId(chatId);
        }

        redditService = new RedditService();

        if (update.hasMessage() && update.getMessage().hasText()) {
            TelegramStatus telegramStatus = userStatusCache.getUsersCurrentTelegramStatus(chatId);
            String text = update.getMessage().getText();
            switch (telegramStatus) {
                case START :
                    subreddit = update.getMessage().getText();
                    message.setText("Выберите фильтр для " + update.getMessage().getText());
                    message.setReplyMarkup(getInlineMessageButtonFilter());
                    userStatusCache.setUsersCurrentTelegramStatus(chatId, TelegramStatus.FILTER);
                    System.out.println("start");
                    break;

                case RUN:
                    person = personService.getByChatId(chatId);
                    person.setDistribution(true);
                    personService.save(person);
                    message.setText("Бот запущен!");
                    userStatusCache.setUsersCurrentTelegramStatus(chatId, TelegramStatus.DEFAULT);
                    System.out.println("run");
                    break;

                case DEFAULT:
                    message.setText("Введите отслеживаемый subreddit ");
                    userStatusCache.setUsersCurrentTelegramStatus(chatId, TelegramStatus.START);
                    System.out.println("default");
                    break;
                //не отреагирует на стоп
                case STOP:
                    message.setText("Бот остановлен! ");
                    //userStatusCache.setUsersCurrentTelegramStatus(chatId, TelegramStatus.STOP);
                    person = personService.getByChatId(chatId);
                    person.setDistribution(false);
                    personService.save(person);
                    System.out.println("stop");
                    break;
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
            } else {
                personData.getSubreddits().add(channelReddit);
                personService.save(personData);
            }
        }
        try {
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
                "Для запуска ленты введите команду /run" + "\n" +
                "Для остановки ленты введите команду /stop");
    }
}

