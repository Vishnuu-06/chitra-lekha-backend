package com.chitralekha.service;

import com.chitralekha.entity.Content;
import com.chitralekha.entity.Rating;
import com.chitralekha.entity.Review;
import com.chitralekha.entity.User;
import com.chitralekha.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private RatingRepository ratingRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ReviewLikeRepository reviewLikeRepository;
    @Mock
    private ReviewCommentRepository reviewCommentRepository;
    @Mock
    private ContentRepository contentRepository;

    private User currentUser;
    private AuthService testAuthService;
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        currentUser = new User("viewer", "viewer@example.com", "pass", true);
        currentUser.setId(10L);

        testAuthService = new AuthService(null, null, null, null, null) {
            @Override
            public User getCurrentUser() {
                return currentUser;
            }
        };

        reviewService = new ReviewService(
                ratingRepository,
                reviewRepository,
                reviewLikeRepository,
                reviewCommentRepository,
                contentRepository,
                testAuthService
        );
    }

    @Test
    void rateContent_ShouldRejectInvalidScore() {
        assertThrows(IllegalArgumentException.class, () ->
                reviewService.rateContent(1L, 0, "Too low")
        );

        assertThrows(IllegalArgumentException.class, () ->
                reviewService.rateContent(1L, 11, "Too high")
        );
    }

    @Test
    void rateContent_ShouldSaveValidScore() {
        Content content = new Content("MOVIE", "TMDB", "101", "Dune", "Desc", "url", 2021, "Sci-Fi");
        content.setId(101L);

        when(contentRepository.findById(101L)).thenReturn(Optional.of(content));
        when(ratingRepository.findByUserAndContent(currentUser, content)).thenReturn(Optional.empty());

        Rating savedRating = new Rating(currentUser, content, 9, "Exceptional scale");
        when(ratingRepository.save(any(Rating.class))).thenReturn(savedRating);

        Rating result = reviewService.rateContent(101L, 9, "Exceptional scale");

        assertNotNull(result);
        assertEquals(9, result.getScore());
        verify(ratingRepository).save(any(Rating.class));
    }

    @Test
    void createReview_ShouldRejectEmptyBody() {
        assertThrows(IllegalArgumentException.class, () ->
                reviewService.createReview(101L, "Title", "   ", 8, false)
        );
    }

    @Test
    void createReview_ShouldPersistValidReviewWithSpoilers() {
        Content content = new Content("BOOK", "GoogleBooks", "gb-1", "Dune", "Desc", "url", 1965, "Sci-Fi");
        content.setId(202L);

        when(contentRepository.findById(202L)).thenReturn(Optional.of(content));
        when(ratingRepository.findByUserAndContent(currentUser, content)).thenReturn(Optional.empty());

        Review review = new Review(currentUser, content, "Masterwork", "Deep themes of ecology.", 10, true);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        Review result = reviewService.createReview(202L, "Masterwork", "Deep themes of ecology.", 10, true);

        assertNotNull(result);
        assertTrue(result.getContainsSpoilers());
        assertEquals("Masterwork", result.getTitle());
        verify(reviewRepository).save(any(Review.class));
    }
}
