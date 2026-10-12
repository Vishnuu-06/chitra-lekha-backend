package com.chitralekha.repository;

import com.chitralekha.entity.Content;
import com.chitralekha.entity.ContentList;
import com.chitralekha.entity.ContentListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContentListItemRepository extends JpaRepository<ContentListItem, Long> {
    List<ContentListItem> findByListOrderByAddedAtDesc(ContentList list);
    Optional<ContentListItem> findByListAndContent(ContentList list, Content content);
    long countByList(ContentList list);
    void deleteByListAndContent(ContentList list, Content content);
}
