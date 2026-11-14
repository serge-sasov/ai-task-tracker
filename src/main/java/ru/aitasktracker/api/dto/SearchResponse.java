package ru.aitasktracker.api.dto;

import java.util.List;

/**
 * Response body for {@code POST /api/search} (contracts §4.4, API-003
 * {@code SearchResponse}). {@code total} is the number of returned hits;
 * {@code results} is ordered by {@code score} desc and never fabricated.
 */
public class SearchResponse {

    private final String query;
    private final int total;
    private final List<SearchHit> results;

    public SearchResponse(String query, int total, List<SearchHit> results) {
        this.query = query;
        this.total = total;
        this.results = results;
    }

    public String getQuery() {
        return query;
    }

    public int getTotal() {
        return total;
    }

    public List<SearchHit> getResults() {
        return results;
    }
}
