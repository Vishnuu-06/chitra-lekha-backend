package com.chitralekha.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "profiles")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(length = 500)
    private String bio;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "favorite_movies", length = 255)
    private String favoriteMovies;

    @Column(name = "favorite_shows", length = 255)
    private String favoriteShows;

    @Column(name = "favorite_anime", length = 255)
    private String favoriteAnime;

    @Column(name = "favorite_books", length = 255)
    private String favoriteBooks;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Profile() {
    }

    public Profile(User user, String displayName, String bio, String avatarUrl, String favoriteMovies, String favoriteShows, String favoriteAnime, String favoriteBooks) {
        this.user = user;
        this.displayName = displayName;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.favoriteMovies = favoriteMovies;
        this.favoriteShows = favoriteShows;
        this.favoriteAnime = favoriteAnime;
        this.favoriteBooks = favoriteBooks;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getFavoriteMovies() { return favoriteMovies; }
    public void setFavoriteMovies(String favoriteMovies) { this.favoriteMovies = favoriteMovies; }
    public String getFavoriteShows() { return favoriteShows; }
    public void setFavoriteShows(String favoriteShows) { this.favoriteShows = favoriteShows; }
    public String getFavoriteAnime() { return favoriteAnime; }
    public void setFavoriteAnime(String favoriteAnime) { this.favoriteAnime = favoriteAnime; }
    public String getFavoriteBooks() { return favoriteBooks; }
    public void setFavoriteBooks(String favoriteBooks) { this.favoriteBooks = favoriteBooks; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
