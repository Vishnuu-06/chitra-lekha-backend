package com.chitralekha.dto;

public class ContentSummaryDto {
    private Long id;
    private String contentType;
    private String provider;
    private String externalId;
    private String title;
    private String description;
    private String posterUrl;
    private String backdropUrl;
    private Integer releaseYear;
    private String genres;
    private String creators;
    private Integer totalItems;
    private Double averageRating;
    private Long ratingsCount;
    private Long reviewsCount;

    public ContentSummaryDto() {}

    public ContentSummaryDto(Long id, String contentType, String provider, String externalId,
                            String title, String description, String posterUrl, Integer releaseYear, String genres) {
        this(id, contentType, provider, externalId, title, description, posterUrl, null, releaseYear, genres, null, null, null, null, null);
    }

    public ContentSummaryDto(Long id, String contentType, String provider, String externalId,
                            String title, String description, String posterUrl, String backdropUrl,
                            Integer releaseYear, String genres, String creators, Integer totalItems,
                            Double averageRating, Long ratingsCount, Long reviewsCount) {
        this.id = id;
        this.contentType = contentType;
        this.provider = provider;
        this.externalId = externalId;
        this.title = title;
        this.description = description;
        this.posterUrl = posterUrl;
        this.backdropUrl = backdropUrl;
        this.releaseYear = releaseYear;
        this.genres = genres;
        this.creators = creators;
        this.totalItems = totalItems;
        this.averageRating = averageRating;
        this.ratingsCount = ratingsCount;
        this.reviewsCount = reviewsCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public String getBackdropUrl() { return backdropUrl; }
    public void setBackdropUrl(String backdropUrl) { this.backdropUrl = backdropUrl; }
    public Integer getReleaseYear() { return releaseYear; }
    public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }
    public String getGenres() { return genres; }
    public void setGenres(String genres) { this.genres = genres; }
    public String getCreators() { return creators; }
    public void setCreators(String creators) { this.creators = creators; }
    public Integer getTotalItems() { return totalItems; }
    public void setTotalItems(Integer totalItems) { this.totalItems = totalItems; }
    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }
    public Long getRatingsCount() { return ratingsCount; }
    public void setRatingsCount(Long ratingsCount) { this.ratingsCount = ratingsCount; }
    public Long getReviewsCount() { return reviewsCount; }
    public void setReviewsCount(Long reviewsCount) { this.reviewsCount = reviewsCount; }
}
