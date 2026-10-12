package com.chitralekha.repository;

import com.chitralekha.entity.Conversation;
import com.chitralekha.entity.Message;
import com.chitralekha.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationOrderByCreatedAtAsc(Conversation conversation);
    long countByReceiverAndIsReadFalse(User receiver);

    @Query("SELECT m FROM Message m WHERE m.conversation = :conversation ORDER BY m.createdAt DESC LIMIT 1")
    Message findLatestMessage(@Param("conversation") Conversation conversation);
}
