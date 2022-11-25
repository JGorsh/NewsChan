package com.gorsh.rednews.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "telegramMessage")
public class TelegramMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column
    private String title;

    @Column
    private String urlPost;

    @Column
    private String urlMedia;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TelegramMessage that = (TelegramMessage) o;
        return Objects.equals(title, that.title) && Objects.equals(urlPost, that.urlPost) && Objects.equals(urlMedia, that.urlMedia);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, urlPost, urlMedia);
    }
}
