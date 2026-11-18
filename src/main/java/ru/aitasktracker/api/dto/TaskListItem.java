package ru.aitasktracker.api.dto;

import java.util.Collections;
import java.util.List;

/**
 * A single row of the task list response (contracts §4.2). {@code tokens_total}
 * is the sum of input+output+cache across all token_usage rows of the task, or
 * 0 when there are none. {@code models} is the ordered list of models used by
 * the task, derived from the same token_usage rows as {@code tokens_total}:
 * ordered by total tokens (input+output+cache) descending, then model name
 * ascending for ties. It is empty when the task has no usage.
 */
public class TaskListItem {

    private final String taskId;
    private final String name;
    private final String startedAt;
    private final String finishedAt;
    private final String status;
    private final long durationS;
    private final long tokensTotal;
    private final List<String> models;

    public TaskListItem(String taskId, String name, String startedAt,
                        String finishedAt, String status,
                        long durationS, long tokensTotal) {
        this(taskId, name, startedAt, finishedAt, status, durationS, tokensTotal,
                Collections.emptyList());
    }

    public TaskListItem(String taskId, String name, String startedAt,
                        String finishedAt, String status,
                        long durationS, long tokensTotal, List<String> models) {
        this.taskId = taskId;
        this.name = name;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.status = status;
        this.durationS = durationS;
        this.tokensTotal = tokensTotal;
        this.models = Collections.unmodifiableList(
                models == null ? List.of() : List.copyOf(models));
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

    /**
     * Ordered model list: total tokens (input+output+cache) descending, then
     * model name ascending for ties. Immutable; empty when the task has no
     * usage. Never {@code null}.
     */
    public List<String> getModels() {
        return models;
    }
}
