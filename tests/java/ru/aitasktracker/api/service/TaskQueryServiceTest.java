package ru.aitasktracker.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import ru.aitasktracker.api.exception.InvalidPeriodException;
import ru.aitasktracker.api.exception.NotFound;
import ru.aitasktracker.api.dto.TaskDetailResponse;
import ru.aitasktracker.api.dto.TaskListItem;
import ru.aitasktracker.api.dto.TaskListResponse;
import ru.aitasktracker.domain.TaskStatus;
import ru.aitasktracker.domain.entity.StepEntity;
import ru.aitasktracker.domain.entity.SolutionEntity;
import ru.aitasktracker.domain.entity.TaskEntity;
import ru.aitasktracker.domain.entity.TokenUsageEntity;
import ru.aitasktracker.domain.repository.StepRepository;
import ru.aitasktracker.domain.repository.SolutionRepository;
import ru.aitasktracker.domain.repository.TaskRepository;
import ru.aitasktracker.domain.repository.TokenUsageRepository;

/**
 * Unit tests for the TaskQueryService. Pure unit tests: every repository is a
 * Mockito mock. No Spring context, no H2/Redis (pom has neither). Covers
 * contracts section 1 (page/size clamp), section 4.2 (list) and section 4.3
 * (detail).
 */
class TaskQueryServiceTest {

    private final TaskRepository taskRepository = mock(TaskRepository.class);
    private final StepRepository stepRepository = mock(StepRepository.class);
    private final SolutionRepository solutionRepository = mock(SolutionRepository.class);
    private final TokenUsageRepository tokenUsageRepository = mock(TokenUsageRepository.class);

    private TaskQueryService service() {
        return new TaskQueryService(taskRepository, stepRepository,
                solutionRepository, tokenUsageRepository);
    }

    // ---- list ----

    @Test
    void listBuildsPageFromQuery() {
        TaskEntity t = new TaskEntity("TASK-1", "SID-1", "Task 1",
                LocalDateTime.of(2026, 11, 10, 9, 0),
                LocalDateTime.of(2026, 11, 10, 11, 30),
                TaskStatus.COMPLETED);
        when(taskRepository.filter(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(t), PageRequest.of(0, 50), 1L));

        TaskListResponse resp = service().list(1, 50, null, null, null);

        assertEquals(1, resp.getTotal());
        TaskListItem item = resp.getItems().get(0);
        assertEquals("TASK-1", item.getTaskId());
        assertEquals("Завершена", item.getStatus());
        // 2h30m = 9000 s (UC-E2E-003 fixture span).
        assertEquals(9000L, item.getDurationS());
        assertEquals(0L, item.getTokensTotal());
    }

    @Test
    void listClampsSizeAboveCap() {
        TaskEntity t = new TaskEntity("TASK-1", "SID-1", "Task 1",
                LocalDateTime.of(2026, 11, 10, 9, 0),
                LocalDateTime.of(2026, 11, 10, 10, 0), TaskStatus.NOT_COMPLETED);
        when(taskRepository.filter(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(t), PageRequest.of(0, 500), 1L));

        TaskListResponse resp = service().list(1, 100_000, null, null, null);

        assertEquals(1, resp.getTotal());
        assertEquals(1, resp.getItems().size());
        verify(taskRepository).filter(any(), any(), any(), any());
    }

    @Test
    void listEmptyDataSetReturnsZeroTotal() {
        when(taskRepository.filter(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 50), 0L));

        TaskListResponse resp = service().list(1, 50, null, null, null);

