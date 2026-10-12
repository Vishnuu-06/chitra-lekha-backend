package com.chitralekha.controller;

import com.chitralekha.entity.Rating;
import com.chitralekha.entity.Review;
import com.chitralekha.entity.ReviewComment;
import com.chitralekha.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/ratings/{contentId}")
    public ResponseEntity<Map<String, Object>> rateContent(
            @PathVariable Long contentId,
            @RequestBody Map<String, Object> payload) {
        int score = ((Number) payload.get("score")).intValue();
        String note = (String) payload.get("review");
        Rating rating = reviewService.rateContent(contentId, score, note);
        return ResponseEntity.ok(Map.of(
                "id", rating.getId(),
                "contentId", rating.getContent().getId(),
                "score", rating.getScore(),
                "review", rating.getReview() != null ? rating.getReview() : ""
        ));
    }

    @DeleteMapping("/ratings/{contentId}")
    public ResponseEntity<Map<String, String>> deleteRating(@PathVariable Long contentId) {
        reviewService.deleteRating(contentId);
        return ResponseEntity.ok(Map.of("message", "Rating removed"));
    }

    @PostMapping("/reviews/{contentId}")
    public ResponseEntity<Map<String, Object>> createReview(
            @PathVariable Long contentId,
            @RequestBody Map<String, Object> payload) {
        String title = (String) payload.get("title");
        String body = (String) payload.get("body");
        Integer score = payload.get("score") != null ? ((Number) payload.get("score")).intValue() : null;
        Boolean spoilers = payload.get("containsSpoilers") != null ? (Boolean) payload.get("containsSpoilers") : false;

        if (body != null && !body.trim().isEmpty()) {
            Review review = reviewService.createReview(contentId, title, body, score, spoilers);
            return ResponseEntity.ok(toReviewMap(review));
        } else if (score != null) {
            Rating rating = reviewService.rateContent(contentId, score, title);
            return ResponseEntity.ok(Map.of(
                    "id", rating.getId(),
                    "ratingScore", rating.getScore(),
                    "title", title != null ? title : "",
                    "body", "",
                    "containsSpoilers", false,
                    "createdAt", java.time.LocalDateTime.now().toString(),
                    "author", Map.of(
                            "id", rating.getUser().getId(),
                            "username", rating.getUser().getUsername()
                    )
            ));
        } else {
            throw new IllegalArgumentException("Either a rating score or review text must be provided");
        }
    }

    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<Map<String, Object>> updateReview(
            @PathVariable Long reviewId,
            @RequestBody Map<String, Object> payload) {
        String title = (String) payload.get("title");
        String body = (String) payload.get("body");
        Integer score = payload.get("score") != null ? ((Number) payload.get("score")).intValue() : null;
        Boolean spoilers = (Boolean) payload.get("containsSpoilers");

        Review review = reviewService.updateReview(reviewId, title, body, score, spoilers);
        return ResponseEntity.ok(toReviewMap(review));
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Map<String, String>> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(Map.of("message", "Review deleted"));
    }

    @PostMapping("/reviews/{reviewId}/like")
    public ResponseEntity<Map<String, Object>> toggleLikeReview(@PathVariable Long reviewId) {
        boolean liked = reviewService.toggleLikeReview(reviewId);
        return ResponseEntity.ok(Map.of("liked", liked));
    }

    @PostMapping("/reviews/{reviewId}/comments")
    public ResponseEntity<Map<String, Object>> addComment(
            @PathVariable Long reviewId,
            @RequestBody Map<String, String> payload) {
        String body = payload.get("body");
        ReviewComment comment = reviewService.addComment(reviewId, body);
        return ResponseEntity.ok(Map.of(
                "id", comment.getId(),
                "body", comment.getBody(),
                "author", comment.getUser().getUsername(),
                "createdAt", comment.getCreatedAt().toString()
        ));
    }

    @GetMapping("/reviews/{reviewId}/comments")
    public ResponseEntity<List<Map<String, Object>>> getComments(@PathVariable Long reviewId) {
        List<ReviewComment> comments = reviewService.getComments(reviewId);
        List<Map<String, Object>> result = comments.stream().map(c -> Map.<String, Object>of(
                "id", c.getId(),
                "body", c.getBody(),
                "author", c.getUser().getUsername(),
                "createdAt", c.getCreatedAt().toString()
        )).toList();
        return ResponseEntity.ok(result);
    }

    private Map<String, Object> toReviewMap(Review r) {
        return Map.of(
                "id", r.getId(),
                "title", r.getTitle() != null ? r.getTitle() : "",
                "body", r.getBody(),
                "ratingScore", r.getRatingScore() != null ? r.getRatingScore() : 0,
                "containsSpoilers", r.getContainsSpoilers(),
                "createdAt", r.getCreatedAt().toString(),
                "author", Map.of(
                        "id", r.getUser().getId(),
                        "username", r.getUser().getUsername()
                )
        );
    }
}
