package com.chitralekha.integration;

import com.chitralekha.dto.ContentSummaryDto;
import com.chitralekha.entity.Content;
import com.chitralekha.repository.ContentRepository;
import com.chitralekha.repository.RatingRepository;
import com.chitralekha.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContentNormalizationService {
    private final ContentRepository contentRepository;
    private final RatingRepository ratingRepository;
    private final ReviewRepository reviewRepository;

    public ContentNormalizationService(ContentRepository contentRepository,
                                      RatingRepository ratingRepository,
                                      ReviewRepository reviewRepository) {
        this.contentRepository = contentRepository;
        this.ratingRepository = ratingRepository;
        this.reviewRepository = reviewRepository;
    }

    public ContentSummaryDto toDto(Content content) {
        if (content == null) return null;

        Double avgScore = ratingRepository.getAverageScoreForContent(content);
        Long ratingsCount = ratingRepository.countRatingsForContent(content);
        Long reviewsCount = reviewRepository.countByContent(content);

        return new ContentSummaryDto(
                content.getId(),
                content.getContentType(),
                content.getProvider(),
                content.getExternalId(),
                content.getTitle(),
                content.getDescription(),
                content.getPosterUrl(),
                content.getBackdropUrl(),
                content.getReleaseYear(),
                content.getGenres(),
                content.getCreators(),
                content.getTotalItems(),
                avgScore != null ? Math.round(avgScore * 10.0) / 10.0 : null,
                ratingsCount != null ? ratingsCount : 0L,
                reviewsCount != null ? reviewsCount : 0L
        );
    }

    @Transactional
    public Content findOrCreate(String contentType, String provider, String externalId,
                               String title, String description, String posterUrl,
                               String backdropUrl, Integer releaseYear, String genres,
                               String creators, Integer totalItems) {
        return contentRepository.findByProviderAndExternalId(provider, externalId)
                .orElseGet(() -> {
                    Content content = new Content(
                            contentType.toUpperCase().trim(),
                            provider.toUpperCase().trim(),
                            externalId.trim(),
                            title != null ? title.trim() : "Untitled",
                            description != null ? description.trim() : "",
                            posterUrl != null ? posterUrl.trim() : null,
                            backdropUrl != null ? backdropUrl.trim() : null,
                            releaseYear,
                            genres != null ? genres.trim() : "",
                            creators != null ? creators.trim() : "",
                            totalItems
                    );
                    return contentRepository.save(content);
                });
    }
}
