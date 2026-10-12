package com.chitralekha.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_content_id")
    private Content sharedContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_review_id")
    private Review sharedReview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_list_id")
    private ContentList sharedList;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isRead == null) this.isRead = false;
    }

    public Message() {}

    public Message(Conversation conversation, User sender, User receiver, String body) {
        this.conversation = conversation;
        this.sender = sender;
        this.receiver = receiver;
        this.body = body;
        this.isRead = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Conversation getConversation() { return conversation; }
    public void setConversation(Conversation conversation) { this.conversation = conversation; }
    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }
    public User getReceiver() { return receiver; }
    public void setReceiver(User receiver) { this.receiver = receiver; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    public Content getSharedContent() { return sharedContent; }
    public void setSharedContent(Content sharedContent) { this.sharedContent = sharedContent; }
    public Review getSharedReview() { return sharedReview; }
    public void setSharedReview(Review sharedReview) { this.sharedReview = sharedReview; }
    public ContentList getSharedList() { return sharedList; }
    public void setSharedList(ContentList sharedList) { this.sharedList = sharedList; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
