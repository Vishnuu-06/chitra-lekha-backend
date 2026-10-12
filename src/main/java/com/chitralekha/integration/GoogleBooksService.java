package com.chitralekha.integration;

import com.chitralekha.dto.ContentSummaryDto;
import com.chitralekha.entity.Content;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleBooksService {
    private static final Logger log = LoggerFactory.getLogger(GoogleBooksService.class);
    private static final String GOOGLE_BOOKS_URL = "https://www.googleapis.com/books/v1";

    private final String apiKey;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final ContentNormalizationService normalizationService;

    public GoogleBooksService(
            @Value("${app.google-books.api-key:}") String apiKey,
            ContentNormalizationService normalizationService,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.restClient = RestClient.builder().baseUrl(GOOGLE_BOOKS_URL).build();
        this.normalizationService = normalizationService;
        this.objectMapper = objectMapper;
    }

    public List<ContentSummaryDto> searchBooks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        try {
            ResponseEntity<String> response = restClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder.path("/volumes")
                                .queryParam("q", query.trim())
                                .queryParam("maxResults", 10)
                                .queryParam("printType", "books");
                        if (!apiKey.isEmpty()) {
                            builder.queryParam("key", apiKey);
                        }
                        return builder.build();
                    })
                    .retrieve()
                    .toEntity(String.class);

            return parseGoogleBooksResults(response.getBody());
        } catch (Exception e) {
            log.warn("Google Books search failed: {}", e.getMessage());
            return List.of();
        }
    }

    private List<ContentSummaryDto> parseGoogleBooksResults(String json) {
        List<ContentSummaryDto> results = new ArrayList<>();
        if (json == null) return results;

        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode items = root.path("items");
            if (items.isArray()) {
                for (JsonNode item : items) {
                    String externalId = item.path("id").asText();
                    JsonNode volumeInfo = item.path("volumeInfo");

                    String title = volumeInfo.path("title").asText("Untitled");
                    String description = volumeInfo.path("description").asText("");

                    // Secure image thumbnail
                    String posterUrl = volumeInfo.path("imageLinks").path("thumbnail").asText("");
                    if (posterUrl.startsWith("http://")) {
                        posterUrl = "https://" + posterUrl.substring(7);
                    }
                    if (posterUrl.isEmpty()) {
                        posterUrl = null;
                    }

                    String publishedDate = volumeInfo.path("publishedDate").asText("");
                    Integer year = null;
                    if (publishedDate.length() >= 4) {
                        try {
                            year = Integer.parseInt(publishedDate.substring(0, 4));
                        } catch (NumberFormatException ignored) {}
                    }

                    List<String> authors = new ArrayList<>();
                    JsonNode authorsNode = volumeInfo.path("authors");
                    if (authorsNode.isArray()) {
                        for (JsonNode a : authorsNode) {
                            authors.add(a.asText());
                        }
                    }
                    String creators = String.join(", ", authors);

                    List<String> categories = new ArrayList<>();
                    JsonNode categoriesNode = volumeInfo.path("categories");
                    if (categoriesNode.isArray()) {
                        for (JsonNode c : categoriesNode) {
                            categories.add(c.asText());
                        }
                    }
                    String genres = String.join(", ", categories);

                    Integer pageCount = volumeInfo.path("pageCount").isNumber() ?
                            volumeInfo.path("pageCount").asInt() : null;

                    Content content = normalizationService.findOrCreate(
                            "BOOK", "GOOGLE_BOOKS", externalId, title, description,
                            posterUrl, null, year, genres, creators, pageCount
                    );
                    results.add(normalizationService.toDto(content));
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Google Books JSON: {}", e.getMessage());
        }
        return results;
    }
}
