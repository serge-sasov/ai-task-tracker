package ru.aitasktracker.api.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import ru.aitasktracker.api.dto.StepItem;
import ru.aitasktracker.api.dto.SolutionItem;
import ru.aitasktracker.api.dto.TaskDetailResponse;
import ru.aitasktracker.api.dto.TaskDetailResponse.SolutionsBlock;
import ru.aitasktracker.api.dto.TaskDetailResponse.TokensBlock;
import ru.aitasktracker.api.dto.TaskListItem;
import ru.aitasktracker.api.dto.TaskListResponse;
import ru.aitasktracker.api.dto.TokenByModel;
import ru.aitasktracker.api.dto.TokenTotals;
import ru.aitasktracker.api.exception.NotFound;
import ru.aitasktracker.domain.TaskDuration;
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
 * Task query service (contracts section 4.2 / 4.3). Thin orchestration over the
 * repositories: builds the task list and task detail responses. All logic here,
 * controllers stay thin (and mockable).
 *
 * <p>{@code page} is 1-based, {@code size} defaults to 50 and is clamped to
 * {@code 500} (a {@code size} above the cap is clamped, never rejected).
 * {@code from}/{@code to} are ISO 8601 local wall-clock strings; an empty value
 * means no filter, an unparseable value throws {@code InvalidPeriodException}.
 * {@code status} is a RU enum value, matched case-insensitively (empty / unmatched
 * means no filter).
 */
public class TaskQueryService {

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final int MAX_SIZE = 500;

    private final TaskRepository taskRepository;
    private final StepRepository stepRepository;
    private final SolutionRepository solutionRepository;
    private final TokenUsageRepository tokenUsageRepository;

    public TaskQueryService(TaskRepository taskRepository,
                            StepRepository stepRepository,
                            SolutionRepository solutionRepository,
                            TokenUsageRepository tokenUsageRepository) {
        this.taskRepository = taskRepository;
        this.stepRepository = stepRepository;
        this.solutionRepository = solutionRepository;
        this.tokenUsageRepository = tokenUsageRepository;
    }

