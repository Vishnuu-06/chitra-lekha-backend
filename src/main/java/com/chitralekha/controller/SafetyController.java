package com.chitralekha.controller;

import com.chitralekha.entity.ContentReport;
import com.chitralekha.service.SafetyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/safety")
public class SafetyController {
    private final SafetyService safetyService;

    public SafetyController(SafetyService safetyService) {
        this.safetyService = safetyService;
    }

    @PostMapping("/report")
    public ResponseEntity<Map<String, Object>> submitReport(@RequestBody Map<String, Object> payload) {
        String reportedType = (String) payload.get("reportedType");
        Long targetId = payload.get("targetId") != null ? ((Number) payload.get("targetId")).longValue() : null;
        String reason = (String) payload.get("reason");
        String details = (String) payload.get("details");

        ContentReport report = safetyService.submitReport(reportedType, targetId, reason, details);
        return ResponseEntity.ok(Map.of(
                "id", report.getId(),
                "status", report.getStatus(),
                "message", "Report submitted for moderator review"
        ));
    }

    @PostMapping("/block/{username}")
    public ResponseEntity<Map<String, Object>> toggleBlock(@PathVariable String username) {
        boolean blocked = safetyService.toggleBlockUser(username);
        return ResponseEntity.ok(Map.of(
                "blocked", blocked,
                "message", blocked ? "User blocked" : "User unblocked"
        ));
    }

    @GetMapping("/block/{username}")
    public ResponseEntity<Map<String, Boolean>> isBlocked(@PathVariable String username) {
        return ResponseEntity.ok(Map.of("blocked", safetyService.isUserBlocked(username)));
    }

    @GetMapping("/blocked")
    public ResponseEntity<List<Map<String, Object>>> getBlockedList() {
        return ResponseEntity.ok(safetyService.getBlockedUsers());
    }
}
