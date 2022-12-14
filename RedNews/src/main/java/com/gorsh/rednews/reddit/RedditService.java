package com.gorsh.rednews.reddit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gorsh.rednews.entities.ChannelReddit;
import com.gorsh.rednews.handlers.TelegramMessageHandler;
import com.gorsh.rednews.service.ChannelRedditService;
import com.gorsh.rednews.service.PersonService;
import com.gorsh.rednews.service.TelegramMessageService;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

@Component
@Log4j
public class RedditService implements Runnable{

    @Value("${configs.reddit.clientId}")
    String clientId;

    @Value("${configs.reddit.secret}")
    String secret;

    @Value("${configs.reddit.accsessTokenUrl}")
    String accsessTokenUrl;

    @Value("${configs.reddit.requestUrl}")
    String requestUrl;

    @Value("${configs.reddit.limit}")
    String limit;

    @Value("${configs.reddit.apiRequestTimeout}")
    int apiRequestTimeout;

    @Autowired
    PersonService personService;

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
                List<String> bodyListResponse = readArticles(getAuthToken(),channelRedditList);
                telegramMessageHandler.telegramMessageMarshaling(bodyListResponse, channelRedditList);
                log.debug("UPDATE!!!");
            }
            try {
                //задержка запросов апи реддита
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    //получение токена доступа к апи
    public synchronized String getAuthToken(){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, secret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.put("User-Agent", Collections.singletonList("myApp:V0.1"));

        String body = "grant_type=client_credentials";
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        String authUrl = accsessTokenUrl;
        ResponseEntity<String> response = restTemplate.postForEntity(authUrl, request, String.class);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<>();
        try {
            map.putAll(mapper.readValue(response.getBody(), new TypeReference<Map<String,Object>>(){}));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return String.valueOf(map.get("access_token"));
    }

    //получения списка body response по списку сабреддитов учитывая фильтр
    public synchronized List<String> readArticles(String authToken, List<ChannelReddit> subreddits){
        List<String> bodyResponseList = new ArrayList<>();
        ResponseEntity<String> response;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
        headers.put("User-Agent", Collections.singletonList("myApp:V0.1"));
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
        if (subreddits!=null){
            for(ChannelReddit ch : subreddits){
                String url = requestUrl + ch.getSubreddit() + "/" + ch.getChannelFilter() + limit;
                log.debug(ch.getSubreddit());
                response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
                bodyResponseList.add(response.getBody());

                try {
                    Thread.sleep(apiRequestTimeout);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return bodyResponseList;
    }
}
