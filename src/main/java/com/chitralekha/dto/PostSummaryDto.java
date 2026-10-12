package com.chitralekha.dto;

import java.time.LocalDateTime;

public class PostSummaryDto {
    private Long id;
    private Long userId;
    private String username;
    private String displayName;
    private String avatarUrl;
    private ContentSummaryDto content;
    private String body;
    private LocalDateTime createdAt;

    public PostSummaryDto() {}

    public PostSummaryDto(Long id, Long userId, String username, String displayName, String avatarUrl,
                         ContentSummaryDto content, String body, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.content = content;
        this.body = body;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public ContentSummaryDto getContent() { return content; }
    public void setContent(ContentSummaryDto content) { this.content = content; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
