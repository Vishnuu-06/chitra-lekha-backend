package com.chitralekha.integration;

import com.chitralekha.dto.ContentSummaryDto;
import com.chitralekha.entity.Content;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AniListContentService {
    private static final Logger log = LoggerFactory.getLogger(AniListContentService.class);
    private static final String ANILIST_URL = "https://graphql.anilist.co";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final ContentNormalizationService normalizationService;

    public AniListContentService(ContentNormalizationService normalizationService, ObjectMapper objectMapper) {
        this.restClient = RestClient.builder().baseUrl(ANILIST_URL).build();
        this.normalizationService = normalizationService;
        this.objectMapper = objectMapper;
    }

    public List<ContentSummaryDto> searchAnime(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        String graphQLQuery = """
            query ($search: String) {
              Page(page: 1, perPage: 10) {
                media(search: $search, type: ANIME, sort: SEARCH_MATCH) {
                  id
                  title {
                    romaji
                    english
                  }
                  description(asHtml: false)
                  coverImage {
                    extraLarge
                    large
                  }
                  bannerImage
                  startDate {
                    year
                  }
                  genres
                  episodes
                  studios(isMain: true) {
                    nodes {
                      name
                    }
                  }
                }
              }
            }
            """;

        Map<String, Object> requestBody = Map.of(
                "query", graphQLQuery,
                "variables", Map.of("search", query.trim())
        );

        try {
            ResponseEntity<String> response = restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .toEntity(String.class);

            return parseAniListResults(response.getBody());
        } catch (Exception e) {
            log.warn("AniList search failed: {}", e.getMessage());
            return List.of();
        }
    }

    private List<ContentSummaryDto> parseAniListResults(String json) {
        List<ContentSummaryDto> results = new ArrayList<>();
        if (json == null) return results;

        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode mediaArray = root.path("data").path("Page").path("media");
            if (mediaArray.isArray()) {
                for (JsonNode media : mediaArray) {
                    String externalId = media.path("id").asText();
                    String englishTitle = media.path("title").path("english").asText("");
                    String romajiTitle = media.path("title").path("romaji").asText("");
                    String title = !englishTitle.isEmpty() ? englishTitle : romajiTitle;

                    String description = media.path("description").asText("");
                    // Strip any lingering HTML tags if present
                    description = description.replaceAll("<[^>]*>", "").trim();

                    String posterUrl = media.path("coverImage").path("extraLarge").asText("");
                    if (posterUrl.isEmpty()) {
                        posterUrl = media.path("coverImage").path("large").asText(null);
                    }
                    String backdropUrl = media.path("bannerImage").asText(null);

                    Integer year = media.path("startDate").path("year").isNumber() ?
                            media.path("startDate").path("year").asInt() : null;

                    List<String> genreList = new ArrayList<>();
                    JsonNode genresNode = media.path("genres");
                    if (genresNode.isArray()) {
                        for (JsonNode g : genresNode) {
                            genreList.add(g.asText());
                        }
                    }
                    String genres = String.join(", ", genreList);

                    List<String> studioList = new ArrayList<>();
                    JsonNode studioNodes = media.path("studios").path("nodes");
                    if (studioNodes.isArray()) {
                        for (JsonNode s : studioNodes) {
                            studioList.add(s.path("name").asText());
                        }
                    }
                    String creators = String.join(", ", studioList);
                    Integer episodes = media.path("episodes").isNumber() ? media.path("episodes").asInt() : null;

                    Content content = normalizationService.findOrCreate(
                            "ANIME", "ANILIST", externalId, title, description,
                            posterUrl, backdropUrl, year, genres, creators, episodes
                    );
                    results.add(normalizationService.toDto(content));
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse AniList JSON: {}", e.getMessage());
        }
        return results;
    }
}
