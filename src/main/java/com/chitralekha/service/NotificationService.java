package com.chitralekha.service;

import com.chitralekha.entity.Notification;
import com.chitralekha.entity.User;
import com.chitralekha.exception.ResourceNotFoundException;
import com.chitralekha.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final AuthService authService;

    public NotificationService(NotificationRepository notificationRepository, AuthService authService) {
        this.notificationRepository = notificationRepository;
        this.authService = authService;
    }

    public List<Map<String, Object>> getNotifications() {
        User user = authService.getCurrentUser();
        List<Notification> list = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        return list.stream().map(n -> Map.<String, Object>of(
                "id", n.getId(),
                "message", n.getMessage(),
                "type", n.getType(),
                "targetUrl", n.getTargetUrl() != null ? n.getTargetUrl() : "",
                "readFlag", n.getReadFlag(),
                "createdAt", n.getCreatedAt().toString()
        )).toList();
    }

    public long getUnreadCount() {
        User user = authService.getCurrentUser();
        return notificationRepository.countByUserAndReadFlagFalse(user);
    }

    @Transactional
    public void markAsRead(Long id) {
        User user = authService.getCurrentUser();
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));

        if (notification.getUser().getId().equals(user.getId())) {
            notification.setReadFlag(true);
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void markAllAsRead() {
        User user = authService.getCurrentUser();
        List<Notification> list = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        for (Notification n : list) {
            if (!n.getReadFlag()) {
                n.setReadFlag(true);
                notificationRepository.save(n);
            }
        }
    }
}
