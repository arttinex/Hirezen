package com.hirezen.service;

import com.hirezen.model.ConversationSummary;
import com.hirezen.model.Message;
import com.hirezen.model.User;
import com.hirezen.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;

    @Transactional
    public Message send(User sender, User recipient, String content) {
        Message saved = messageRepository.save(Message.builder()
                .sender(sender)
                .recipient(recipient)
                .content(content.trim())
                .build());
        log.info("Message sent: {} -> {}", sender.getEmail(), recipient.getEmail());
        return saved;
    }

    /**
     * Groups this user's messages into one row per conversation partner, with
     * their most recent message. A conversation is "primary" once the partner
     * has replied at least once - until then it sits in "general", the same
     * way an unaccepted message request works.
     */
    public List<ConversationSummary> conversationsFor(User user) {
        List<Message> all = messageRepository.findAllForUser(user);

        Map<Long, List<Message>> byPartner = new LinkedHashMap<>();
        for (Message m : all) {
            User partner = m.getSender().getId().equals(user.getId()) ? m.getRecipient() : m.getSender();
            byPartner.computeIfAbsent(partner.getId(), k -> new ArrayList<>()).add(m);
        }

        List<ConversationSummary> summaries = new ArrayList<>();
        for (List<Message> msgs : byPartner.values()) {
            Message last = msgs.get(0); // findAllForUser is already newest-first
            User partner = last.getSender().getId().equals(user.getId()) ? last.getRecipient() : last.getSender();
            boolean sentByMe = msgs.stream().anyMatch(m -> m.getSender().getId().equals(user.getId()));
            boolean sentByThem = msgs.stream().anyMatch(m -> m.getSender().getId().equals(partner.getId()));
            summaries.add(new ConversationSummary(partner, last, sentByMe && sentByThem));
        }

        summaries.sort((a, b) -> b.lastMessage().getSentAt().compareTo(a.lastMessage().getSentAt()));
        return summaries;
    }

    public List<Message> conversationWith(User user, User partner) {
        return messageRepository.findConversation(user, partner);
    }

    /** Marks every message the partner sent to this user as read - called when the thread is opened. */
    @Transactional
    public void markAsRead(User user, User partner) {
        List<Message> unreadIncoming = messageRepository.findConversation(user, partner).stream()
                .filter(m -> m.getRecipient().getId().equals(user.getId()) && !m.isReadFlag())
                .toList();
        unreadIncoming.forEach(m -> m.setReadFlag(true));
        messageRepository.saveAll(unreadIncoming);
    }
}
