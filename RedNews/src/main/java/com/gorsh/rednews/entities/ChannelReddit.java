package com.gorsh.rednews.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "channelReddit")
public class ChannelReddit {

    @Id
    @JsonIgnore
    private String id;

    @Column
    private String channelName;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "messageId")
    private List<TelegramMessage> channels = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public List<TelegramMessage> getChannels() {
        return channels;
    }

    public void setChannels(List<TelegramMessage> channels) {
        this.channels = channels;
    }
}
