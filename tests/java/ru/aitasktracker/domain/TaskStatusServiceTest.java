package ru.aitasktracker.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for status derivation and duration invariants (contracts §2).
 * Pure JUnit, no database (pom has no H2/Testcontainers).
 */
class TaskStatusServiceTest {

    private final TaskStatusService service = new TaskStatusService();

    @Test
    void derivesCompletedFromExplicitSource() {
        TaskStatus status = service.deriveStatus("Завершена", null);
        assertEquals(TaskStatus.COMPLETED, status);
    }

    @Test
    void derivesInterruptedFromExplicitSource() {
        TaskStatus status = service.deriveStatus("Прервана", null);
        assertEquals(TaskStatus.INTERRUPTED, status);
    }

    @Test
    void derivesNotCompletedFromExplicitSource() {
        TaskStatus status = service.deriveStatus("Не завершена", null);
        assertEquals(TaskStatus.NOT_COMPLETED, status);
    }

    @Test
    void prefersExplicitSourceOverEndState() {
        // Source explicitly says COMPLETED even though finishedAt is null — source wins.
        TaskStatus status = service.deriveStatus("Завершена", null);
        assertEquals(TaskStatus.COMPLETED, status);
    }

    @Test
    void derivesCompletedFromNonEmptyFinishedAt() {
        TaskStatus status = service.deriveStatus("", LocalDateTime.of(2026, 11, 10, 11, 30));
        assertEquals(TaskStatus.COMPLETED, status);
    }

    @Test
    void derivesNotCompletedFromNoEndState() {
        TaskStatus status = service.deriveStatus("", null);
        assertEquals(TaskStatus.NOT_COMPLETED, status);
    }

    @Test
    void derivesFromCaseInsensitiveSource() {
        TaskStatus status = service.deriveStatus("завершена", null);
        assertEquals(TaskStatus.COMPLETED, status);
    }

    @Test
    void derivesFromTrimmedSource() {
        TaskStatus status = service.deriveStatus("  Прервана  ", null);
        assertEquals(TaskStatus.INTERRUPTED, status);
    }

    @Test
    void isConsistentWhenCompletedAndFinishedPresent() {
        assertTrue(service.isConsistent(TaskStatus.COMPLETED,
                LocalDateTime.of(2026, 11, 10, 11, 30)));
    }

    @Test
    void isConsistentWhenNotCompletedAndFinishedAbsent() {
        assertTrue(service.isConsistent(TaskStatus.NOT_COMPLETED, null));
    }

    @Test
    void isConsistentWhenInterruptedAndFinishedAbsent() {
        assertTrue(service.isConsistent(TaskStatus.INTERRUPTED, null));
    }

    @Test
    void isNotConsistentWhenCompletedButFinishedAbsent() {
        assertFalse(service.isConsistent(TaskStatus.COMPLETED, null));
    }

    @Test
    void validateConsistencyThrowsWhenCompletedButFinishedAbsent() {
        assertThrows(IllegalArgumentException.class,
                () -> service.validateConsistency(TaskStatus.COMPLETED, null));
    }

    @Test
    void validateConsistencyPassesWhenConsistent() {
        service.validateConsistency(TaskStatus.NOT_COMPLETED, null);
        service.validateConsistency(TaskStatus.COMPLETED,
                LocalDateTime.of(2026, 11, 10, 11, 30));
    }

    @Test
    void fromValueCaseInsensitiveMatch() {
        Optional<TaskStatus> status = TaskStatus.fromValue("завершена");
        assertTrue(status.isPresent());
        assertEquals(TaskStatus.COMPLETED, status.get());
    }

    @Test
    void fromValueEmptyOrNullEmpty() {
        assertTrue(TaskStatus.fromValue("").isEmpty());
        assertTrue(TaskStatus.fromValue("  ").isEmpty());
        assertTrue(TaskStatus.fromValue(null).isEmpty());
    }

    @Test
    void fromValueUnmatchedEmpty() {
        assertTrue(TaskStatus.fromValue("some other value").isEmpty());
    }

    @Test
    void allCanonicalValuesExposeRussianStrings() {
        assertEquals("Завершена", TaskStatus.COMPLETED.value());
        assertEquals("Прервана", TaskStatus.INTERRUPTED.value());
        assertEquals("Не завершена", TaskStatus.NOT_COMPLETED.value());
    }

    @Test
    void fromValueRoundTripsAllValues() {
        for (TaskStatus status : TaskStatus.values()) {
            Optional<TaskStatus> resolved = TaskStatus.fromValue(status.value());
            assertTrue(resolved.isPresent());
            assertEquals(status, resolved.get());
        }
    }

    @Test
    void valueOfMatchesCanonical() {
        assertEquals("Завершена", TaskStatus.valueOf("COMPLETED").value());
    }
}
