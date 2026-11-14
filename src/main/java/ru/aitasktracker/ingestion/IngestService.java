package ru.aitasktracker.ingestion;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.aitasktracker.domain.TaskStatus;
import ru.aitasktracker.domain.TaskStatusService;
import ru.aitasktracker.domain.entity.IngestStateEntity;
import ru.aitasktracker.domain.entity.StepEntity;
import ru.aitasktracker.domain.entity.SolutionEntity;
import ru.aitasktracker.domain.entity.TaskEntity;
import ru.aitasktracker.domain.entity.TokenUsageEntity;
import ru.aitasktracker.domain.repository.IngestStateRepository;
import ru.aitasktracker.domain.repository.StepRepository;
import ru.aitasktracker.domain.repository.SolutionRepository;
import ru.aitasktracker.domain.repository.TaskRepository;
import ru.aitasktracker.domain.repository.TokenUsageRepository;
import ru.aitasktracker.ingestion.domain.IngestedSession;
import ru.aitasktracker.ingestion.domain.IngestJsonParser;
import ru.aitasktracker.ingestion.domain.SourceStatus;
import ru.aitasktracker.ingestion.extractor.SolutionExtractor;

/**
 * Ingestion service (contracts §3). Turns per-session opencode JSON files into a
 * single-Postgres-transaction upsert batch (idempotent, R-025), updates the
 * watermark in {@code ingest_state}, and computes the per-cycle {@link SourceStatus}
 * (R-028). It never blocks on the {@link SolutionExtractor} (R-028) and never
 * writes to the read-only source directory (C-3).
 */
@Service
public class IngestService {

    private static final Logger log = LoggerFactory.getLogger(IngestService.class);

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /** Watermark keys (ADR-0001 §2, contracts §2). */
    static final String WK_MTIME = "last_file_mtime";
    static final String WK_SYNC = "last_sync_at";
    static final String WK_COUNT = "last_file_count";
    static final String WK_PARSE = "parse_errors";
    static final String WK_SOURCE_STATUS = "source_status";
    static final String WK_LAST_INGEST = "last_ingest_at";

    private final Path dataDir;
    private final IngestJsonParser parser;
    private final SolutionExtractor extractor;
    private final IngestStateRepository ingestStateRepository;
    private final TaskRepository taskRepository;
    private final StepRepository stepRepository;
    private final SolutionRepository solutionRepository;
    private final TokenUsageRepository tokenUsageRepository;

    public IngestService(Path dataDir,
                         IngestJsonParser parser,
                         SolutionExtractor extractor,
                         IngestStateRepository ingestStateRepository,
                         TaskRepository taskRepository,
                         StepRepository stepRepository,
                         SolutionRepository solutionRepository,
                         TokenUsageRepository tokenUsageRepository) {
        this.dataDir = dataDir;
        this.parser = parser;
        this.extractor = extractor;
        this.ingestStateRepository = ingestStateRepository;
        this.taskRepository = taskRepository;
        this.stepRepository = stepRepository;
        this.solutionRepository = solutionRepository;
        this.tokenUsageRepository = tokenUsageRepository;
    }

    /**
     * Full poll cycle: read source files, parse each (capturing per-file errors),
     * upsert the batch in one transaction, update the watermark and source_status.
     *
     * @param pollIntervalSeconds cadence of the poller, used to classify stale source
     * @return the parsed sessions that were written (empty when source unavailable)
     */
    @Transactional
    public List<IngestedSession> ingestAll(int pollIntervalSeconds) {
        long pollMs = Math.max(1, pollIntervalSeconds) * 1000L;
        SourceStatus status = computeSourceStatus(pollMs);
        if (status == SourceStatus.UNAVAILABLE) {
            log.info("Source unavailable at {}; skipping ingestion cycle.", dataDir);
            updateWatermark(SourceStatus.UNAVAILABLE, 0L, 0L, List.of());
            return List.of();
        }

        List<Path> files = readJsonFiles();
        List<String> parseErrors = new ArrayList<>();
        List<IngestedSession> sessions = new ArrayList<>();

        for (Path file : files) {
            IngestJsonParser.ParseOutcome outcome =
                    parser.parseFile(file, file.getFileName().toString());
            if (outcome.isSuccessful()) {
                sessions.add(outcome.getSession());
            } else {
                String message = file.getFileName() + ": " + outcome.getError();
                log.warn("Skipping session file {}: {}", file.getFileName(), outcome.getError());
                parseErrors.add(message);
            }
        }

        for (IngestedSession session : sessions) {
            try {
                upsertSession(session);
            } catch (RuntimeException e) {
                log.error("Failed to upsert session {}: {}",
                        session.getSessionId(), e.getMessage());
                // Roll back the whole cycle if one session cannot be written.
                throw e;
            }
        }

        if (!parseErrors.isEmpty()) {
            appendParseErrors(parseErrors);
        }

        updateWatermark(status, (long) files.size(), (long) sessions.size(), parseErrors);
        log.info("Ingested {} session(s) from {} (status={}); {} parse error(s).",
                sessions.size(), files.size(), status, parseErrors.size());
        return sessions;
    }

