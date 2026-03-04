package org.learn.springai.repo;

import org.learn.springai.model.Chat;
import org.learn.springai.model.ChatEntry;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.ArrayList;
import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query("SELECT e FROM ChatEntry e WHERE e.chatId = :chatId ORDER BY e.createdAt DESC")
    List<ChatEntry> findHistoryByChatId(@Param("chatId") Long chatId, Pageable pageable);

}
