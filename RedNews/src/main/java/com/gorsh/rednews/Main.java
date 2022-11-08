package com.gorsh.rednews;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.token.OAuthToken;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {

    private static final String PARAM_CLIENT_ID = "client_id";
    private static final String PARAM_RESPONSE_TYPE = "response_type";
    private static final String PARAM_STATE = "state";
    private static final String PARAM_REDIRECT_URI = "redirect_uri";
    private static final String PARAM_DURATION = "duration";
    private static final String PARAM_SCOPE = "scope";
    private static final String PARAM_GRANT_TYPE = "grant_type";
    private static final String PARAM_CODE = "code";
    private static final String PARAM_DEVICE_ID = "device_id";

    private static final String HEADER_USER_AGENT = "User-Agent";
    private static final String HEADER_AUTHORIZATION = "Authorization";



    public static void main(String[] args) throws OAuthSystemException, OAuthProblemException, IOException {

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
        System.out.println(getAuthToken());

    }

    public static String getAuthToken(){

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.orb.ru", 3128));
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setProxy(proxy);

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("YIs2-_3udGw-RmaGqkj94w", "Gm2TKpV2_YZLWcBU-oh6l44vRiHj-w");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.put("User-Agent",
                Collections.singletonList("myApp:V0.1"));
        String body = "grant_type=client_credentials";
        HttpEntity<String> request
                = new HttpEntity<>(body, headers);
        String authUrl = "https://www.reddit.com/api/v1/access_token";
        ResponseEntity<String> response = restTemplate.postForEntity(
                authUrl, request, String.class);
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

    public String readArticles(String subReddit) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        String authToken = getAuthToken();
        headers.setBearerAuth(authToken);
        headers.put("User-Agent",
                Collections.singletonList(""));
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
        String url = "https://oauth.reddit.com/r/"+subReddit+"/hot";
        ResponseEntity<String> response
                = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return response.getBody();
    }
}
