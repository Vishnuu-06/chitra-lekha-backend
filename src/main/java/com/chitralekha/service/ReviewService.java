package com.chitralekha.service;

import com.chitralekha.entity.*;
import com.chitralekha.exception.ResourceNotFoundException;
import com.chitralekha.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {
    private final RatingRepository ratingRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final ContentRepository contentRepository;
    private final AuthService authService;

    public ReviewService(
            RatingRepository ratingRepository,
            ReviewRepository reviewRepository,
            ReviewLikeRepository reviewLikeRepository,
            ReviewCommentRepository reviewCommentRepository,
            ContentRepository contentRepository,
            AuthService authService) {
        this.ratingRepository = ratingRepository;
        this.reviewRepository = reviewRepository;
        this.reviewLikeRepository = reviewLikeRepository;
        this.reviewCommentRepository = reviewCommentRepository;
        this.contentRepository = contentRepository;
        this.authService = authService;
    }

    @Transactional
    public Rating rateContent(Long contentId, int score, String shortNote) {
        if (score < 1 || score > 10) {
            throw new IllegalArgumentException("Rating score must be between 1 and 10");
        }
        User user = authService.getCurrentUser();
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found: " + contentId));

        Rating rating = ratingRepository.findByUserAndContent(user, content)
                .orElseGet(() -> new Rating(user, content, score, shortNote));

        rating.setScore(score);
        if (shortNote != null) {
            rating.setReview(shortNote.trim());
        }
        return ratingRepository.save(rating);
    }

    @Transactional
    public void deleteRating(Long contentId) {
        User user = authService.getCurrentUser();
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found: " + contentId));

        ratingRepository.findByUserAndContent(user, content)
                .ifPresent(ratingRepository::delete);
    }

    @Transactional
    public Review createReview(Long contentId, String title, String body, Integer score, Boolean containsSpoilers) {
        if (body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("Review body cannot be empty");
        }
        User user = authService.getCurrentUser();
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found: " + contentId));

        if (score != null && (score < 1 || score > 10)) {
            throw new IllegalArgumentException("Rating score must be between 1 and 10");
        }

        if (score != null) {
            rateContent(contentId, score, null);
        }

        Review review = new Review(user, content, title, body.trim(), score, containsSpoilers);
        return reviewRepository.save(review);
    }

    @Transactional
    public Review updateReview(Long reviewId, String title, String body, Integer score, Boolean containsSpoilers) {
        User user = authService.getCurrentUser();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("You are not authorized to edit this review");
        }

        if (body != null && !body.trim().isEmpty()) {
            review.setBody(body.trim());
        }
        if (title != null) {
            review.setTitle(title.trim());
        }
        if (score != null) {
            if (score < 1 || score > 10) {
                throw new IllegalArgumentException("Rating score must be between 1 and 10");
            }
            review.setRatingScore(score);
            rateContent(review.getContent().getId(), score, null);
        }
        if (containsSpoilers != null) {
            review.setContainsSpoilers(containsSpoilers);
        }

        return reviewRepository.save(review);
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        User user = authService.getCurrentUser();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("You are not authorized to delete this review");
        }
        reviewRepository.delete(review);
    }

    @Transactional
    public boolean toggleLikeReview(Long reviewId) {
        User user = authService.getCurrentUser();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));

        Optional<ReviewLike> existing = reviewLikeRepository.findByReviewAndUser(review, user);
        if (existing.isPresent()) {
            reviewLikeRepository.delete(existing.get());
            return false;
        } else {
            reviewLikeRepository.save(new ReviewLike(review, user));
            return true;
        }
    }

    @Transactional
    public ReviewComment addComment(Long reviewId, String body) {
        if (body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment body cannot be empty");
        }
        User user = authService.getCurrentUser();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));

        return reviewCommentRepository.save(new ReviewComment(review, user, body.trim()));
    }

    public List<ReviewComment> getComments(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));
        return reviewCommentRepository.findByReviewOrderByCreatedAtAsc(review);
    }
}
