package com.gorsh.rednews.repository;

import com.gorsh.rednews.entities.TelegramMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TelegramMessageRepository extends JpaRepository<TelegramMessage, Long> {
}
