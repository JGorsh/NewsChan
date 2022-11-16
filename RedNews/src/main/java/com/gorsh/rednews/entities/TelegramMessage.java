package com.gorsh.rednews.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "person")
public class TelegramMessage {

    @Id
    @JsonIgnore
    private String id;

    @Column
    private String subreddit;

    @Column
    private String urlPost;

    @Column
    private String urlMedia;


}
