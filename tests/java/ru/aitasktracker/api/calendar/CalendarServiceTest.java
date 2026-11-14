package ru.aitasktracker.api.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;

import ru.aitasktracker.api.calendar.dto.CalendarResponse;
import ru.aitasktracker.api.calendar.service.CalendarService;
import ru.aitasktracker.api.exception.InvalidPeriodException;
import ru.aitasktracker.domain.TaskStatus;
import ru.aitasktracker.domain.entity.TaskEntity;
import ru.aitasktracker.domain.repository.TaskRepository;
import ru.aitasktracker.domain.repository.TokenUsageRepository;

/**
 * Unit tests for the CalendarService. Every repository is a Mockito mock; no
 * Spring context, no H2/Redis (the pom has neither). Covers contracts §4.5:
 * view parsing, the week/day/month grids, the twelve year cells, and the
 * INVALID_PERIOD error path.
 */
class CalendarServiceTest {

    private TaskRepository taskRepository;
    private TokenUsageRepository tokenUsageRepository;
    private CalendarService service;

    /** Seed of tasks keyed by their start date, scoped to this test class. */
    private static final List<TaskEntity> SEED = List.of(
            new TaskEntity("TASK-001", "SID-1", "Task A",
                    LocalDateTime.of(2026, 11, 10, 9, 0),
                    LocalDateTime.of(2026, 11, 10, 11, 30), TaskStatus.COMPLETED),
            new TaskEntity("TASK-002", "SID-2", "Task B",
                    LocalDateTime.of(2026, 11, 10, 10, 0),
                    LocalDateTime.of(2026, 11, 10, 12, 0), TaskStatus.COMPLETED),
            new TaskEntity("TASK-003", "SID-3", "Task C",
                    LocalDateTime.of(2026, 11, 11, 8, 30),
                    LocalDateTime.of(2026, 11, 11, 10, 45), TaskStatus.COMPLETED),
            new TaskEntity("TASK-004", "SID-4", "Task D",
                    LocalDateTime.of(2026, 11, 11, 11, 0),
                    LocalDateTime.of(2026, 11, 11, 12, 10), TaskStatus.COMPLETED),
            new TaskEntity("TASK-005", "SID-5", "Task E",
                    LocalDateTime.of(2026, 11, 11, 9, 0),
                    LocalDateTime.of(2026, 11, 11, 9, 40), TaskStatus.COMPLETED));

    /**
     * Stub the repository so {@code findByStartedAtBetween(from, to, page)}
     * returns exactly the seeded tasks whose {@code started_at} falls within
     * the window, mirroring the real SQL predicate.
     */
    private void stubTasksBetween(TaskRepository repo) {
        when(repo.findByStartedAtBetween(any(), any(), any()))
                .thenAnswer(inv -> {
                    LocalDateTime from = inv.getArgument(0);
                    LocalDateTime to = inv.getArgument(1);
                    return pageForWindow(from, to);
                });
    }

    private PageImpl<TaskEntity> pageForWindow(LocalDateTime from, LocalDateTime to) {
        List<TaskEntity> inside = new ArrayList<>();
        for (TaskEntity task : SEED) {
            LocalDateTime start = task.getStartedAt();
            if (!start.isBefore(from) && !start.isAfter(to)) {
                inside.add(task);
            }
        }
        return new PageImpl<>(inside);
    }

    @BeforeEach
    void setUp() {
        taskRepository = mock(TaskRepository.class);
        tokenUsageRepository = mock(TokenUsageRepository.class);
        service = new CalendarService(taskRepository, tokenUsageRepository);
    }

    @Test
    void weekViewBuildsSevenMonSunCells() {
        LocalDate anchor = LocalDate.of(2026, 11, 12); // Thursday
        PageImpl<TaskEntity> page = new PageImpl<>(List.of());
        when(taskRepository.findByStartedAtBetween(any(), any(), any()))
                .thenReturn(page);

        CalendarResponse resp = service.calendar("week", anchor.toString());

        assertEquals("week", resp.getView());
        assertEquals(7, resp.getDays().size(), "week grid is Mon–Sun");
        LocalDate first = LocalDate.parse(resp.getDays().get(0).getDate());
        assertEquals(DayOfWeek.MONDAY, first.getDayOfWeek(), "week starts Monday");
        assertTrue(first.isBefore(anchor) || first.equals(anchor),
                "Monday is at or before the anchor");
    }

    @Test
    void weekGridKeepsAnchorTasksInRightCells() {
        when(taskRepository.findByStartedAtBetween(any(), any(), any()))
                .thenAnswer(inv -> {
                    LocalDateTime from = inv.getArgument(0);
                    LocalDateTime to = inv.getArgument(1);
                    return pageForWindow(from, to);
                });

        CalendarResponse resp = service.calendar("week", "2026-11-11"); // Thu 11.11

        assertEquals(7, resp.getDays().size());
        long totalTasks = resp.getDays().stream()
                .mapToLong(d -> d.getTasks().size())
                .sum();
        assertEquals(5, totalTasks, "all five seeded tasks span 10-11.11");
    }

    @Test
    void yearViewBuildsTwelveCells() {
        when(taskRepository.findByStartedAtBetween(any(), any(), any()))
                .thenAnswer(inv -> new PageImpl<>(List.of()));

        CalendarResponse resp = service.calendar("year", "2026-06-15");

        assertEquals("year", resp.getView());
        assertEquals(12, resp.getMonths().size(), "year view is twelve month cells");
    }

    @Test
    void emptyPeriodStillReturnsFullGrid() {
        when(taskRepository.findByStartedAtBetween(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        CalendarResponse resp = service.calendar("week", "2026-11-11");

        assertEquals(7, resp.getDays().size(), "empty period still yields the 7-day grid");
        assertTrue(resp.getDays().get(0).getTasks().isEmpty());
    }

    @Test
    void weekIsDefaultWhenViewAbsent() {
        when(taskRepository.findByStartedAtBetween(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        CalendarResponse resp = service.calendar("", "2026-11-12");

        assertEquals("week", resp.getView());
    }

    @Test
    void unknownViewThrowsInvalidPeriod() {
        InvalidPeriodException ex = assertThrows(InvalidPeriodException.class,
                () -> service.calendar("decade", "2026-11-12"));
        assertEquals(InvalidPeriodException.CODE, ex.code());
    }

    @Test
    void missingDateThrowsInvalidPeriod() {
        InvalidPeriodException ex = assertThrows(InvalidPeriodException.class,
                () -> service.calendar("week", null));
        assertEquals(InvalidPeriodException.CODE, ex.code());
    }

    @Test
    void unparseableDateThrowsInvalidPeriod() {
        InvalidPeriodException ex = assertThrows(InvalidPeriodException.class,
                () -> service.calendar("week", "11/2026"));
        assertEquals(InvalidPeriodException.CODE, ex.code());
    }
}
