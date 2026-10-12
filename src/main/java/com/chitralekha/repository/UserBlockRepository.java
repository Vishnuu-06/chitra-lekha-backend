package com.chitralekha.repository;

import com.chitralekha.entity.User;
import com.chitralekha.entity.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {
    Optional<UserBlock> findByBlockerAndBlocked(User blocker, User blocked);
    boolean existsByBlockerAndBlocked(User blocker, User blocked);
    List<UserBlock> findByBlocker(User blocker);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM UserBlock b WHERE (b.blocker = :u1 AND b.blocked = :u2) OR (b.blocker = :u2 AND b.blocked = :u1)")
    boolean isBlockedEitherWay(@Param("u1") User u1, @Param("u2") User u2);
}
