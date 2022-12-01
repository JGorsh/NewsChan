package com.gorsh.rednews.repository;

import com.gorsh.rednews.entities.ChannelReddit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelRedditRepository extends JpaRepository<ChannelReddit, Long> {

    ChannelReddit getChannelRedditBySubreddit(String subreddit);

}
