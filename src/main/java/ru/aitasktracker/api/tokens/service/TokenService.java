package ru.aitasktracker.api.tokens.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import ru.aitasktracker.api.exception.InvalidPeriodException;
import ru.aitasktracker.api.tokens.dto.TokenByModel;
import ru.aitasktracker.api.tokens.dto.TokenStatsResponse;
import ru.aitasktracker.api.tokens.dto.TokenTotals;
import ru.aitasktracker.domain.entity.TaskEntity;
import ru.aitasktracker.domain.entity.TokenUsageEntity;
import ru.aitasktracker.domain.repository.TaskRepository;
import ru.aitasktracker.domain.repository.TokenUsageRepository;

/**
 * Token statistics service (contracts §4.6, API-002). Sums token_usage rows by
 * model over the selected window: {@code all}, a single
 * {@code date}, or a {@code from}/{@code to} range where {@code to} must be
 * {@code >=} {@code from}. Empty period yields zeroed totals with
 * {@code has_data = false} so the widget never breaks (contracts §4.6, S-05).
 *
 * <p>Every repository call is an existing method, so no repository surface
 * change is required. Totals are the exact integer sum of seed usage.
 */
@Service
public class TokenService {

    private static final int MAX_ROWS = 100000;
    private static final String INVALID_PERIOD = "Некорректный период. Проверьте даты.";

    private final TaskRepository taskRepository;
    private final TokenUsageRepository tokenUsageRepository;

    public TokenService(TaskRepository taskRepository,
                        TokenUsageRepository tokenUsageRepository) {
        this.taskRepository = taskRepository;
        this.tokenUsageRepository = tokenUsageRepository;
    }

    /**
     * Resolve the period from the four query params and build the response.
     *
     * <p>{@code period} is {@code all} by default. A single {@code date} selects
     * that calendar day. {@code from}+{@code to} select an inclusive range and
     * {@code to} must be {@code >=} {@code from} or the call throws
     * {@code InvalidPeriodException} (contracts §4.6).
     */
    public TokenStatsResponse stats(String period, String date, String from, String to) {
        boolean hasFrom = from != null && !from.isBlank();
        boolean hasTo = to != null && !to.isBlank();
        if (hasFrom || hasTo) {
            if (!hasFrom || !hasTo) {
                throw new InvalidPeriodException(INVALID_PERIOD);
            }
            LocalDate f = parseDay(from);
            LocalDate t = parseDay(to);
            if (t.isBefore(f)) {
                throw new InvalidPeriodException(INVALID_PERIOD);
            }
            return build(from.trim() + ".." + to.trim(),
                    f.atStartOfDay(), t.plusDays(1).atStartOfDay());
        }
        if (date != null && !date.isBlank()) {
            LocalDate day = parseDay(date);
            return build(day.toString(), day.atStartOfDay(), day.plusDays(1).atStartOfDay());
        }
        if (period != null && !period.isBlank() && !"all".equalsIgnoreCase(period.trim())) {
            LocalDate day = parseDay(period);
            return build(day.toString(), day.atStartOfDay(), day.plusDays(1).atStartOfDay());
        }
        return build("all", null, null);
    }

    private TokenStatsResponse build(String label, LocalDateTime windowFrom,
                                     LocalDateTime windowTo) {
        List<TaskEntity> tasks;
        if (windowFrom == null) {
            tasks = taskRepository.findAll(
                    PageRequest.of(0, MAX_ROWS, Sort.by("startedAt"))).getContent();
        } else {
            tasks = taskRepository.findByStartedAtBetween(
                    windowFrom, windowTo, PageRequest.of(0, MAX_ROWS)).getContent();
        }

        Map<String, long[]> byModel = new LinkedHashMap<>();
        long input = 0L, output = 0L, cache = 0L;
        for (TaskEntity task : tasks) {
            for (TokenUsageEntity row : tokenUsageRepository.findByTaskId(task.getTaskId())) {
                long[] agg = byModel.computeIfAbsent(row.getModel(), k -> new long[3]);
                agg[0] += row.getInputTokens();
                agg[1] += row.getOutputTokens();
                agg[2] += row.getCacheTokens();
                input += row.getInputTokens();
                output += row.getOutputTokens();
                cache += row.getCacheTokens();
            }
        }

        List<TokenByModel> rows = new ArrayList<>();
        for (Map.Entry<String, long[]> entry : byModel.entrySet()) {
            long modelInput = entry.getValue()[0];
            long modelOutput = entry.getValue()[1];
            long modelCache = entry.getValue()[2];
            rows.add(new TokenByModel(entry.getKey(), modelInput, modelOutput,
                    modelCache, modelInput + modelOutput + modelCache));
        }

        boolean hasData = !rows.isEmpty();
        TokenTotals totals = new TokenTotals(input, output, input + output + cache);
        return new TokenStatsResponse(label, rows, totals, hasData);
    }

    private static LocalDate parseDay(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new InvalidPeriodException("Некорректный период. Проверьте даты.");
        }
        try {
            return LocalDate.parse(raw.trim());
        } catch (RuntimeException e) {
            throw new InvalidPeriodException("Некорректный период. Проверьте даты.");
        }
    }
}