    /**
     * Parse a possibly-absent ISO 8601 local wall-clock query param. Empty returns
     * {@code null} (no filter); an unparseable value throws
     * {@code InvalidPeriodException}.
     */
    public static LocalDateTime parseIsoOrNull(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(raw.trim(), TS_FMT);
        } catch (Exception e) {
            throw new ru.aitasktracker.api.exception.InvalidPeriodException(
                    "Некорректный период. Проверьте даты.");
        }
    }

    /** Case-insensitive RU lookup; empty/blank/unmatched yields {@code null} (no filter). */
    public static TaskStatus parseStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return TaskStatus.fromValue(raw).orElse(null);
    }

    /**
     * Filter, sort (started_at desc) and paginate the task list (contracts section
     * 4.2). {@code from}/{@code to}/{@code status} are nullable strings.
     */
    public TaskListResponse list(int page, int size,
                                 String from, String to, String status) {
        LocalDateTime fromLdt = parseIsoOrNull(from);
        LocalDateTime toLdt = parseIsoOrNull(to);
        TaskStatus st = parseStatus(status);

        int clampedSize = Math.min(Math.max(size, 1), MAX_SIZE);
        int safePage = Math.max(page, 1);
        Pageable pageable = PageRequest.of(safePage - 1, clampedSize, Sort.by(Sort.Direction.DESC, "startedAt"));
        Page<TaskEntity> pageResult = taskRepository.filter(fromLdt, toLdt, st, pageable);

        List<TaskListItem> items = new ArrayList<>();
        for (TaskEntity task : pageResult.getContent()) {
            items.add(toListItem(task));
        }
        return new TaskListResponse((int) pageResult.getTotalElements(), items);
    }

    private TaskListItem toListItem(TaskEntity task) {
        long durationS = duration(task);
        List<TokenUsageEntity> rows = tokenUsageRepository.findByTaskId(task.getTaskId());
        long tokensTotal = total(rows);
        List<String> models = tokenModels(rows);
        String finished = task.getFinishedAt() == null ? null : iso(task.getFinishedAt());
        return new TaskListItem(task.getTaskId(), task.getName(), iso(task.getStartedAt()),
                finished, task.getStatus().value(), durationS, tokensTotal, models);
    }

    /** duration_s = seconds(startedAt, finishedAt or lastStepAt) (contracts section 2). */
    private long duration(TaskEntity task) {
        LocalDateTime lastStep = stepRepository.lastStepTime(task.getTaskId()).orElse(null);
        return TaskDuration.seconds(task.getStartedAt(), task.getFinishedAt(), lastStep);
    }

    /** Sum of input+output+cache across all token_usage rows, 0 if none. */
    private static long total(List<TokenUsageEntity> rows) {
        long sum = 0L;
        for (TokenUsageEntity row : rows) {
            sum += row.getInputTokens() + row.getOutputTokens() + row.getCacheTokens();
        }
        return sum;
    }

    /**
     * Ordered model list from the given rows (contracts §4.2): per model, the
     * sum of input+output+cache, ordered by that total descending then by model
     * name ascending for ties. Empty when there are no rows.
     */
    private static List<String> tokenModels(List<TokenUsageEntity> rows) {
        Map<String, Long> byModel = new LinkedHashMap<>();
        for (TokenUsageEntity row : rows) {
            long modelTokens = row.getInputTokens() + row.getOutputTokens()
                    + row.getCacheTokens();
            byModel.merge(row.getModel(), modelTokens, Long::sum);
        }
        List<String> ordered = new ArrayList<>(byModel.keySet());
        ordered.sort((a, b) -> {
            int cmp = Long.compare(byModel.get(b), byModel.get(a));
            if (cmp != 0) {
                return cmp;
            }
            return a.compareTo(b);
        });
        return ordered;
    }

    /**
     * Task detail with steps (chronological), solutions and token breakdown
     * (contracts section 4.3).
     */
    public TaskDetailResponse detail(String taskId) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(NotFound::new);

        List<StepItem> steps = stepRepository.findByTaskIdOrderByAtAsc(taskId).stream()
                .map(step -> new StepItem(step.getMessageId(), iso(step.getAt()), step.getText()))
                .toList();

        SolutionItem chosen = null;
        List<SolutionItem> proposed = new ArrayList<>();
        for (SolutionEntity sol : solutionRepository.findByTaskIdOrderBySolutionIdAsc(taskId)) {
            SolutionItem item = new SolutionItem(sol.getText(), sol.getSourceMessageId());
            if ("chosen".equals(sol.getKind())) {
                chosen = item;
            } else {
                proposed.add(item);
            }
        }
        SolutionsBlock solutions = new SolutionsBlock(proposed, chosen);

        List<TokenUsageEntity> rows = tokenUsageRepository.findByTaskId(taskId);
        TokensBlock tokens = buildTokenRows(rows);

        String finished = task.getFinishedAt() == null ? null : iso(task.getFinishedAt());
        return new TaskDetailResponse(task.getTaskId(), task.getName(), iso(task.getStartedAt()),
                finished, task.getStatus().value(), duration(task), steps, solutions, tokens);
    }

    private TokensBlock buildTokenRows(List<TokenUsageEntity> rows) {
        boolean hasData = !rows.isEmpty();
        long input = 0L, output = 0L, cache = 0L;
        for (TokenUsageEntity row : rows) {
            input += row.getInputTokens();
            output += row.getOutputTokens();
            cache += row.getCacheTokens();
        }
        TokenTotals total = new TokenTotals(input, output, cache, hasData);

        List<TokenByModel> byModel = new ArrayList<>();
        for (TokenUsageEntity row : rows) {
            long totalTokens = row.getInputTokens() + row.getOutputTokens() + row.getCacheTokens();
            byModel.add(new TokenByModel(row.getModel(), row.getInputTokens(),
                    row.getOutputTokens(), row.getCacheTokens(), totalTokens));
        }
        return new TokensBlock(byModel, total, hasData);
    }

    private static String iso(LocalDateTime value) {
        return value.format(TS_FMT);
    }
}
