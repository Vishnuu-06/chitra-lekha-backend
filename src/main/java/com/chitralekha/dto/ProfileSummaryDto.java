package com.chitralekha.dto;

public class ProfileSummaryDto {
    private Long id;
    private String displayName;
    private String bio;
    private String avatarUrl;
    private String favoriteMovies;
    private String favoriteShows;
    private String favoriteAnime;
    private String favoriteBooks;

    public ProfileSummaryDto() {}

    public ProfileSummaryDto(Long id, String displayName, String bio, String avatarUrl,
                            String favoriteMovies, String favoriteShows, String favoriteAnime, String favoriteBooks) {
        this.id = id;
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
}
