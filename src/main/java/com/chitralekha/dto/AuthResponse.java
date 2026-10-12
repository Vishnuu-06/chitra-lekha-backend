package com.chitralekha.dto;

public class AuthResponse {
    private Long id;
    private String token;
    private String username;
    private String email;
    private String displayName;
    private String avatarUrl;
    private String message;

    public AuthResponse() {}

    public AuthResponse(Long id, String token, String username, String email, String displayName, String avatarUrl, String message) {
        this.id = id;
        this.token = token;
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.avatarUrl = avatarUrl;
        this.message = message;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
