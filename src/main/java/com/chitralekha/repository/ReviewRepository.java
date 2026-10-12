package com.chitralekha.repository;

import com.chitralekha.entity.Content;
import com.chitralekha.entity.Review;
import com.chitralekha.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByContentOrderByCreatedAtDesc(Content content);
    List<Review> findByUserOrderByCreatedAtDesc(User user);
    Optional<Review> findByUserAndContent(User user, Content content);
    long countByUser(User user);
    long countByContent(Content content);
}
