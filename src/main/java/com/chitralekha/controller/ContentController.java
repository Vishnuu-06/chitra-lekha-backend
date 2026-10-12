package com.chitralekha.controller;

import com.chitralekha.dto.ContentSummaryDto;
import com.chitralekha.entity.Content;
import com.chitralekha.entity.Review;
import com.chitralekha.exception.ResourceNotFoundException;
import com.chitralekha.integration.ContentIntegrationService;
import com.chitralekha.repository.ContentRepository;
import com.chitralekha.repository.ReviewRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/content")
public class ContentController {
    private final ContentIntegrationService integrationService;
    private final ContentRepository contentRepository;
    private final ReviewRepository reviewRepository;

    public ContentController(ContentIntegrationService integrationService,
                             ContentRepository contentRepository,
                             ReviewRepository reviewRepository) {
        this.integrationService = integrationService;
        this.contentRepository = contentRepository;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/search")
    public ResponseEntity<List<ContentSummaryDto>> search(
            @RequestParam(name = "query", required = false, defaultValue = "") String query,
            @RequestParam(name = "category", required = false, defaultValue = "ALL") String category) {
        return ResponseEntity.ok(integrationService.searchUnified(query, category));
    }

    @GetMapping("/discover")
    public ResponseEntity<List<ContentSummaryDto>> discover(
            @RequestParam(name = "type", defaultValue = "MOVIE") String type,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "24") int size,
            @RequestParam(name = "genre", required = false) String genre,
            @RequestParam(name = "sortBy", defaultValue = "recent") String sortBy) {
        return ResponseEntity.ok(integrationService.getCategoryContent(type, page, size, genre, sortBy));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContentSummaryDto> getDetails(@PathVariable Long id) {
        ContentSummaryDto dto = integrationService.getContentById(id);
        if (dto == null) {
            throw new ResourceNotFoundException("Content not found with id: " + id);
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<Map<String, Object>>> getContentReviews(@PathVariable Long id) {
        Content content = contentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found with id: " + id));

        List<Review> reviews = reviewRepository.findByContentOrderByCreatedAtDesc(content);
        List<Map<String, Object>> result = reviews.stream().map(r -> Map.<String, Object>of(
                "id", r.getId(),
                "title", r.getTitle() != null ? r.getTitle() : "",
                "body", r.getBody(),
                "ratingScore", r.getRatingScore() != null ? r.getRatingScore() : 0,
                "containsSpoilers", r.getContainsSpoilers(),
                "createdAt", r.getCreatedAt().toString(),
                "author", Map.of(
                        "id", r.getUser().getId(),
                        "username", r.getUser().getUsername()
                )
        )).toList();

        return ResponseEntity.ok(result);
    }
}
