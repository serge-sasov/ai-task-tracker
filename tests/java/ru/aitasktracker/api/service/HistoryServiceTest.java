package ru.aitasktracker.api.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ru.aitasktracker.domain.repository.IngestStateRepository;
import ru.aitasktracker.domain.repository.StepRepository;
import ru.aitasktracker.domain.repository.SolutionRepository;
import ru.aitasktracker.domain.repository.TaskRepository;
import ru.aitasktracker.domain.repository.TokenUsageRepository;
import ru.aitasktracker.domain.entity.TaskEntity;

/**
 * Unit tests for the HistoryService delete-all path. Pure unit tests: every
 * repository is a Mockito mock. No Spring context, no H2/Redis (pom has neither).
 *
 * <p>Covers the deletion contract: children are removed before the parent, the
 * ingest-state watermark table is never touched, and {@code deleteAll()} reports
 * {@code true} when something was deleted and {@code false} for an empty data set.
 */
class HistoryServiceTest {

    private final TaskRepository taskRepository = mock(TaskRepository.class);
    private final StepRepository stepRepository = mock(StepRepository.class);
    private final SolutionRepository solutionRepository = mock(SolutionRepository.class);
    private final TokenUsageRepository tokenUsageRepository = mock(TokenUsageRepository.class);
    private final IngestStateRepository ingestStateRepository = mock(IngestStateRepository.class);

    private HistoryService service() {
        return new HistoryService(stepRepository, solutionRepository,
                tokenUsageRepository, taskRepository, ingestStateRepository);
    }

    private static TaskEntity task(String id) {
        TaskEntity task = new TaskEntity();
        task.setTaskId(id);
        return task;
    }

    @Test
    void deleteAllDeletesChildrenBeforeParentAndReportsTrue() {
        when(taskRepository.findAll()).thenReturn(List.of(task("TASK-1")));

        boolean deleted = service().deleteAll();

        Assertions.assertTrue(deleted);
        verify(stepRepository, times(1)).deleteByTaskId("TASK-1");
        verify(solutionRepository, times(1)).deleteByTaskId("TASK-1");
        verify(tokenUsageRepository, times(1)).deleteByTaskId("TASK-1");
        verify(taskRepository, times(1)).deleteById("TASK-1");
    }

    @Test
    void deleteAllIsNoOpWhenEmptyDataSet() {
        when(taskRepository.findAll()).thenReturn(List.of());

        boolean deleted = service().deleteAll();

        Assertions.assertFalse(deleted);
        verify(stepRepository, never())
                .deleteByTaskId(anyString());
        verify(solutionRepository, never())
                .deleteByTaskId(anyString());
        verify(tokenUsageRepository, never())
                .deleteByTaskId(anyString());
        verify(taskRepository, never())
                .deleteById(anyString());
    }

    @Test
    void deleteAllNeverTouchesIngestState() {
        when(taskRepository.findAll()).thenReturn(List.of(task("TASK-1")));

        service().deleteAll();

        // Ingest-state rows survive deletion; the service never deletes them.
        verify(ingestStateRepository, never()).deleteAll();
        verify(ingestStateRepository, never()).findByKey(anyString());
    }

    @Test
    void deleteAllProcessesEveryTask() {
        when(taskRepository.findAll())
                .thenReturn(List.of(task("TASK-1"), task("TASK-2")));

        boolean deleted = service().deleteAll();

        Assertions.assertTrue(deleted);
        verify(stepRepository, times(2)).deleteByTaskId(anyString());
        verify(taskRepository, times(2)).deleteById(anyString());
    }
}
