package com.gorsh.rednews.service;

import com.gorsh.rednews.entities.Person;
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
public class TelegramDistribution {

    @Autowired
    PersonService personService;

    private void onTelegramDistribution(String botToken){
        String apiUrl = "https://api.telegram.org/" + botToken + "/sendMessage";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
        List<Person> personList = personService.getPersonList();
        for (Person person : personList){
            body.add("chat_id", person.getChatId());

        }

        body.add("text", "Hello");
        HttpEntity<Object> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("https://api.telegram.org/bot5636275218:AAGij5CRWKFgOJW5BJ4inMxn5VuepfZb--g/sendMessage", request,  String.class);
        System.out.println(response.getBody());

    }
}
