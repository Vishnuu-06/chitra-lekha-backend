package com.chitralekha.repository;

import com.chitralekha.entity.Review;
import com.chitralekha.entity.ReviewComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {
    List<ReviewComment> findByReviewOrderByCreatedAtAsc(Review review);
    long countByReview(Review review);
}
