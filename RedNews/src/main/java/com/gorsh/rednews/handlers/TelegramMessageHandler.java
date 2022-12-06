package com.gorsh.rednews.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gorsh.rednews.entities.ChannelReddit;
import com.gorsh.rednews.entities.TelegramMessage;
import com.gorsh.rednews.service.ChannelRedditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//маппинг списка ответов сабреддита и сохранение в бд

@Component
public class TelegramMessageHandler {

    private List<TelegramMessage> telegramMessageList;

    private TelegramMessage telegramMessage;

    private ChannelReddit channelReddit;

    @Autowired
    ChannelRedditService channelRedditService;

    public List<TelegramMessage> telegramMessageMarshaling (List<String> resultResponseList, List<ChannelReddit> channelRedditList){

        ObjectMapper mapper= new ObjectMapper();
        telegramMessageList = new ArrayList<>();

        try {
            for(String resultResponse : resultResponseList) {
                JsonNode node = mapper.readTree(resultResponse).get("data").get("children");
                if (node.isArray()) {
                    for (final JsonNode objNode : node) {
                        boolean isVideo = Boolean.valueOf((objNode.get("data").get("is_video").asText()));
                        String subreddit = objNode.get("data").get("subreddit").asText();
                        telegramMessage = new TelegramMessage();
                        checkAndFillContent(telegramMessage, isVideo, objNode);
                        telegramMessageList.add(telegramMessage);
                        checkSaveMessageToDb(telegramMessage, channelRedditList, subreddit);
                        }
                    }
                }
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return telegramMessageList;
    }

    public void checkSaveMessageToDb (TelegramMessage telegramMessage, List<ChannelReddit> channelRedditList, String subreddit){
        for(ChannelReddit ch: channelRedditList) {
            if (ch.getSubreddit().equals(subreddit)) {
                channelReddit = channelRedditService.getChannelRedditBySubreddit(subreddit);
                if (!channelReddit.getMessages().contains(telegramMessage)) {
                    channelReddit.getMessages().add(telegramMessage);
                    channelRedditService.save(channelReddit);
                }
            }
        }
    }

    public void checkAndFillContent(TelegramMessage telegramMessage, Boolean isVideo, JsonNode objNode) {
        if (isVideo) {
            telegramMessage.setTitle(objNode.get("data").get("title").asText());
            telegramMessage.setUrlMedia(objNode.get("data").get("media").get("reddit_video").get("fallback_url").asText());
            telegramMessage.setUrlPost(objNode.get("data").get("permalink").asText());
            telegramMessage.setDate(new Date());
        } else {
            telegramMessage.setTitle(objNode.get("data").get("title").asText());
            telegramMessage.setUrlMedia(objNode.get("data").get("url").asText());
            telegramMessage.setUrlPost(objNode.get("data").get("permalink").asText());
            telegramMessage.setDate(new Date());
        }
        //String selftext = objNode.get("data").get("selftext").asText();
    }
}
