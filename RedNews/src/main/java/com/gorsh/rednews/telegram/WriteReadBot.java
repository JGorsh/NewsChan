package com.gorsh.rednews.telegram;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gorsh.rednews.entities.Person;
import com.gorsh.rednews.repository.PersonRepository;
import com.gorsh.rednews.service.PersonService;
import com.gorsh.rednews.service.RedditService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
//@Getter
//@Setter
@Component
public class WriteReadBot extends TelegramLongPollingBot {

    private String subreddit;
    private String filter;
    boolean startWait = false;
    
    boolean lentaLoop = false;


    Person person;


    RedditService redditService;

    @Autowired
    PersonService personService ;

    public WriteReadBot(DefaultBotOptions options) {
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
        if(update.hasMessage()){
            person  = new Person();
            chatId = update.getMessage().getChatId().toString();
            person.setChatId(chatId);
            person.setName(update.getMessage().getChat().getUserName());
            personService.save(person);
            System.out.println(person.getChatId() + " " + person.getName());
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
                while (lentaLoop){
                    sndMsgRdt(chatId, subreddit, filter);
                    try {
                        Thread.sleep(500000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
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
                startWait = false;
            }

            if (update.getCallbackQuery().getData().equals("hot")){
                filter = "hot";
                message.setText("Ваш отслеживаемый subreddit " + subreddit + " с фильтром hot" + "\n" +
                        "Для запуска ленты введите команду /run" + "\n" +
                        "Для остановки ленты введите команду /stop");
                startWait = false;
            }

            if (update.getCallbackQuery().getData().equals("top")){
                filter = "top";
                message.setText("Ваш отслеживаемый subreddit " + subreddit + " с фильтром top" + "\n" +
                        "Для запуска ленты введите команду /run" + "\n" +
                        "Для остановки ленты введите команду /stop");
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

    
    public void sndMsgRdt(String chatId, String subreddit, String filter){

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
        ObjectMapper mapper= new ObjectMapper();
//        Long chatId = 457487030L;
//        Long chatIdNast = 393135248L;
        try {
                JsonNode node = mapper.readTree(redditService.readArticles(redditService.getAuthToken(), subreddit, filter)).get("data").get("children");
                SendMessage message = new SendMessage();
                message.setChatId(chatId.toString());
                if (node.isArray()) {
                    for (final JsonNode objNode : node) {
                        boolean isVideo = Boolean.valueOf((objNode.get("data").get("is_video").asText()));

                        if(isVideo){
                            message.setText(objNode.get("data").get("title").asText()
                                    + "\n\n"
                                    + objNode.get("data").get("media").get("reddit_video").get("fallback_url").asText()
                                    + "\n\n" + "Link Post: "
                                    + "reddit.com" + objNode.get("data").get("permalink").asText());
                            execute(message);

                            System.out.println(objNode.get("data").get("title").asText());
                            System.out.println(objNode.get("data").get("media").get("reddit_video").get("fallback_url").asText());
                            System.out.println("reddit.com" + objNode.get("data").get("permalink").asText());
                        }
                        else {
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

        } catch (TelegramApiException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
//    public void sendMsg (SendMessage message, String text) {
//        SendMessage sendMessage = new SendMessage();
//        sendMessage.enableMarkdown(true);
//
//        // Создаем клавиатуру
//        ReplyKeyboardMarkup replyKeyboardMarkup = new
//                ReplyKeyboardMarkup();
//        sendMessage.setReplyMarkup(replyKeyboardMarkup);
//        replyKeyboardMarkup.setSelective(true);
//        replyKeyboardMarkup.setResizeKeyboard(true);
//        replyKeyboardMarkup.setOneTimeKeyboard(false);
//
//        // Создаем список строк клавиатуры
//        List<KeyboardRow> keyboard = new ArrayList<>();
//
//        // Первая строчка клавиатуры
//        KeyboardRow keyboardFirstRow = new KeyboardRow();
//        // Добавляем кнопки в первую строчку клавиатуры
//        keyboardFirstRow.add("new");
//        keyboardFirstRow.add("best");
//        keyboardFirstRow.add("top");
//
////        // Вторая строчка клавиатуры
////        KeyboardRow keyboardSecondRow = new KeyboardRow();
////        // Добавляем кнопки во вторую строчку клавиатуры
////        keyboardSecondRow.add("Команда 3");
////        keyboardSecondRow.add("Команда 4");
//
//        // Добавляем все строчки клавиатуры в список
//        keyboard.add(keyboardFirstRow);
//       // keyboard.add(keyboardSecondRow);
//        // и устанавливаем этот список нашей клавиатуре
//        replyKeyboardMarkup.setKeyboard(keyboard);
//
//        sendMessage.setChatId(message.getChatId().toString());
//        sendMessage.setReplyToMessageId(message.getReplyToMessageId());
//        sendMessage.setText(text);
//        try {
//            execute(sendMessage);
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//        }
//    }
}

