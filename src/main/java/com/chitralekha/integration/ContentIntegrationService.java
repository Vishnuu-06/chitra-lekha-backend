package com.chitralekha.integration;

import com.chitralekha.dto.ContentSummaryDto;
import com.chitralekha.entity.Content;
import com.chitralekha.repository.ContentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ContentIntegrationService {
    private final ContentRepository contentRepository;
    private final ContentNormalizationService normalizationService;
    private final TmdbContentService tmdbContentService;
    private final AniListContentService aniListContentService;
    private final GoogleBooksService googleBooksService;
    private final OpenLibraryService openLibraryService;

    public ContentIntegrationService(
            ContentRepository contentRepository,
            ContentNormalizationService normalizationService,
            TmdbContentService tmdbContentService,
            AniListContentService aniListContentService,
            GoogleBooksService googleBooksService,
            OpenLibraryService openLibraryService) {
        this.contentRepository = contentRepository;
        this.normalizationService = normalizationService;
        this.tmdbContentService = tmdbContentService;
        this.aniListContentService = aniListContentService;
        this.googleBooksService = googleBooksService;
        this.openLibraryService = openLibraryService;
    }

    public List<ContentSummaryDto> getCategoryContent(String contentType, int page, int size, String genre, String sortBy) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if ("year".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "releaseYear");
        } else if ("title".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.ASC, "title");
        }

        PageRequest pageRequest = PageRequest.of(Math.max(0, page), Math.min(size, 50), sort);
        Page<Content> contentPage;

        if (genre != null && !genre.trim().isEmpty()) {
            contentPage = contentRepository.findByContentTypeAndGenresContainingIgnoreCase(
                    contentType.toUpperCase(), genre.trim(), pageRequest
            );
        } else {
            contentPage = contentRepository.findByContentType(contentType.toUpperCase(), pageRequest);
        }

        return contentPage.getContent().stream()
                .map(normalizationService::toDto)
                .toList();
    }

    public List<ContentSummaryDto> searchUnified(String query, String categoryFilter) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        String trimmedQuery = query.trim();
        Map<String, ContentSummaryDto> resultMap = new LinkedHashMap<>();

        // 1. Search existing internal catalog
        PageRequest internalPage = PageRequest.of(0, 20);
        Page<Content> internalMatches;
        if (categoryFilter != null && !categoryFilter.equalsIgnoreCase("ALL")) {
            internalMatches = contentRepository.findByContentTypeAndTitleContainingIgnoreCase(
                    categoryFilter.toUpperCase(), trimmedQuery, internalPage
            );
        } else {
            internalMatches = contentRepository.searchByKeyword(trimmedQuery, internalPage);
        }

        for (Content c : internalMatches.getContent()) {
            resultMap.put(c.getProvider() + ":" + c.getExternalId(), normalizationService.toDto(c));
        }

        // 2. Query external metadata providers where applicable
        if (categoryFilter == null || "ALL".equalsIgnoreCase(categoryFilter) || "MOVIE".equalsIgnoreCase(categoryFilter)) {
            List<ContentSummaryDto> tmdbMovies = tmdbContentService.searchMovies(trimmedQuery);
            for (ContentSummaryDto dto : tmdbMovies) {
                resultMap.putIfAbsent(dto.getProvider() + ":" + dto.getExternalId(), dto);
            }
        }

        if (categoryFilter == null || "ALL".equalsIgnoreCase(categoryFilter) || "SHOW".equalsIgnoreCase(categoryFilter)) {
            List<ContentSummaryDto> tmdbShows = tmdbContentService.searchTvShows(trimmedQuery);
            for (ContentSummaryDto dto : tmdbShows) {
                resultMap.putIfAbsent(dto.getProvider() + ":" + dto.getExternalId(), dto);
            }
        }

        if (categoryFilter == null || "ALL".equalsIgnoreCase(categoryFilter) || "ANIME".equalsIgnoreCase(categoryFilter)) {
            List<ContentSummaryDto> anilistAnime = aniListContentService.searchAnime(trimmedQuery);
            for (ContentSummaryDto dto : anilistAnime) {
                resultMap.putIfAbsent(dto.getProvider() + ":" + dto.getExternalId(), dto);
            }
        }

        if (categoryFilter == null || "ALL".equalsIgnoreCase(categoryFilter) || "BOOK".equalsIgnoreCase(categoryFilter)) {
            List<ContentSummaryDto> googleBooks = googleBooksService.searchBooks(trimmedQuery);
            for (ContentSummaryDto dto : googleBooks) {
                resultMap.putIfAbsent(dto.getProvider() + ":" + dto.getExternalId(), dto);
            }
            if (googleBooks.isEmpty()) {
                List<ContentSummaryDto> openBooks = openLibraryService.searchBooks(trimmedQuery);
                for (ContentSummaryDto dto : openBooks) {
                    resultMap.putIfAbsent(dto.getProvider() + ":" + dto.getExternalId(), dto);
                }
            }
        }

        return new ArrayList<>(resultMap.values());
    }

    public ContentSummaryDto getContentById(Long id) {
        Content content = contentRepository.findById(id).orElse(null);
        return content != null ? normalizationService.toDto(content) : null;
    }
}