    /**
     * Idempotent single-transaction upsert for one session (R-025). Existing rows
     * are updated in place on their natural keys; new rows are inserted. A failure
     * rolls back the whole batch so there are never half-written records.
     *
     * <p>On {@code (session_id)} conflict, the existing task is updated; the task's
     * {@code task_id} is preserved so child rows (steps/solutions/usage) that key by
     * {@code task_id} stay consistent.
     */
    private void upsertSession(IngestedSession session) {
        TaskEntity task = taskRepository.findBySessionId(session.getSessionId())
                .map(existing -> applySession(existing, session))
                .orElseGet(() -> createTask(session));
        task = taskRepository.save(task);

        upsertSteps(task, session);
        upsertSolutions(task, session);
        upsertUsage(task, session);
    }

    private TaskEntity applySession(TaskEntity existing, IngestedSession session) {
        existing.setName(session.getName() != null
                ? session.getName() : existing.getName());
        existing.setStartedAt(session.getStartedAt() != null
                ? session.getStartedAt() : existing.getStartedAt());
        existing.setFinishedAt(session.getFinishedAt());
        existing.setStatus(parser.deriveStatus(session));
        existing.setUpdatedAt(LocalDateTime.now());
        return existing;
    }

    private TaskEntity mergeTask(TaskEntity existing) {
        return existing;
    }

    private TaskEntity createTask(IngestedSession session) {
        TaskEntity task = new TaskEntity();
        task.setTaskId(session.getTaskId());
        task.setSessionId(session.getSessionId());
        task.setName(session.getName());
        task.setStartedAt(session.getStartedAt());
        task.setFinishedAt(session.getFinishedAt());
        task.setStatus(parser.deriveStatus(session));
        task.setSourcePath(session.getSourcePath());
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return task;
    }

    private void upsertSteps(TaskEntity task, IngestedSession session) {
        for (IngestedSession.IngestedStep step : session.getSteps()) {
            Optional<StepEntity> existing = stepRepository
                    .findByTaskIdAndMessageId(task.getTaskId(), step.getMessageId());
            StepEntity stepEntity = existing.orElseGet(() -> {
                StepEntity created = new StepEntity();
                created.setTaskId(task.getTaskId());
                created.setMessageId(step.getMessageId());
                created.setAt(step.getAt());
                created.setText(step.getText());
                return created;
            });
            stepEntity.setAt(step.getAt() != null
                    ? step.getAt() : stepEntity.getAt());
            stepEntity.setText(step.getText() != null
                    ? step.getText() : stepEntity.getText());
            stepRepository.save(stepEntity);
        }
    }

    private void upsertSolutions(TaskEntity task, IngestedSession session) {
        // Structured solutions from the file.
        for (IngestedSession.IngestedSolution sol : session.getSolutions()) {
            upsertOneSolution(task, sol, "structured");
        }
        // Extractor solutions (chunk 5). Best-effort; never throws (R-028).
        Optional<List<IngestedSession.IngestedSolution>> extracted;
        try {
            extracted = extractor.extract(session);
        } catch (Exception e) {
            log.warn("Solution extraction skipped for {}: {}",
                    session.getSessionId(), e.getMessage());
            return;
        }
        if (extracted.isPresent()) {
            for (IngestedSession.IngestedSolution sol : extracted.get()) {
                upsertOneSolution(task, sol, "llm:" + sol.getKind() + "@extracted");
            }
        }
    }

    private void upsertOneSolution(TaskEntity task, IngestedSession.IngestedSolution sol,
                                   String extractedBy) {
        String kind = normalizeKind(sol.getKind());
        Optional<SolutionEntity> existing = solutionRepository
                .findByTaskIdAndKindAndTextAndSourceMessageId(
                        task.getTaskId(), kind, sol.getText(), sol.getSourceMessageId());
        SolutionEntity entity = existing.orElseGet(() -> {
            SolutionEntity created = new SolutionEntity(
                    task.getTaskId(), kind, sol.getText(), sol.getSourceMessageId());
            created.setExtractedBy(extractedBy);
            return created;
        });
        solutionRepository.save(entity);
    }

    private String normalizeKind(String kind) {
        if (kind == null) {
            return "proposed";
        }
        String lower = kind.trim().toLowerCase();
        if (lower.contains("chosen")) {
            return "chosen";
        }
        return "proposed";
    }

    private void upsertUsage(TaskEntity task, IngestedSession session) {
        List<IngestedSession.IngestedUsage> usages = session.getUsages();
        // No per-usage entries: nothing to write (contracts §3, S-05 «нет данных»).
        if (usages.isEmpty()) {
            return;
        }
        // Exactly one step: key on it (per-message semantics).
        if (session.getSteps().size() == 1) {
            for (IngestedSession.IngestedUsage usage : usages) {
                upsertOneUsage(task, session.getSteps().get(0).getMessageId(), usage, "message");
            }
            return;
        }
        // No steps at all: store a single session-level row (no double counting).
        if (session.getSteps().isEmpty()) {
            upsertOneUsage(task, null, usages.get(0), "session");
            return;
        }
        // Otherwise: per-message rows, keyed on the first message id present.
        String messageId = session.getSteps().get(0).getMessageId();
        for (IngestedSession.IngestedUsage usage : usages) {
            upsertOneUsage(task, messageId, usage, "message");
        }
    }

