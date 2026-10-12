package com.chitralekha.controller;

import com.chitralekha.dto.ContentSummaryDto;
import com.chitralekha.entity.User;
import com.chitralekha.entity.UserContentActivity;
import com.chitralekha.exception.ResourceNotFoundException;
import com.chitralekha.integration.ContentNormalizationService;
import com.chitralekha.repository.UserRepository;
import com.chitralekha.service.AuthService;
import com.chitralekha.service.TrackingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tracking")
public class TrackingController {
    private final TrackingService trackingService;
    private final UserRepository userRepository;
    private final ContentNormalizationService normalizationService;
    private final AuthService authService;

    public TrackingController(TrackingService trackingService,
                              UserRepository userRepository,
                              ContentNormalizationService normalizationService,
                              AuthService authService) {
        this.trackingService = trackingService;
        this.userRepository = userRepository;
        this.normalizationService = normalizationService;
        this.authService = authService;
    }

    @PostMapping("/{contentId}")
    public ResponseEntity<Map<String, Object>> updateTracking(
            @PathVariable Long contentId,
            @RequestBody Map<String, Object> payload) {
        String status = (String) payload.get("status");
        Integer progress = payload.get("progress") != null ? ((Number) payload.get("progress")).intValue() : 0;
        Boolean isFavorite = (Boolean) payload.get("isFavorite");

        UserContentActivity activity = trackingService.updateTracking(contentId, status, progress, isFavorite);
        return ResponseEntity.ok(toMap(activity));
    }

    @GetMapping("/{contentId}")
    public ResponseEntity<?> getTrackingForContent(@PathVariable Long contentId) {
        Optional<UserContentActivity> activityOpt = trackingService.getTrackingForContent(contentId);
        if (activityOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("tracked", false));
        }
        return ResponseEntity.ok(toMap(activityOpt.get()));
    }

    @DeleteMapping("/{contentId}")
    public ResponseEntity<Map<String, String>> deleteTracking(@PathVariable Long contentId) {
        trackingService.deleteTracking(contentId);
        return ResponseEntity.ok(Map.of("message", "Tracking record removed"));
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<Map<String, Object>>> getUserTracking(
            @PathVariable String username,
            @RequestParam(name = "status", required = false) String status) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        List<UserContentActivity> activities = trackingService.getUserActivity(user, status);
        List<Map<String, Object>> result = activities.stream().map(this::toMap).toList();
        return ResponseEntity.ok(result);
    }

    private Map<String, Object> toMap(UserContentActivity a) {
        return Map.of(
                "id", a.getId(),
                "contentId", a.getContent().getId(),
                "content", normalizationService.toDto(a.getContent()),
                "status", a.getStatus(),
                "progress", a.getProgress(),
                "isFavorite", a.getIsFavorite(),
                "updatedAt", a.getUpdatedAt().toString()
        );
    }
}
