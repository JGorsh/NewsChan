package com.gorsh.rednews.telegram;

import com.gorsh.rednews.entities.ChannelReddit;
import com.gorsh.rednews.entities.Person;
import com.gorsh.rednews.entities.TelegramMessage;
import com.gorsh.rednews.service.PersonService;

import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@Log4j
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

        //while (true){
            if(personList!=null){
                for (Person person : personList) {
                    if (person.isDistribution()) {
                        List<ChannelReddit> channelRedditList = person.getSubreddits();
                        for (ChannelReddit channelReddit : channelRedditList) {
                            List<TelegramMessage> telegramMessageList = channelReddit.getMessages();
                            String subreddit = channelReddit.getSubreddit();
                            for (TelegramMessage telegramMessage : telegramMessageList) {
                                if(!telegramMessage.isSent()){
                                    body = new LinkedMultiValueMap<>();
                                    handlerMsgRdt(person, telegramMessage, body, subreddit);
                                    HttpEntity<Object> request = new HttpEntity<>(body, headers);
                                    try {
                                        restTemplate.postForEntity(apiUrl, request, String.class);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        continue;
                                    }
                                    telegramMessage.setSent(true);
                                    personService.save(person);
                                    log.debug(person.getChatId() +  "-->" + telegramMessage.getTitle());
                                }
                            }
                        }
                    }
                }
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
            }
        //}

    }

    public void handlerMsgRdt(Person person, TelegramMessage telegramMessage, MultiValueMap<String, String> body, String subreddit) {
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
        String bodyString = "**" + subreddit + "**"
                + "\n\n"
                + telegramMessage.getTitle()
                + "\n\n" + "Link Post: "
                + telegramMessage.getUrlMedia()
                + "\n\n"
                + "reddit.com" + telegramMessage.getUrlPost() + "\uD83D\uDCAC";

        body.add("chat_id", person.getChatId());
        body.add("text", bodyString);
    }

    @Override
    @Scheduled(fixedDelay = 3600000)
    public void run() {
        onTelegramDistribution("5636275218:AAGij5CRWKFgOJW5BJ4inMxn5VuepfZb--g");
    }
}
