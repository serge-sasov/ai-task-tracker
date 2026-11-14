package ru.aitasktracker.api.dto;

/**
 * Request body for {@code POST /api/search} (contracts §4.4, API-003).
 * {@code query} is mandatory and non-empty; {@code limit} and
 * {@code task_id} are optional.
 */
public class SearchRequest {

    private final String query;
    private final Integer limit;
    private final String taskId;

    public SearchRequest(String query, Integer limit, String taskId) {
        this.query = query;
        this.limit = limit;
        this.taskId = taskId;
    }

    public String getQuery() {
        return query;
    }

    public Integer getLimit() {
        return limit;
    }

    public String getTaskId() {
        return taskId;
    }
}
