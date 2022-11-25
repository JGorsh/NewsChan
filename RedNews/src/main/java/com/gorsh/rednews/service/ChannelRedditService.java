package com.gorsh.rednews.service;

import com.gorsh.rednews.entities.ChannelReddit;
import com.gorsh.rednews.repository.ChannelRedditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChannelRedditService {

    @Autowired
    ChannelRedditRepository channelRedditRepository;

    public ChannelReddit save(ChannelReddit channelReddit) {
        return channelRedditRepository.save(channelReddit);
    }

    public List<ChannelReddit> getAll (){
        return channelRedditRepository.findAll();
    }

    public ChannelReddit getChannelRedditBySubreddit(String subreddit) {
        return channelRedditRepository.getChannelRedditBySubreddit(subreddit);
    }

}
