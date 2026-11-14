package ru.aitasktracker.api.calendar.dto;

/**
 * One task rendered on a single calendar day (contracts §4.5, API-002
 * CalendarTask). A task that spans two days appears as a cell on each of those
 * days, keyed by the same {@code task_id} so the SPA can join them into a bar.
 */
public class CalendarTask {

    private final String taskId;
    private final String startedAt;
    private final String finishedAt;
    private final long durationS;

    public CalendarTask(String taskId, String startedAt, String finishedAt, long durationS) {
        this.taskId = taskId;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.durationS = durationS;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getStartedAt() {
        return startedAt;
    }

    public String getFinishedAt() {
        return finishedAt;
    }

    public long getDurationS() {
        return durationS;
    }
}
