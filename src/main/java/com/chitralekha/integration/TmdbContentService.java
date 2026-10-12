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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TmdbContentService {
    private static final Logger log = LoggerFactory.getLogger(TmdbContentService.class);
    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3";
    private static final String TMDB_IMAGE_BASE = "https://image.tmdb.org/t/p/w500";
    private static final String TMDB_BACKDROP_BASE = "https://image.tmdb.org/t/p/original";

    private final String apiKey;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final ContentNormalizationService normalizationService;

    public TmdbContentService(
            @Value("${app.tmdb.api-key:}") String apiKey,
            ContentNormalizationService normalizationService,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.restClient = RestClient.builder().baseUrl(TMDB_BASE_URL).build();
        this.normalizationService = normalizationService;
        this.objectMapper = objectMapper;
    }

    public boolean isConfigured() {
        return !apiKey.isEmpty();
    }

    public List<ContentSummaryDto> searchMovies(String query) {
        if (!isConfigured() || query == null || query.trim().isEmpty()) {
            return List.of();
        }
        try {
            ResponseEntity<String> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search/movie")
                            .queryParam("api_key", apiKey)
                            .queryParam("query", query.trim())
                            .queryParam("include_adult", false)
                            .build())
                    .retrieve()
                    .toEntity(String.class);

            return parseTmdbResults(response.getBody(), "MOVIE");
        } catch (Exception e) {
            log.warn("TMDB movie search failed: {}", e.getMessage());
            return List.of();
        }
    }

    public List<ContentSummaryDto> searchTvShows(String query) {
        if (!isConfigured() || query == null || query.trim().isEmpty()) {
            return List.of();
        }
        try {
            ResponseEntity<String> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search/tv")
                            .queryParam("api_key", apiKey)
                            .queryParam("query", query.trim())
                            .queryParam("include_adult", false)
                            .build())
                    .retrieve()
                    .toEntity(String.class);

            return parseTmdbResults(response.getBody(), "SHOW");
        } catch (Exception e) {
            log.warn("TMDB TV search failed: {}", e.getMessage());
            return List.of();
        }
    }

    private List<ContentSummaryDto> parseTmdbResults(String json, String contentType) {
        List<ContentSummaryDto> results = new ArrayList<>();
        if (json == null) return results;

        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode items = root.path("results");
            if (items.isArray()) {
                for (JsonNode item : items) {
                    String externalId = item.path("id").asText();
                    String title = "MOVIE".equals(contentType) ? item.path("title").asText() : item.path("name").asText();
                    String overview = item.path("overview").asText("");
                    String posterPath = item.path("poster_path").asText("");
                    String backdropPath = item.path("backdrop_path").asText("");
                    String releaseDate = "MOVIE".equals(contentType) ? item.path("release_date").asText("") : item.path("first_air_date").asText("");

                    Integer year = null;
                    if (releaseDate.length() >= 4) {
                        try {
                            year = Integer.parseInt(releaseDate.substring(0, 4));
                        } catch (NumberFormatException ignored) {}
                    }

                    String posterUrl = !posterPath.isEmpty() ? TMDB_IMAGE_BASE + posterPath : null;
                    String backdropUrl = !backdropPath.isEmpty() ? TMDB_BACKDROP_BASE + backdropPath : null;

                    Content content = normalizationService.findOrCreate(
                            contentType, "TMDB", externalId, title, overview,
                            posterUrl, backdropUrl, year, null, null, null
                    );
                    results.add(normalizationService.toDto(content));
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse TMDB JSON: {}", e.getMessage());
        }
        return results;
    }
}
