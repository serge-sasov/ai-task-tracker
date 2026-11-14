package ru.aitasktracker.api.dto;

/**
 * A single row of the task list response (contracts §4.2). {@code tokens_total}
 * is the sum of input+output+cache across all token_usage rows of the task, or
 * 0 when there are none.
 */
public class TaskListItem {

    private final String taskId;
    private final String name;
    private final String startedAt;
    private final String finishedAt;
    private final String status;
    private final long durationS;
    private final long tokensTotal;

    public TaskListItem(String taskId, String name, String startedAt,
                        String finishedAt, String status,
                        long durationS, long tokensTotal) {
        this.taskId = taskId;
        this.name = name;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.status = status;
        this.durationS = durationS;
        this.tokensTotal = tokensTotal;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getName() {
        return name;
    }

    public String getStartedAt() {
        return startedAt;
    }

    public String getFinishedAt() {
        return finishedAt;
    }

    public String getStatus() {
        return status;
    }

    public long getDurationS() {
        return durationS;
    }

    public long getTokensTotal() {
        return tokensTotal;
    }
}
