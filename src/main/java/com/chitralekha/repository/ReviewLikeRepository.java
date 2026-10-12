package com.chitralekha.repository;

import com.chitralekha.entity.Review;
import com.chitralekha.entity.ReviewLike;
import com.chitralekha.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    Optional<ReviewLike> findByReviewAndUser(Review review, User user);
    long countByReview(Review review);
    boolean existsByReviewAndUser(Review review, User user);
}
