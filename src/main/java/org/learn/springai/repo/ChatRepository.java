package org.learn.springai.repo;

import org.learn.springai.model.Chat;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {

}
