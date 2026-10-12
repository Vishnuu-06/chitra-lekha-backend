package com.chitralekha.service;

import com.chitralekha.entity.Content;
import com.chitralekha.entity.User;
import com.chitralekha.entity.UserContentActivity;
import com.chitralekha.exception.ResourceNotFoundException;
import com.chitralekha.repository.ContentRepository;
import com.chitralekha.repository.UserContentActivityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TrackingService {
    private final UserContentActivityRepository activityRepository;
    private final ContentRepository contentRepository;
    private final AuthService authService;

    public TrackingService(UserContentActivityRepository activityRepository,
                           ContentRepository contentRepository,
                           AuthService authService) {
        this.activityRepository = activityRepository;
        this.contentRepository = contentRepository;
        this.authService = authService;
    }

    @Transactional
    public UserContentActivity updateTracking(Long contentId, String status, Integer progress, Boolean isFavorite) {
        User user = authService.getCurrentUser();
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found: " + contentId));

        UserContentActivity activity = activityRepository.findByUserAndContent(user, content)
                .orElseGet(() -> new UserContentActivity(user, content, status, progress, isFavorite));

        if (status != null && !status.trim().isEmpty()) {
            activity.setStatus(status.toUpperCase().trim());
            if ("WATCHED".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status) || "READ".equalsIgnoreCase(status)) {
                if (activity.getCompletedAt() == null) {
                    activity.setCompletedAt(LocalDateTime.now());
                }
            } else if ("WATCHING".equalsIgnoreCase(status) || "READING".equalsIgnoreCase(status)) {
                if (activity.getStartedAt() == null) {
                    activity.setStartedAt(LocalDateTime.now());
                }
            }
        }
        if (progress != null) {
            activity.setProgress(Math.max(0, progress));
        }
        if (isFavorite != null) {
            activity.setIsFavorite(isFavorite);
        }

        return activityRepository.save(activity);
    }

    public Optional<UserContentActivity> getTrackingForContent(Long contentId) {
        Optional<User> userOpt = authService.getAuthenticatedUserOptional();
        if (userOpt.isEmpty()) return Optional.empty();

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found: " + contentId));

        return activityRepository.findByUserAndContent(userOpt.get(), content);
    }

    public List<UserContentActivity> getUserActivity(User user, String status) {
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            return activityRepository.findByUserAndStatusOrderByUpdatedAtDesc(user, status.toUpperCase().trim());
        }
        return activityRepository.findByUserOrderByUpdatedAtDesc(user);
    }

    @Transactional
    public void deleteTracking(Long contentId) {
        User user = authService.getCurrentUser();
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found: " + contentId));

        activityRepository.findByUserAndContent(user, content)
                .ifPresent(activityRepository::delete);
    }
}