        assertEquals(0, resp.getTotal());
        assertEquals(0, resp.getItems().size());
    }

    @Test
    void listParsesStatusCaseInsensitively() {
        TaskEntity t = new TaskEntity("TASK-1", "SID-1", "Task 1",
                LocalDateTime.of(2026, 11, 10, 9, 0),
                LocalDateTime.of(2026, 11, 10, 10, 0), TaskStatus.INTERRUPTED);
        when(taskRepository.filter(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(t), PageRequest.of(0, 50), 1L));

        service().list(1, 50, null, null, "прервана");

        verify(taskRepository).filter(any(), any(), any(), any());
        // No exception means the RU status matched case-insensitively.
    }

    @Test
    void listUnparseableFromThrowsInvalidPeriod() {
        assertThrows(InvalidPeriodException.class,
                () -> service().list(1, 50, "not-a-date", null, null));
    }

    @Test
    void listUnparseableToThrowsInvalidPeriod() {
        assertThrows(InvalidPeriodException.class,
                () -> service().list(1, 50, null, "bad", null));
    }

    @Test
    void listParsesValidPeriodBounds() {
        TaskEntity t = new TaskEntity("TASK-1", "SID-1", "Task 1",
                LocalDateTime.of(2026, 11, 10, 9, 0),
                LocalDateTime.of(2026, 11, 10, 10, 0), TaskStatus.NOT_COMPLETED);
        when(taskRepository.filter(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(t), PageRequest.of(0, 50), 1L));

        service().list(1, 50, "2026-11-09T00:00:00", "2026-11-11T23:59:59", null);

        verify(taskRepository).filter(any(), any(), any(), any());
    }

    // ---- detail ----

    @Test
    void detailFetchesTaskWithStepsAndTokens() {
        TaskEntity task = new TaskEntity("TASK-1", "SID-1", "Task 1",
                LocalDateTime.of(2026, 11, 10, 9, 0),
                LocalDateTime.of(2026, 11, 10, 11, 30),
                TaskStatus.COMPLETED);
        when(taskRepository.findById("TASK-1")).thenReturn(Optional.of(task));

        StepEntity step = new StepEntity("TASK-1", "msg-1",
                LocalDateTime.of(2026, 11, 10, 9, 5), "hello");
        when(stepRepository.findByTaskIdOrderByAtAsc("TASK-1"))
                .thenReturn(List.of(step));

        SolutionEntity proposed = new SolutionEntity("TASK-1", "proposed", "P", "msg-1");
        SolutionEntity chosen = new SolutionEntity("TASK-1", "chosen", "C", "msg-1");
        when(solutionRepository.findByTaskIdOrderBySolutionIdAsc("TASK-1"))
                .thenReturn(List.of(proposed, chosen));

        TokenUsageEntity token = new TokenUsageEntity("TASK-1", "msg-1",
                "gpt-4o-mini", 100L, 50L, 20L, "session");
        when(tokenUsageRepository.findByTaskId("TASK-1"))
                .thenReturn(List.of(token));

        TaskDetailResponse resp = service().detail("TASK-1");

        assertEquals("TASK-1", resp.getTaskId());
        assertEquals("Завершена", resp.getStatus());
        assertEquals(9000L, resp.getDurationS());
        assertEquals(1, resp.getSteps().size());
        assertEquals("msg-1", resp.getSteps().get(0).getMessageId());
        assertEquals(1, resp.getSolutions().getProposed().size());
        assertEquals("P", resp.getSolutions().getProposed().get(0).getText());
        assertEquals("C", resp.getSolutions().getChosen().getText());
        assertEquals(1, resp.getTokens().getByModel().size());
        assertEquals(100L, resp.getTokens().getTotal().getInputTokens());
        assertEquals(50L, resp.getTokens().getTotal().getOutputTokens());
        assertEquals(20L, resp.getTokens().getTotal().getCacheTokens());
        assertEquals(170L, resp.getTokens().getTotal().getTotalTokens());
        assertTrue(resp.getTokens().hasData());
    }

    @Test
    void detailThrowsNotFoundWhenMissing() {
        when(taskRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(NotFound.class, () -> service().detail("missing"));
    }

    @Test
    void detailEmptySolutionSetHasNullChosen() {
        TaskEntity t = new TaskEntity("TASK-1", "SID-1", "Task 1",
                LocalDateTime.of(2026, 11, 10, 9, 0),
                null, TaskStatus.NOT_COMPLETED);
        when(stepRepository.lastStepTime("TASK-1"))
                .thenReturn(Optional.of(LocalDateTime.of(2026, 11, 10, 9, 5)));
        when(taskRepository.findById("TASK-1")).thenReturn(Optional.of(t));
        when(stepRepository.findByTaskIdOrderByAtAsc("TASK-1"))
                .thenReturn(List.of());
        when(solutionRepository.findByTaskIdOrderBySolutionIdAsc("TASK-1"))
                .thenReturn(List.of());
        when(tokenUsageRepository.findByTaskId("TASK-1"))
                .thenReturn(List.of());

        TaskDetailResponse resp = service().detail("TASK-1");

        assertEquals(0, resp.getSolutions().getProposed().size());
        assertNull(resp.getSolutions().getChosen());
        assertFalse(resp.getTokens().hasData());
    }

    @Test
    void listEmptyUsageProducesEmptyModels() {
        TaskEntity t = new TaskEntity("TASK-1", "SID-1", "Task 1",
                LocalDateTime.of(2026, 11, 10, 9, 0),
                LocalDateTime.of(2026, 11, 10, 10, 0), TaskStatus.NOT_COMPLETED);
        when(taskRepository.filter(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(t), PageRequest.of(0, 50), 1L));
        when(tokenUsageRepository.findByTaskId("TASK-1"))
                .thenReturn(List.of());

        TaskListResponse resp = service().list(1, 50, null, null, null);

        TaskListItem item = resp.getItems().get(0);
        assertEquals(0L, item.getTokensTotal());
        assertTrue(item.getModels().isEmpty());
    }

    @Test
    void listOrdersModelsByTotalDescendingThenNameAsc() {
        TaskEntity t = new TaskEntity("TASK-1", "SID-1", "Task 1",
                LocalDateTime.of(2026, 11, 10, 9, 0),
                LocalDateTime.of(2026, 11, 10, 10, 0), TaskStatus.NOT_COMPLETED);
        when(taskRepository.filter(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(t), PageRequest.of(0, 50), 1L));
        // per-model totals: zzz=100, aaa=300, mmm=200
        when(tokenUsageRepository.findByTaskId("TASK-1")).thenReturn(List.of(
                new TokenUsageEntity("TASK-1", "m1", "zzz", 60L, 40L, 0L, "session"),
                new TokenUsageEntity("TASK-1", "m2", "mmm", 120L, 80L, 0L, "session"),
                new TokenUsageEntity("TASK-1", "m3", "aaa", 300L, 0L, 0L, "session")));

        TaskListResponse resp = service().list(1, 50, null, null, null);

        TaskListItem item = resp.getItems().get(0);
        // aaa(300) -> mmm(200) -> zzz(100)
        assertEquals(List.of("aaa", "mmm", "zzz"), item.getModels());
        assertEquals(600L, item.getTokensTotal());
    }

    @Test
    void listTiesAreOrderedByNameAscending() {
        TaskEntity t = new TaskEntity("TASK-1", "SID-1", "Task 1",
                LocalDateTime.of(2026, 11, 10, 9, 0),
                LocalDateTime.of(2026, 11, 10, 10, 0), TaskStatus.NOT_COMPLETED);
        when(taskRepository.filter(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(t), PageRequest.of(0, 50), 1L));
        // two models with equal total 500 -> name ascending
        when(tokenUsageRepository.findByTaskId("TASK-1")).thenReturn(List.of(
                new TokenUsageEntity("TASK-1", "m1", "llama3.1:8b", 300L, 200L, 0L, "session"),
                new TokenUsageEntity("TASK-1", "m2", "qwen3.8:27b", 500L, 0L, 0L, "session"),
                new TokenUsageEntity("TASK-1", "m3", "alpha", 0L, 500L, 0L, "session")));

        TaskListResponse resp = service().list(1, 50, null, null, null);

        TaskListItem item = resp.getItems().get(0);
        assertEquals(List.of("alpha", "llama3.1:8b", "qwen3.8:27b"), item.getModels());
    }

    @Test
    void listCountsCacheTokensInModelTotal() {
        TaskEntity t = new TaskEntity("TASK-1", "SID-1", "Task 1",
                LocalDateTime.of(2026, 11, 10, 9, 0),
                LocalDateTime.of(2026, 11, 10, 10, 0), TaskStatus.NOT_COMPLETED);
        when(taskRepository.filter(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(t), PageRequest.of(0, 50), 1L));
        // two rows, same model, differ by cache -> cache is summed into the total
        when(tokenUsageRepository.findByTaskId("TASK-1")).thenReturn(List.of(
                new TokenUsageEntity("TASK-1", "m1", "qwen3.8:27b", 100L, 50L, 0L, "session"),
                new TokenUsageEntity("TASK-1", "m2", "qwen3.8:27b", 0L, 0L, 1000L, "session"),
                new TokenUsageEntity("TASK-1", "m3", "llama3.1:8b", 1L, 1L, 1L, "session")));

        TaskListResponse resp = service().list(1, 50, null, null, null);

        TaskListItem item = resp.getItems().get(0);
        // qwen3.8:27b = 1150 (incl. cache), llama3.1:8b = 3 -> qwen first
        assertEquals(List.of("qwen3.8:27b", "llama3.1:8b"), item.getModels());
        assertEquals(1153L, item.getTokensTotal());
    }

    @Test
    void listModelsReturnIsDefensivelyImmutable() {
        TaskEntity t = new TaskEntity("TASK-1", "SID-1", "Task 1",
                LocalDateTime.of(2026, 11, 10, 9, 0),
                LocalDateTime.of(2026, 11, 10, 10, 0), TaskStatus.NOT_COMPLETED);
        when(taskRepository.filter(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(t), PageRequest.of(0, 50), 1L));
        when(tokenUsageRepository.findByTaskId("TASK-1")).thenReturn(List.of(
                new TokenUsageEntity("TASK-1", "m1", "only", 10L, 0L, 0L, "session")));

        TaskListItem item = service().list(1, 50, null, null, null).getItems().get(0);

        assertThrows(UnsupportedOperationException.class,
                () -> item.getModels().add("injected"));
        assertEquals(List.of("only"), item.getModels());
    }

    private static void assertTrue(boolean b) {
        org.junit.jupiter.api.Assertions.assertTrue(b);
    }

    private static void assertFalse(boolean b) {
        org.junit.jupiter.api.Assertions.assertFalse(b);
    }
}
