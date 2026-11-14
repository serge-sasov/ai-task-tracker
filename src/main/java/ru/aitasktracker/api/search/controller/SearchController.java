package ru.aitasktracker.api.search.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.aitasktracker.api.dto.SearchRequest;
import ru.aitasktracker.api.dto.SearchResponse;
import ru.aitasktracker.api.search.service.SearchService;

/**
 * Semantic search endpoint {@code POST /api/search} (contracts §4.4, API-003).
 *
 * <p>The request body is {@code {"query": "...", "limit": 5, "task_id": "TASK-001"}}.
 * The {@code query} is mandatory (empty/blank -> 400 {@code INVALID_QUERY}).
 * {@code limit} caps the number of returned hits (clamped to the configured
 * top-K, default 5); {@code task_id} restricts search to a single task. The
 * service delegates to {@link SearchService}; Ollama degradation is turned into
 * 503 {@code AI_SEARCH_UNAVAILABLE} by the shared envelope handler.
 */
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping
    public SearchResponse search(@RequestBody SearchRequest body) {
        return searchService.search(
                body.getQuery(),
                body.getLimit(),
                body.getTaskId());
    }
}
