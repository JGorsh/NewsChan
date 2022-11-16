package com.gorsh.rednews;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.gorsh.rednews.telegram.WriteReadBot;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.conn.DefaultManagedHttpClientConnection;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;

public class Main {

//    private static final String PARAM_CLIENT_ID = "client_id";
//    private static final String PARAM_RESPONSE_TYPE = "response_type";
//    private static final String PARAM_STATE = "state";
//    private static final String PARAM_REDIRECT_URI = "redirect_uri";
//    private static final String PARAM_DURATION = "duration";
//    private static final String PARAM_SCOPE = "scope";
//    private static final String PARAM_GRANT_TYPE = "grant_type";
//    private static final String PARAM_CODE = "code";
//    private static final String PARAM_DEVICE_ID = "device_id";
//
//    private static final String HEADER_USER_AGENT = "User-Agent";
//    private static final String HEADER_AUTHORIZATION = "Authorization";



    public static void main(String[] args) throws  JsonProcessingException {

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
        Long chatId = 457487030L;
        Long chatIdNast = 393135248L;
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            DefaultBotOptions botOptions = new DefaultBotOptions();
            botOptions.setProxyHost("proxy.orb.ru");
            botOptions.setProxyPort(3128);
            botOptions.setProxyType(DefaultBotOptions.ProxyType.HTTP);
            WriteReadBot bot = new WriteReadBot(botOptions);
            botsApi.registerBot(bot);

            while(true){
                JsonNode node = mapper.readTree(readArticles()).get("data").get("children");
                SendMessage message = new SendMessage();
                message.setChatId(chatId.toString());
                message.setText("UPDATING" + "\n");
                bot.execute(message);

                if (node.isArray()) {
                    for (final JsonNode objNode : node) {
                        boolean isVideo = Boolean.valueOf((objNode.get("data").get("is_video").asText()));

                        if(isVideo){
                            message.setText(objNode.get("data").get("title").asText()
                                    + "\n\n"
                                    + objNode.get("data").get("media").get("reddit_video").get("fallback_url").asText()
                                    + "\n\n" + "Link Post: "
                                    + "reddit.com" + objNode.get("data").get("permalink").asText());
                            bot.execute(message);

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
                            bot.execute(message);

                            System.out.println(objNode.get("data").get("title").asText());
                            System.out.println(objNode.get("data").get("url").asText());
                            System.out.println("reddit.com" + objNode.get("data").get("permalink").asText());
                        }
                    }
                }
                Thread.sleep(600000);
            }

        } catch (TelegramApiException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }


//        String clientId = "YIs2-_3udGw-RmaGqkj94w";
//        String secretKey = "Gm2TKpV2_YZLWcBU-oh6l44vRiHj-w";
//        String tokenUrl = "https://www.reddit.com/api/v1/access_token";
//        String oAuthUrl = "https://www.reddit.com/api/v1/authorize?";
//        String respType = "code";
//        String redirectUrl = "http://localhost:8080";
//        String duration = "permanent";
//        String scope = "edit";
//
//        StringBuilder strReq = new StringBuilder();
//        strReq.append(oAuthUrl + PARAM_CLIENT_ID + "=" + clientId + "&"
//                + PARAM_RESPONSE_TYPE + "=" + respType + "&"
//                + PARAM_STATE + "=" + UUID.randomUUID() + "&"
//                + PARAM_REDIRECT_URI + "=" + redirectUrl + "&"
//                + PARAM_DURATION + "=" + duration + "&"
//                + PARAM_SCOPE + "=" + scope);
//
//        HttpGet requestOauth = new HttpGet(strReq.toString());
//        System.out.println(strReq);



    public static String getAuthToken(){
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

    public static String readArticles() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String authToken = getAuthToken();
        headers.setBearerAuth(authToken);
        headers.put("User-Agent", Collections.singletonList("myApp:V0.1"));
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
        String url = "https://oauth.reddit.com/r/funny/new?limit=1";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return response.getBody();
    }
}