    private void upsertOneUsage(TaskEntity task, String messageId,
                                IngestedSession.IngestedUsage usage, String scope) {
        Optional<TokenUsageEntity> existing = tokenUsageRepository
                .findByTaskIdAndMessageIdAndModel(task.getTaskId(), messageId, usage.getModel());
        TokenUsageEntity entity = existing.orElseGet(() -> new TokenUsageEntity(
                task.getTaskId(), messageId, usage.getModel(),
                usage.getInputTokens(), usage.getOutputTokens(),
                usage.getCacheTokens(), scope));
        entity.setInputTokens(usage.getInputTokens());
        entity.setOutputTokens(usage.getOutputTokens());
        entity.setCacheTokens(usage.getCacheTokens());
        tokenUsageRepository.save(entity);
    }

    /**
     * Compute the current {@link SourceStatus} without any DB access.
     * <ul>
     *   <li>{@link SourceStatus#UNAVAILABLE} when the directory is missing or
     *       contains no readable JSON;</li>
     *   <li>{@link SourceStatus#STALE} when the newest file hasn't changed for
     *       more than {@code 3 * pollIntervalMs} (contracts §3);</li>
     *   <li>{@link SourceStatus#AVAILABLE} otherwise (files present and fresh).</li>
     * </ul>
     */
    public SourceStatus computeSourceStatus(long pollIntervalMs) {
        if (!Files.isDirectory(dataDir)) {
            return SourceStatus.UNAVAILABLE;
        }
        List<Long> mtimes = readJsonFiles().stream()
                .map(this::mtimeOrNull)
                .filter(java.util.Objects::nonNull)
                .toList();
        if (mtimes.isEmpty()) {
            return SourceStatus.UNAVAILABLE;
        }
        long newest = mtimes.stream().max(Long::compareTo).orElse(0L);
        long staleThresholdMs = 3L * pollIntervalMs;
        if (System.currentTimeMillis() - newest > staleThresholdMs) {
            return SourceStatus.STALE;
        }
        return SourceStatus.AVAILABLE;
    }

    private Long mtimeOrNull(Path file) {
        try {
            if (!Files.isRegularFile(file)) {
                return null;
            }
            return Files.getLastModifiedTime(file).toInstant().toEpochMilli();
        } catch (Exception e) {
            log.warn("Could not read mtime {}: {}", file, e.getMessage());
            return null;
        }
    }

    /** List readable JSON files in the source dir (source is read-only). */
    public List<Path> readJsonFiles() {
        try {
            if (!Files.isDirectory(dataDir)) {
                return List.of();
            }
            return Files.list(dataDir)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".json"))
                    .sorted(Comparator.comparing(p -> p))
                    .toList();
        } catch (Exception e) {
            log.warn("Could not read source directory {}: {}", dataDir, e.getMessage());
            return List.of();
        }
    }

    /** Update the watermark keys (R-025 / ADR-0001 §2). Upsert-style, never duplicates. */
    private void updateWatermark(SourceStatus status, long fileCount, long sessionCount,
                                 List<String> parseErrors) {
        upsert(WK_MTIME, String.valueOf(nowEpochMillis()));
        upsert(WK_SYNC, nowIso());
        upsert(WK_COUNT, String.valueOf(fileCount) + " / " + sessionCount);
        upsert(WK_SOURCE_STATUS, status.name());
        upsert(WK_LAST_INGEST, nowIso());
        if (!parseErrors.isEmpty()) {
            appendParseErrors(parseErrors);
        }
    }

    private void appendParseErrors(List<String> errors) {
        String existing = ingestStateRepository.findByKey(WK_PARSE)
                .map(IngestStateEntity::getValue).orElse("");
        List<String> all = new ArrayList<>();
        if (existing != null && !existing.isBlank()) {
            java.util.Arrays.stream(existing.split("\\R"))
                    .filter(s -> !s.isBlank())
                    .forEach(all::add);
        }
        for (String error : errors) {
            all.add(nowIso() + " | " + error);
        }
        if (all.size() > 100) {
            all = all.subList(Math.max(0, all.size() - 100), all.size());
        }
        upsert(WK_PARSE, String.join("\n", all));
    }

    private void upsert(String key, String value) {
        Optional<IngestStateEntity> existing = ingestStateRepository.findByKey(key);
        if (existing.isPresent()) {
            existing.get().setValue(value);
            ingestStateRepository.save(existing.get());
        } else {
            ingestStateRepository.save(new IngestStateEntity(key, value));
        }
    }

    private static long nowEpochMillis() {
        return System.currentTimeMillis();
    }

    private static String nowIso() {
        return LocalDateTime.now().format(TS_FMT);
    }

    public Path getDataDir() {
        return dataDir;
    }
}
