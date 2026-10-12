package com.chitralekha.repository;

import com.chitralekha.entity.ContentList;
import com.chitralekha.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentListRepository extends JpaRepository<ContentList, Long> {
    List<ContentList> findByOwnerOrderByCreatedAtDesc(User owner);
    List<ContentList> findByVisibilityOrderByCreatedAtDesc(String visibility);
    List<ContentList> findByOwnerAndVisibility(User owner, String visibility);
    long countByOwner(User owner);
}
