package com.chitralekha.service;

import com.chitralekha.entity.ContentReport;
import com.chitralekha.entity.User;
import com.chitralekha.entity.UserBlock;
import com.chitralekha.exception.ResourceNotFoundException;
import com.chitralekha.repository.ContentReportRepository;
import com.chitralekha.repository.UserBlockRepository;
import com.chitralekha.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SafetyService {
    private final ContentReportRepository reportRepository;
    private final UserBlockRepository blockRepository;
    private final UserRepository userRepository;
    private final AuthService authService;

    public SafetyService(
            ContentReportRepository reportRepository,
            UserBlockRepository blockRepository,
            UserRepository userRepository,
            AuthService authService) {
        this.reportRepository = reportRepository;
        this.blockRepository = blockRepository;
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @Transactional
    public ContentReport submitReport(String reportedType, Long targetId, String reason, String details) {
        if (reportedType == null || reason == null || reason.trim().isEmpty() || targetId == null) {
            throw new IllegalArgumentException("Reported type, target, and reason are required");
        }
        User reporter = authService.getCurrentUser();
        ContentReport report = new ContentReport(reporter, reportedType.toUpperCase().trim(), targetId, reason.trim(), details != null ? details.trim() : "");
        return reportRepository.save(report);
    }

    @Transactional
    public boolean toggleBlockUser(String targetUsername) {
        User blocker = authService.getCurrentUser();
        User target = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + targetUsername));

        if (blocker.getId().equals(target.getId())) {
            throw new IllegalArgumentException("You cannot block yourself");
        }

        Optional<UserBlock> existing = blockRepository.findByBlockerAndBlocked(blocker, target);
        if (existing.isPresent()) {
            blockRepository.delete(existing.get());
            return false;
        } else {
            blockRepository.save(new UserBlock(blocker, target));
            return true;
        }
    }

    public boolean isUserBlocked(String targetUsername) {
        Optional<User> currentOpt = authService.getAuthenticatedUserOptional();
        if (currentOpt.isEmpty()) return false;

        return userRepository.findByUsername(targetUsername)
                .map(target -> blockRepository.existsByBlockerAndBlocked(currentOpt.get(), target))
                .orElse(false);
    }

    public List<Map<String, Object>> getBlockedUsers() {
        User blocker = authService.getCurrentUser();
        return blockRepository.findByBlocker(blocker).stream().map(b -> Map.<String, Object>of(
                "id", b.getBlocked().getId(),
                "username", b.getBlocked().getUsername()
        )).toList();
    }
}
