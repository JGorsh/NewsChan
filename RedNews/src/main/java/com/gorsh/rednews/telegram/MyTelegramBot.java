package com.gorsh.rednews.telegram;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gorsh.rednews.entities.ChannelReddit;
import com.gorsh.rednews.entities.Person;
import com.gorsh.rednews.entities.TelegramMessage;
import com.gorsh.rednews.service.ChannelRedditService;
import com.gorsh.rednews.service.PersonService;
import com.gorsh.rednews.reddit.RedditService;
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
public class MyTelegramBot extends TelegramLongPollingBot {

    private String subreddit;
    private String filter;
    boolean startWait = false;

    private TelegramStatus status;

    private boolean lentaLoop = false;

    private Person person;

    private ChannelReddit channelReddit;

    private TelegramMessage telegramMessage;

    private RedditService redditService;

    @Autowired
    PersonService personService ;

    @Autowired
    ChannelRedditService channelRedditService;

    private List<ChannelReddit> subreddits = new ArrayList<>();


    public MyTelegramBot(DefaultBotOptions options) {
        super(options);
    }

    @Override
    public String getBotUsername() {
        return "RedditNewsChanelBot";
    }

    @Override
    public String getBotToken() {
        return "5636275218:AAGij5CRWKFgOJW5BJ4inMxn5VuepfZb--g";
    }

    @Override
    public void onUpdateReceived(Update update) {

        SendMessage message = new SendMessage();
        String chatId = "";
        Long userId;
        String userName;
        if(update.hasMessage()){
            person  = new Person();
            chatId = update.getMessage().getChatId().toString();
            userId = update.getMessage().getFrom().getId();
            userName = update.getMessage().getFrom().getUserName();
            person.setChatId(chatId);
            person.setUserId(userId);
            person.setUserName(userName);
            //personService.save(person);
            System.out.println(person.getChatId() + " " + person.getUserName());
            message.setChatId(chatId);
        }
        if (update.hasCallbackQuery()){
            chatId = update.getCallbackQuery().getMessage().getChatId().toString();
            message.setChatId(chatId);
        }

        redditService = new RedditService();

        if (update.hasMessage() && update.getMessage().hasText() && startWait == false) {
            String command = update.getMessage().getText();
            if (command.equals("/start")) {
                message.setText("Введите отслеживаемый subreddit ");
                startWait = true; // Переменная = true в этом цикле она не будет false, а значит будет считка сообщения до тех пор, пока она снова не станет false
            }

            if (command.equals("/stop")){
                lentaLoop = false;
            }

            if (command.equals("/run")) {
                lentaLoop = true;
                    sndMsgRdt(chatId, subreddits);
                startWait = false; // Переменная = true в этом цикле она не будет false, а значит будет считка сообщения до тех пор, пока она снова не станет false
            }
        }
        else if (update.hasMessage() && update.getMessage().hasText() && startWait == true) {
            subreddit =update.getMessage().getText();
            message.setText("Выберите фильтр для "+update.getMessage().getText());
            message.setReplyMarkup(getInlineMessageButton());
            startWait = false; // "Считка" закончена
        }

        if (update.hasCallbackQuery() && startWait == false){
            if (update.getCallbackQuery().getData().equals("new")){
                filter = "new";
                message.setText("Ваш отслеживаемый subreddit " + subreddit + " с фильтром new" + "\n" +
                        "Для запуска ленты введите команду /run" + "\n" +
                        "Для остановки ленты введите команду /stop");
                channelReddit.setSubreddit(subreddit);
                channelReddit.setChannelFilter("new");
                subreddits.add(channelReddit);
                person.setSubreddits(subreddits);
                personService.save(person);
                startWait = false;
            }

            if (update.getCallbackQuery().getData().equals("hot")){
                filter = "hot";
                message.setText("Ваш отслеживаемый subreddit " + subreddit + " с фильтром hot" + "\n" +
                        "Для запуска ленты введите команду /run" + "\n" +
                        "Для остановки ленты введите команду /stop");
                channelReddit.setSubreddit(subreddit);
                channelReddit.setChannelFilter("hot");
                startWait = false;
            }

            if (update.getCallbackQuery().getData().equals("top")){
                filter = "top";
                message.setText("Ваш отслеживаемый subreddit " + subreddit + " с фильтром top" + "\n" +
                        "Для запуска ленты введите команду /run" + "\n" +
                        "Для остановки ленты введите команду /stop");
                channelReddit.setSubreddit(subreddit);
                channelReddit.setChannelFilter("top");
                startWait = false;
            }
        }
        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private InlineKeyboardMarkup getInlineMessageButton(){
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

    
    public void sndMsgRdt(String chatId, List<ChannelReddit> subreddits){

        System.setProperty("https.proxyHost", "proxy.orb.ru");
        System.setProperty("https.proxyPort", "3128");

//        RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders headers = new HttpHeaders();
//        HttpEntity<String> request = new HttpEntity<>(headers);
//        ResponseEntity<String> response = restTemplate.postForEntity("https://api.telegram.org/bot5636275218:AAGij5CRWKFgOJW5BJ4inMxn5VuepfZb--g/getUpdates", request,  String.class);
//        System.out.println(response.getBody());

//        RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders headers = new HttpHeaders();
//        MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
//        body.add("chat_id", "457487030");
//        body.add("text", "Hello");
//        HttpEntity<Object> request = new HttpEntity<>(body, headers);
//        ResponseEntity<String> response = restTemplate.postForEntity("https://api.telegram.org/bot5636275218:AAGij5CRWKFgOJW5BJ4inMxn5VuepfZb--g/sendMessage", request,  String.class);
//        System.out.println(response.getBody());
//        Long chatId = 457487030L;
//        Long chatIdNast = 393135248L;
        ObjectMapper mapper= new ObjectMapper();
        try {
            List<String> resultResponseList = redditService.readArticles(redditService.getAuthToken(), subreddits);
            for(String resultResponse : resultResponseList) {
                JsonNode node = mapper.readTree(resultResponse).get("data").get("children");
                SendMessage message = new SendMessage();
                message.setChatId(chatId.toString());
                if (node.isArray()) {
                    for (final JsonNode objNode : node) {
                        boolean isVideo = Boolean.valueOf((objNode.get("data").get("is_video").asText()));

                        if (isVideo) {
                            message.setText(objNode.get("data").get("title").asText()
                                    + "\n\n"
                                    + objNode.get("data").get("media").get("reddit_video").get("fallback_url").asText()
                                    + "\n\n" + "Link Post: "
                                    + "reddit.com" + objNode.get("data").get("permalink").asText());
                            execute(message);

                            System.out.println(objNode.get("data").get("title").asText());
                            System.out.println(objNode.get("data").get("media").get("reddit_video").get("fallback_url").asText());
                            System.out.println("reddit.com" + objNode.get("data").get("permalink").asText());
                        } else {
                            message.setText(objNode.get("data").get("title").asText()
                                    + "\n\n"
                                    + objNode.get("data").get("url").asText()
                                    + "\n\n" + "Link Post: "
                                    + "reddit.com" + objNode.get("data").get("permalink").asText());
                            execute(message);

                            System.out.println(objNode.get("data").get("title").asText());
                            System.out.println(objNode.get("data").get("url").asText());
                            System.out.println("reddit.com" + objNode.get("data").get("permalink").asText());
                        }
                    }
                }
                //        Thread.sleep(600000);
            }
            } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


    }
}

