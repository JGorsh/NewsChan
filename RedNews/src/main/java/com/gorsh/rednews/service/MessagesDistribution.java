package com.gorsh.rednews.service;

import com.gorsh.rednews.entities.ChannelReddit;
import com.gorsh.rednews.entities.Person;
import com.gorsh.rednews.entities.TelegramMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class MessagesDistribution implements Runnable{

    @Autowired
    PersonService personService;

    //обработать похожие сообщения
    private void onTelegramDistribution(String botToken){

        String apiUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, String> body;
        List<Person> personList = personService.getPersonList();

        while (true){
            if(personList!=null){
                for (Person person : personList) {
                    if (person.isDistribution()) {
                        List<ChannelReddit> channelRedditList = person.getSubreddits();

                        for (ChannelReddit channelReddit : channelRedditList) {
                            List<TelegramMessage> telegramMessageList = channelReddit.getMessages();

                            for (TelegramMessage telegramMessage : telegramMessageList) {
                                body = new LinkedMultiValueMap<String, String>();
                                handlerMsgRdt(person, telegramMessage, body);
                                HttpEntity<Object> request = new HttpEntity<>(body, headers);
                                System.out.println(telegramMessage);
                                System.out.println(request);
                                try {
                                    ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
                                    //System.out.println(response.getBody());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    continue;
                                }
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    public void handlerMsgRdt(Person person, TelegramMessage telegramMessage, MultiValueMap<String, String> body) {
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
//        Long chatIdNast = 393135248L
        String bodyString = telegramMessage.getTitle() + "\uD83D\uDC48"
                + "\n\n"
                + telegramMessage.getUrlMedia()
                + "\n\n" + "Link Post: "
                + "reddit.com" + telegramMessage.getUrlPost() + "\uD83D\uDCAC";

        body.add("chat_id", person.getChatId());
        body.add("text", bodyString);
    }

    @Override
    public void run() {
        onTelegramDistribution("5636275218:AAGij5CRWKFgOJW5BJ4inMxn5VuepfZb--g");
    }
}
