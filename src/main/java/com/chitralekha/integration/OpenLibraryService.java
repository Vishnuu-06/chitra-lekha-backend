package com.chitralekha.integration;

import com.chitralekha.dto.ContentSummaryDto;
import com.chitralekha.entity.Content;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class OpenLibraryService {
    private static final Logger log = LoggerFactory.getLogger(OpenLibraryService.class);
    private static final String OPEN_LIBRARY_URL = "https://openlibrary.org";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final ContentNormalizationService normalizationService;

    public OpenLibraryService(ContentNormalizationService normalizationService, ObjectMapper objectMapper) {
        this.restClient = RestClient.builder().baseUrl(OPEN_LIBRARY_URL).build();
        this.normalizationService = normalizationService;
        this.objectMapper = objectMapper;
    }

    public List<ContentSummaryDto> searchBooks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        try {
            ResponseEntity<String> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search.json")
                            .queryParam("q", query.trim())
                            .queryParam("limit", 10)
                            .build())
                    .retrieve()
                    .toEntity(String.class);

            return parseOpenLibraryResults(response.getBody());
        } catch (Exception e) {
            log.warn("Open Library search failed: {}", e.getMessage());
            return List.of();
        }
    }

    private List<ContentSummaryDto> parseOpenLibraryResults(String json) {
        List<ContentSummaryDto> results = new ArrayList<>();
        if (json == null) return results;

        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode docs = root.path("docs");
            if (docs.isArray()) {
                for (JsonNode doc : docs) {
                    String key = doc.path("key").asText("");
                    if (key.isEmpty()) continue;
                    String externalId = key.replace("/works/", "");

                    String title = doc.path("title").asText("Untitled");
                    Integer year = doc.path("first_publish_year").isNumber() ?
                            doc.path("first_publish_year").asInt() : null;

                    List<String> authors = new ArrayList<>();
                    JsonNode authorNodes = doc.path("author_name");
                    if (authorNodes.isArray()) {
                        for (JsonNode a : authorNodes) {
                            authors.add(a.asText());
                        }
                    }
                    String creators = String.join(", ", authors);

                    String posterUrl = null;
                    if (doc.path("cover_i").isNumber()) {
                        long coverId = doc.path("cover_i").asLong();
                        posterUrl = "https://covers.openlibrary.org/b/id/" + coverId + "-L.jpg";
                    }

                    Content content = normalizationService.findOrCreate(
                            "BOOK", "OPEN_LIBRARY", externalId, title, "",
                            posterUrl, null, year, "", creators, null
                    );
                    results.add(normalizationService.toDto(content));
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Open Library JSON: {}", e.getMessage());
        }
        return results;
    }
}
