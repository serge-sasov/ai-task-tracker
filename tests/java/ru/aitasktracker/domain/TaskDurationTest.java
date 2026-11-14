package ru.aitasktracker.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the duration invariant R-009:
 * {@code duration_s = COALESCE(finished_at, last_step_at) − started_at}.
 */
class TaskDurationTest {

    @Test
    void secondsWhenFinishedPresent() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 10, 9, 0, 0);
        LocalDateTime finish = LocalDateTime.of(2026, 11, 10, 11, 30, 0);
        // 2h30m = 9000 s (UC-E2E-003 fixture).
        assertEquals(9000L, TaskDuration.seconds(start, finish, finish));
    }

    @Test
    void secondsFallsBackToLastStepWhenFinishedAbsent() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 11, 11, 0, 0);
        LocalDateTime lastStep = LocalDateTime.of(2026, 11, 11, 12, 10, 0);
        // 1h10m = 4200 s.
        assertEquals(4200L, TaskDuration.seconds(start, null, lastStep));
    }

    @Test
    void secondsPrefersFinishedOverLastStep() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 11, 9, 0, 0);
        LocalDateTime finish = LocalDateTime.of(2026, 11, 11, 9, 40, 0);
        LocalDateTime lastStep = LocalDateTime.of(2026, 11, 11, 10, 0, 0);
        // Finished wins: 40m = 2400 s (UC-E2E-003 fixture).
        assertEquals(2400L, TaskDuration.seconds(start, finish, lastStep));
    }

    @Test
    void secondsZeroWhenIdentical() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 10, 9, 0, 0);
        assertEquals(0L, TaskDuration.seconds(start, start, start));
    }

    @Test
    void secondsThrowsWhenStartAbsent() {
        assertThrows(IllegalArgumentException.class,
                () -> TaskDuration.seconds(null,
                        LocalDateTime.of(2026, 11, 10, 11, 30),
                        LocalDateTime.of(2026, 11, 10, 11, 30)));
    }

    @Test
    void secondsThrowsWhenEndUnknown() {
        assertThrows(IllegalArgumentException.class,
                () -> TaskDuration.seconds(
                        LocalDateTime.of(2026, 11, 10, 9, 0, 0), null, null));
    }

    @Test
    void secondsThrowsWhenEndBeforeStart() {
        assertThrows(IllegalArgumentException.class,
                () -> TaskDuration.seconds(
                        LocalDateTime.of(2026, 11, 10, 12, 0, 0),
                        LocalDateTime.of(2026, 11, 10, 10, 0, 0), null));
    }
}
