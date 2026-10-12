package com.chitralekha.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "list_items", uniqueConstraints = @UniqueConstraint(columnNames = {"list_id", "content_id"}))
public class ContentListItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "list_id", nullable = false)
    private ContentList list;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @Column(name = "item_order", nullable = false)
    private Integer itemOrder = 0;

    @Column(length = 255)
    private String notes;

    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        this.addedAt = LocalDateTime.now();
        if (this.itemOrder == null) this.itemOrder = 0;
    }

    public ContentListItem() {
    }

    public ContentListItem(ContentList list, Content content) {
        this.list = list;
        this.content = content;
        this.itemOrder = 0;
    }

    public ContentListItem(ContentList list, Content content, Integer itemOrder, String notes) {
        this.list = list;
        this.content = content;
        this.itemOrder = itemOrder != null ? itemOrder : 0;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ContentList getList() { return list; }
    public void setList(ContentList list) { this.list = list; }
    public Content getContent() { return content; }
    public void setContent(Content content) { this.content = content; }
    public Integer getItemOrder() { return itemOrder; }
    public void setItemOrder(Integer itemOrder) { this.itemOrder = itemOrder; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
}
