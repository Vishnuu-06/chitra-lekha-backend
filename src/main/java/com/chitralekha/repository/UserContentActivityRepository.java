package com.chitralekha.repository;

import com.chitralekha.entity.Content;
import com.chitralekha.entity.User;
import com.chitralekha.entity.UserContentActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserContentActivityRepository extends JpaRepository<UserContentActivity, Long> {
    Optional<UserContentActivity> findByUserAndContent(User user, Content content);
    List<UserContentActivity> findByUserOrderByUpdatedAtDesc(User user);
    List<UserContentActivity> findByUserAndStatusOrderByUpdatedAtDesc(User user, String status);
    List<UserContentActivity> findByUserAndIsFavoriteTrue(User user);
    long countByUser(User user);
    long countByUserAndStatus(User user, String status);

    @Query("SELECT a FROM UserContentActivity a WHERE a.user = :user AND a.content.contentType = :contentType")
    List<UserContentActivity> findByUserAndContentType(@Param("user") User user, @Param("contentType") String contentType);
}
