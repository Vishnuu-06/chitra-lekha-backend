package com.chitralekha.repository;

import com.chitralekha.entity.Follow;
import com.chitralekha.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    Optional<Follow> findByFollowerAndFollowee(User follower, User followee);
    boolean existsByFollowerAndFollowee(User follower, User followee);
    long countByFollowee(User user);
    long countByFollower(User user);
    List<Follow> findByFollowee(User user);
    List<Follow> findByFollower(User user);

    @Query("SELECT f.followee.id FROM Follow f WHERE f.follower = :user")
    List<Long> findFolloweeIds(@Param("user") User user);
}
