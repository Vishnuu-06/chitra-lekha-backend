package com.chitralekha.service;

import com.chitralekha.entity.*;
import com.chitralekha.exception.ResourceNotFoundException;
import com.chitralekha.integration.ContentNormalizationService;
import com.chitralekha.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MessagingService {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final UserBlockRepository blockRepository;
    private final ContentRepository contentRepository;
    private final ReviewRepository reviewRepository;
    private final ContentListRepository listRepository;
    private final NotificationRepository notificationRepository;
    private final ContentNormalizationService normalizationService;
    private final AuthService authService;

    public MessagingService(
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            UserRepository userRepository,
            ProfileRepository profileRepository,
            UserBlockRepository blockRepository,
            ContentRepository contentRepository,
            ReviewRepository reviewRepository,
            ContentListRepository listRepository,
            NotificationRepository notificationRepository,
            ContentNormalizationService normalizationService,
            AuthService authService) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.blockRepository = blockRepository;
        this.contentRepository = contentRepository;
        this.reviewRepository = reviewRepository;
        this.listRepository = listRepository;
        this.notificationRepository = notificationRepository;
        this.normalizationService = normalizationService;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getConversations() {
        User current = authService.getCurrentUser();
        List<Conversation> conversations = conversationRepository.findAllForUser(current);

        return conversations.stream().map(c -> {
            User otherUser = c.getUserOne().getId().equals(current.getId()) ? c.getUserTwo() : c.getUserOne();
            Profile otherProfile = profileRepository.findByUserId(otherUser.getId()).orElse(null);
            Message latest = messageRepository.findLatestMessage(c);

            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("otherUser", Map.of(
                    "id", otherUser.getId(),
                    "username", otherUser.getUsername(),
                    "displayName", otherProfile != null ? otherProfile.getDisplayName() : otherUser.getUsername(),
                    "avatarUrl", otherProfile != null && otherProfile.getAvatarUrl() != null ? otherProfile.getAvatarUrl() : ""
            ));
            if (latest != null) {
                map.put("latestMessage", latest.getBody());
                map.put("latestTimestamp", latest.getCreatedAt().toString());
                map.put("isUnread", latest.getReceiver().getId().equals(current.getId()) && !latest.getIsRead());
            } else {
                map.put("latestMessage", "");
                map.put("latestTimestamp", c.getCreatedAt().toString());
                map.put("isUnread", false);
            }
            return map;
        }).toList();
    }

    @Transactional
    public Conversation getOrCreateConversation(String targetUsername) {
        User current = authService.getCurrentUser();
        User target = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + targetUsername));

        if (current.getId().equals(target.getId())) {
            throw new IllegalArgumentException("Cannot start a conversation with yourself");
        }

        if (blockRepository.isBlockedEitherWay(current, target)) {
            throw new IllegalStateException("Cannot message this user due to privacy and blocking settings");
        }

        return conversationRepository.findBetweenUsers(current, target)
                .orElseGet(() -> conversationRepository.save(new Conversation(current, target)));
    }

    @Transactional
    public List<Map<String, Object>> getConversationMessages(Long conversationId) {
        User current = authService.getCurrentUser();
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));

        // Strict authorization check
        if (!conv.getUserOne().getId().equals(current.getId()) && !conv.getUserTwo().getId().equals(current.getId())) {
            throw new IllegalStateException("You are not authorized to view this conversation");
        }

        List<Message> messages = messageRepository.findByConversationOrderByCreatedAtAsc(conv);

        // Mark incoming messages as read
        for (Message m : messages) {
            if (m.getReceiver().getId().equals(current.getId()) && !m.getIsRead()) {
                m.setIsRead(true);
                messageRepository.save(m);
            }
        }

        return messages.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("senderId", m.getSender().getId());
            map.put("senderUsername", m.getSender().getUsername());
            map.put("body", m.getBody());
            map.put("createdAt", m.getCreatedAt().toString());
            map.put("isMine", m.getSender().getId().equals(current.getId()));

            if (m.getSharedContent() != null) {
                map.put("sharedContent", normalizationService.toDto(m.getSharedContent()));
            }
            return map;
        }).toList();
    }

    @Transactional
    public Map<String, Object> sendMessage(Long conversationId, String body, Long sharedContentId) {
        if (body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("Message body cannot be empty");
        }

        User current = authService.getCurrentUser();
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));

        if (!conv.getUserOne().getId().equals(current.getId()) && !conv.getUserTwo().getId().equals(current.getId())) {
            throw new IllegalStateException("You are not authorized to send messages in this conversation");
        }

        User receiver = conv.getUserOne().getId().equals(current.getId()) ? conv.getUserTwo() : conv.getUserOne();

        if (blockRepository.isBlockedEitherWay(current, receiver)) {
            throw new IllegalStateException("Cannot message this user due to privacy and blocking settings");
        }

        Message message = new Message(conv, current, receiver, body.trim());

        if (sharedContentId != null) {
            contentRepository.findById(sharedContentId).ifPresent(message::setSharedContent);
        }

        conv.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conv);
        Message saved = messageRepository.save(message);

        // Create genuine notification
        notificationRepository.save(new Notification(
                receiver,
                current,
                current.getUsername() + " sent you a message.",
                "MESSAGE",
                "/messages"
        ));

        Map<String, Object> res = new HashMap<>();
        res.put("id", saved.getId());
        res.put("senderId", current.getId());
        res.put("senderUsername", current.getUsername());
        res.put("body", saved.getBody());
        res.put("createdAt", saved.getCreatedAt().toString());
        res.put("isMine", true);
        if (saved.getSharedContent() != null) {
            res.put("sharedContent", normalizationService.toDto(saved.getSharedContent()));
        }
        return res;
    }
}
