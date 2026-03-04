package org.learn.springai.services;

import lombok.Builder;
import org.learn.springai.model.Chat;
import org.learn.springai.model.ChatEntry;
import org.learn.springai.repo.ChatRepository;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.util.Comparator;
import java.util.List;

@Builder
public class PostgresChatMemory implements ChatMemory {

    private ChatRepository chatMemoryRepository;

    private int maxMessages;

    @Override
    public void add(String conversationId, List<Message> messages) {
        Chat chat = chatMemoryRepository.findById(Long.valueOf(conversationId)).orElseThrow();
        for (Message message : messages) {
            chat.addChatEntry(ChatEntry.toChatEntry(message));
        }
        chatMemoryRepository.save(chat);
    }

    @Override
    public List<Message> get(String conversationId) {
        Chat chat = chatMemoryRepository.findById(Long.valueOf(conversationId)).orElseThrow();
        return chat.getHistory().stream()
                .sorted(Comparator.comparing(ChatEntry::getCreatedAt))
                .map(ChatEntry::toMessage)
                .limit(maxMessages)
                .toList();

    }

    @Override
    public void clear(String conversationId) {
        //not implemented
    }
}
