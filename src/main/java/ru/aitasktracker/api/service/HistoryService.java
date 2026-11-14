package ru.aitasktracker.api.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.aitasktracker.domain.repository.IngestStateRepository;
import ru.aitasktracker.domain.repository.StepRepository;
import ru.aitasktracker.domain.repository.SolutionRepository;
import ru.aitasktracker.domain.repository.TaskRepository;
import ru.aitasktracker.domain.repository.TokenUsageRepository;

/**
 * History deletion service (contracts section 4.7). Deletes every task's rows in
 * one transaction, children before the parent ({@code steps} -> {@code solutions}
 * -> {@code token_usage} -> {@code tasks}) so foreign keys stay consistent, and
 * never touches {@code ingest_state}. Reports {@code true} when something was
 * deleted, {@code false} when the data set was empty (no-op, not an error).
 *
 * <p>The confirmation gate is enforced by the controller (contracts section 4.7),
 * not here: callers only invoke {@link #deleteAll()} once confirmation is present.
 */
@Service
public class HistoryService {

    private static final Logger log = LoggerFactory.getLogger(HistoryService.class);

    private final StepRepository stepRepository;
    private final SolutionRepository solutionRepository;
    private final TokenUsageRepository tokenUsageRepository;
    private final TaskRepository taskRepository;
    private final IngestStateRepository ingestStateRepository;

    public HistoryService(StepRepository stepRepository,
                          SolutionRepository solutionRepository,
                          TokenUsageRepository tokenUsageRepository,
                          TaskRepository taskRepository,
                          IngestStateRepository ingestStateRepository) {
        this.stepRepository = stepRepository;
        this.solutionRepository = solutionRepository;
        this.tokenUsageRepository = tokenUsageRepository;
        this.taskRepository = taskRepository;
        this.ingestStateRepository = ingestStateRepository;
    }

    /**
     * Delete all history atomically (contracts section 4.7). Children are removed
     * first; {@code ingest_state} is left intact so the next poll re-imports.
     *
     * @return {@code true} when at least one task was deleted, {@code false} when
     *         the data set was empty.
     */
    @Transactional
    public boolean deleteAll() {
        List<String> taskIds = taskRepository.findAll()
                .stream().map(t -> t.getTaskId()).toList();

        boolean anyTasks = !taskIds.isEmpty();
        for (String taskId : taskIds) {
            stepRepository.deleteByTaskId(taskId);
            solutionRepository.deleteByTaskId(taskId);
            tokenUsageRepository.deleteByTaskId(taskId);
            taskRepository.deleteById(taskId);
        }
        log.info("Deleted {} task(s) from history (ingest_state preserved).", taskIds.size());
        return anyTasks;
    }
}
