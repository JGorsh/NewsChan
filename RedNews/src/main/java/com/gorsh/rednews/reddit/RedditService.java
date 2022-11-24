package com.gorsh.rednews.reddit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gorsh.rednews.entities.ChannelReddit;
import com.gorsh.rednews.entities.TelegramMessage;
import com.gorsh.rednews.handlers.TelegramMessageHandler;
import com.gorsh.rednews.service.ChannelRedditService;
import com.gorsh.rednews.service.TelegramMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

@Component
public class RedditService implements Runnable{

    @Autowired
    ChannelRedditService channelRedditService;

    @Autowired
    TelegramMessageService telegramMessageService;

    @Autowired
    TelegramMessageHandler telegramMessageHandler;

    @Override
    public void run() {
        while(true){
            List<ChannelReddit> channelRedditList = channelRedditService.getAll();
            if(channelRedditList!=null){
                List<String> bodyListResponse = readArticles(getAuthToken(),channelRedditService.getAll());
                List<TelegramMessage> telegramMessageList = telegramMessageHandler.telegramMessageMarshaling(bodyListResponse);
                telegramMessageService.saveAll(telegramMessageList);
                System.out.println("Update");
            }
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    //получение токена доступа к апи
    public String getAuthToken(){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("YIs2-_3udGw-RmaGqkj94w", "Gm2TKpV2_YZLWcBU-oh6l44vRiHj-w");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.put("User-Agent", Collections.singletonList("myApp:V0.1"));

        String body = "grant_type=client_credentials";
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        String authUrl = "https://www.reddit.com/api/v1/access_token";
        ResponseEntity<String> response = restTemplate.postForEntity(authUrl, request, String.class);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<>();
        try {
            map.putAll(mapper
                    .readValue(response.getBody(), new TypeReference<Map<String,Object>>(){}));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return String.valueOf(map.get("access_token"));
    }

    //получения списка body response по списку сабреддитов учитывая фильтр
    public List<String> readArticles(String authToken, List<ChannelReddit> subreddits){
        List<String> bodyResponseList = new ArrayList<>();
        ResponseEntity<String> response;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
        headers.put("User-Agent", Collections.singletonList("myApp:V0.1"));
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
        for(ChannelReddit ch : subreddits){
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
            String url = "https://oauth.reddit.com/r/" + ch.getSubreddit() + "/" + ch.getChannelFilter() + "?limit=1";
            System.out.println("readReddit");
            response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            bodyResponseList.add(response.getBody());

        }
        return bodyResponseList;
    }


}
