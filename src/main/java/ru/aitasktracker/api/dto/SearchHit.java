package ru.aitasktracker.api.dto;

/**
 * One semantic-search hit (contracts §4.4, API-003 {@code SearchHit}). A ranked
 * task step or solution with a relevance {@code score} (0..1, higher is better).
 */
public class SearchHit {

    private final String taskId;
    private final String kind;
    private final String text;
    private final float score;

    public SearchHit(String taskId, String kind, String text, float score) {
        this.taskId = taskId;
        this.kind = kind;
        this.text = text;
        this.score = score;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getKind() {
        return kind;
    }

    public String getText() {
        return text;
    }

    public float getScore() {
        return score;
    }
}
