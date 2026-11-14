package ru.aitasktracker.api.tokens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;

import ru.aitasktracker.api.exception.InvalidPeriodException;
import ru.aitasktracker.api.tokens.dto.TokenStatsResponse;
import ru.aitasktracker.api.tokens.service.TokenService;
import ru.aitasktracker.domain.TaskStatus;
import ru.aitasktracker.domain.entity.TaskEntity;
import ru.aitasktracker.domain.entity.TokenUsageEntity;
import ru.aitasktracker.domain.repository.TaskRepository;
import ru.aitasktracker.domain.repository.TokenUsageRepository;

/**
 * Unit tests for the TokenService. Every repository is a Mockito mock; no
 * Spring context, no H2/Redis (the pom has neither). Covers contracts §4.6:
 * the {@code all}/{@code date}/{@code from..to} windows, the per-model rows,
 * the exact totals, and the INVALID_PERIOD range check.
 */
class TokenServiceTest {

    private TaskRepository taskRepository;
    private TokenUsageRepository tokenUsageRepository;
    private TokenService service;

    private static final List<TaskEntity> SEED = List.of(
            new TaskEntity("TASK-001", "SID-1", "Task A",
                    LocalDateTime.of(2026, 11, 10, 9, 0), null, TaskStatus.COMPLETED),
            new TaskEntity("TASK-002", "SID-2", "Task B",
                    LocalDateTime.of(2026, 11, 10, 10, 0), null, TaskStatus.COMPLETED),
            new TaskEntity("TASK-003", "SID-3", "Task C",
                    LocalDateTime.of(2026, 11, 11, 8, 0), null, TaskStatus.COMPLETED));

    private static final List<TokenUsageEntity> TASK_1 = List.of(
            new TokenUsageEntity("TASK-001", "m1", "qwen3.8:27b",
                    10750, 4590, 0, "task"));
    private static final List<TokenUsageEntity> TASK_2 = List.of(
            new TokenUsageEntity("TASK-002", "m1", "qwen3.8:27b",
                    0, 0, 0, "task"));
    private static final List<TokenUsageEntity> TASK_3 = List.of(
            new TokenUsageEntity("TASK-003", "m2", "llama3.1:8b",
                    1400, 550, 0, "task"));

    /**
     * Stub the repository so {@code findByStartedAtBetween(from, to)} returns
     * the seeded tasks whose {@code started_at} falls within the window,
     * mirroring the real SQL predicate across every view.
     */
    private void stubTasksBetween(TaskRepository repo) {
        when(repo.findByStartedAtBetween(any(), any(), any()))
                .thenAnswer(inv -> {
                    LocalDateTime from = inv.getArgument(0);
                    LocalDateTime to = inv.getArgument(1);
                    List<TaskEntity> inside = new ArrayList<>();
                    for (TaskEntity task : SEED) {
                        LocalDateTime start = task.getStartedAt();
                        if (!start.isBefore(from) && !start.isAfter(to)) {
                            inside.add(task);
                        }
                    }
                    return new PageImpl<>(inside);
                });
    }

    private List<TokenUsageEntity> rowsFor(String taskId) {
        switch (taskId) {
            case "TASK-001": return TASK_1;
            case "TASK-002": return TASK_2;
            case "TASK-003": return TASK_3;
            default: return List.of();
        }
    }

    @BeforeEach
    void setUp() {
        taskRepository = mock(TaskRepository.class);
        tokenUsageRepository = mock(TokenUsageRepository.class);
        service = new TokenService(taskRepository, tokenUsageRepository);
    }

    @Test
    void allPeriodReturnsHasDataWithRows() {
        stubTasksBetween(taskRepository);
        when(tokenUsageRepository.findByTaskId(any()))
                .thenAnswer(inv -> rowsFor(inv.getArgument(0)));

        TokenStatsResponse resp = service.stats("all", null, null, null);

        assertEquals("all", resp.getPeriod());
        assertTrue(resp.isHasData());
        assertEquals(2, resp.getByModel().size());
        assertEquals(17290L, resp.getTotal().getTotalTokens());
    }

    @Test
    void emptyPeriodReturnsZeroTotalsAndNoData() {
        when(taskRepository.findByStartedAtBetween(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        TokenStatsResponse resp = service.stats("all", null, null, null);

        assertFalse(resp.isHasData());
        assertEquals(0, resp.getByModel().size());
        assertEquals(0L, resp.getTotal().getTotalTokens());
        assertEquals(0L, resp.getTotal().getInputTokens());
        assertEquals(0L, resp.getTotal().getOutputTokens());
    }

    @Test
    void singleDayWindowIsScoped() {
        stubTasksBetween(taskRepository);
        when(tokenUsageRepository.findByTaskId(any()))
                .thenAnswer(inv -> rowsFor(inv.getArgument(0)));

        TokenStatsResponse resp = service.stats("day", "2026-11-11", null, null);

        assertEquals("2026-11-11", resp.getPeriod());
        assertEquals(1950L, resp.getTotal().getTotalTokens());
    }

    @Test
    void fromDateToWindowIsScoped() {
        stubTasksBetween(taskRepository);
        when(tokenUsageRepository.findByTaskId(any()))
                .thenAnswer(inv -> rowsFor(inv.getArgument(0)));

        TokenStatsResponse resp = service.stats("range", null, "2026-11-10", "2026-11-11");

        assertTrue(resp.getPeriod().startsWith("2026-11-10"));
        assertEquals(17290L, resp.getTotal().getTotalTokens(), "10.11 + 11.11 totals");
    }

    @Test
    void toBeforeFromThrowsInvalidPeriod() {
        InvalidPeriodException ex = assertThrows(InvalidPeriodException.class,
                () -> service.stats("range", null, "2026-11-12", "2026-11-10"));
        assertEquals(InvalidPeriodException.CODE, ex.code());
    }

    @Test
    void invalidRangeDateThrowsInvalidPeriod() {
        InvalidPeriodException ex = assertThrows(InvalidPeriodException.class,
                () -> service.stats("range", null, "bad", "2026-11-10"));
        assertEquals(InvalidPeriodException.CODE, ex.code());
    }
}
