package com.chitralekha.repository;

import com.chitralekha.entity.Content;
import com.chitralekha.entity.Rating;
import com.chitralekha.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByUserAndContent(User user, Content content);
    List<Rating> findByUserOrderByCreatedAtDesc(User user);
    List<Rating> findByContentOrderByCreatedAtDesc(Content content);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.content = :content")
    Double getAverageScoreForContent(@Param("content") Content content);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.content = :content")
    Long countRatingsForContent(@Param("content") Content content);

    @Query("SELECT r FROM Rating r WHERE r.user = :user AND r.score >= 7 ORDER BY r.score DESC")
    List<Rating> findHighlyRatedByUser(@Param("user") User user);
}
