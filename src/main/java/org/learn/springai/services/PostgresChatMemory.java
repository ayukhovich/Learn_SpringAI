package org.learn.springai.services;

import lombok.Builder;
import org.learn.springai.model.Chat;
import org.learn.springai.model.ChatEntry;
import org.learn.springai.repo.ChatRepository;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;

@Builder
public class PostgresChatMemory implements ChatMemory {

    private ChatRepository chatMemoryRepository;

    private int maxMessages;

    @Override
    public void add(String conversationId, List<Message> messages) {
        Long chatId = Long.valueOf(conversationId);
        Chat chat = chatMemoryRepository.findById(chatId).orElseThrow();
        for (Message message : messages) {
            ChatEntry entry = ChatEntry.toChatEntry(message);
            chat.addChatEntry(entry);
        }
        chatMemoryRepository.save(chat);
    }

    @Override
    public List<Message> get(String conversationId) {
        Long chatId = Long.valueOf(conversationId);
        if (!chatMemoryRepository.existsById(chatId)) {
            return List.of();
        }
        return  chatMemoryRepository.findHistoryByChatId(chatId, PageRequest.of(0, maxMessages))
                .stream()
                .map(ChatEntry::toMessage)
                .toList()
                .reversed();
    }

    @Override
    public void clear(String conversationId) {
        //not implemented
    }
}
