package com.chitralekha.repository;

import com.chitralekha.entity.Conversation;
import com.chitralekha.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("SELECT c FROM Conversation c WHERE (c.userOne = :user1 AND c.userTwo = :user2) OR (c.userOne = :user2 AND c.userTwo = :user1)")
    Optional<Conversation> findBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);

    @Query("SELECT c FROM Conversation c WHERE c.userOne = :user OR c.userTwo = :user ORDER BY c.updatedAt DESC")
    List<Conversation> findAllForUser(@Param("user") User user);
}
