package com.chitralekha.controller;

import com.chitralekha.entity.Conversation;
import com.chitralekha.service.MessagingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {
    private final MessagingService messagingService;

    public ConversationController(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getConversations() {
        return ResponseEntity.ok(messagingService.getConversations());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> getOrCreateConversation(@RequestBody Map<String, String> payload) {
        String targetUsername = payload.get("username");
        if (targetUsername == null || targetUsername.trim().isEmpty()) {
            throw new IllegalArgumentException("Target username is required");
        }
        Conversation c = messagingService.getOrCreateConversation(targetUsername.trim());
        return ResponseEntity.ok(Map.of("id", c.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<Map<String, Object>>> getMessages(@PathVariable Long id) {
        return ResponseEntity.ok(messagingService.getConversationMessages(id));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        String body = (String) payload.get("body");
        Long sharedContentId = payload.get("sharedContentId") != null ? ((Number) payload.get("sharedContentId")).longValue() : null;
        return ResponseEntity.ok(messagingService.sendMessage(id, body, sharedContentId));
    }
}
