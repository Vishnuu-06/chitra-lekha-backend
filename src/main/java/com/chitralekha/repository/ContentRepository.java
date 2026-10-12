package com.chitralekha.repository;

import com.chitralekha.entity.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
    Optional<Content> findByProviderAndExternalId(String provider, String externalId);
    Page<Content> findByContentType(String contentType, Pageable pageable);
    Page<Content> findByTitleContainingIgnoreCase(String query, Pageable pageable);
    Page<Content> findByContentTypeAndTitleContainingIgnoreCase(String contentType, String query, Pageable pageable);
    Page<Content> findByContentTypeAndGenresContainingIgnoreCase(String contentType, String genre, Pageable pageable);

    @Query("SELECT c FROM Content c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.creators) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Content> searchByKeyword(@Param("query") String query, Pageable pageable);

    @Query("SELECT c FROM Content c WHERE c.contentType = :type ORDER BY c.createdAt DESC")
    List<Content> findLatestByType(@Param("type") String type, Pageable pageable);
}
