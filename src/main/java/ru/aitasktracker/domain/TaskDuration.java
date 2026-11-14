package ru.aitasktracker.domain;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Duration invariant R-009:
 * {@code duration_s = COALESCE(finished_at, last_step_at) − started_at}, computed in seconds.
 *
 * <p>Duration is measured from timestamps, never from summed step durations (contracts §2,
 * data-model note, Q-002).
 */
public final class TaskDuration {

    private TaskDuration() {
    }

    /**
     * @param startedAt  session start (must be non-null)
     * @param finishedAt session end, or {@code null} to fall back to the last step time
     * @param lastStepAt last activity time in the session (used when {@code finishedAt} is null)
     * @return whole seconds of the effective span
     * @throws IllegalArgumentException when the effective end time is unknown or precedes start
     */
    public static long seconds(LocalDateTime startedAt, LocalDateTime finishedAt, LocalDateTime lastStepAt) {
        if (startedAt == null) {
            throw new IllegalArgumentException("started_at must be present");
        }
        LocalDateTime end = (finishedAt != null) ? finishedAt : lastStepAt;
        if (end == null) {
            throw new IllegalArgumentException("neither finished_at nor last_step_at is present");
        }
        long seconds = ChronoUnit.SECONDS.between(startedAt, end);
        if (seconds < 0) {
            throw new IllegalArgumentException(
                    "end time precedes start time: started_at=" + startedAt + " end=" + end);
        }
        return seconds;
    }
}
