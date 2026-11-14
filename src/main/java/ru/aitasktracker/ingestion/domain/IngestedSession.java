package ru.aitasktracker.ingestion.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Normalised in-memory representation of one opencode session, produced by the
 * {@code IngestJsonParser} regardless of input shape ({@code actions} fixture vs
 * {@code messages} opencode). This is the internal model the parser maps into
 * (contracts §3).
 */
public final class IngestedSession {

    private String sessionId;
    private String taskId;
    private String name;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private String rawStatus;
    private String sourcePath;
    private final List<IngestedStep> steps = new ArrayList<>();
    private final List<IngestedSolution> solutions = new ArrayList<>();
    private final List<IngestedUsage> usages = new ArrayList<>();

    public static final class IngestedStep {
        private final String messageId;
        private final LocalDateTime at;
        private final String text;

        IngestedStep(String messageId, LocalDateTime at, String text) {
            this.messageId = messageId;
            this.at = at;
            this.text = text;
        }

        public String getMessageId() {
            return messageId;
        }

        public LocalDateTime getAt() {
            return at;
        }

        public String getText() {
            return text;
        }
    }

    public static final class IngestedSolution {
        private final String kind;
        private final String text;
        private final String sourceMessageId;

        IngestedSolution(String kind, String text, String sourceMessageId) {
            this.kind = kind;
            this.text = text;
            this.sourceMessageId = sourceMessageId;
        }

        public String getKind() {
            return kind;
        }

        public String getText() {
            return text;
        }

        public String getSourceMessageId() {
            return sourceMessageId;
        }
    }

    public static final class IngestedUsage {
        private final String model;
        private final long inputTokens;
        private final long outputTokens;
        private final long cacheTokens;

        IngestedUsage(String model, long inputTokens, long outputTokens, long cacheTokens) {
            this.model = model;
            this.inputTokens = inputTokens;
            this.outputTokens = outputTokens;
            this.cacheTokens = cacheTokens;
        }

        public String getModel() {
            return model;
        }

        public long getInputTokens() {
            return inputTokens;
        }

        public long getOutputTokens() {
            return outputTokens;
        }

        public long getCacheTokens() {
            return cacheTokens;
        }
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public String getRawStatus() {
        return rawStatus;
    }

    public void setRawStatus(String rawStatus) {
        this.rawStatus = rawStatus;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public List<IngestedStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    /**
     * Package-private mutation used by {@link IngestJsonParser} (same package).
     * Keeps {@link #getSteps()} unmodifiable for callers while still allowing the
     * parser to populate the backing list.
     */
    void addStep(IngestedStep step) {
        steps.add(step);
    }

    public List<IngestedSolution> getSolutions() {
        return Collections.unmodifiableList(solutions);
    }

    void addSolution(IngestedSolution solution) {
        solutions.add(solution);
    }

    public List<IngestedUsage> getUsages() {
        return Collections.unmodifiableList(usages);
    }

    void addUsage(IngestedUsage usage) {
        usages.add(usage);
    }

    public Optional<IngestedUsage> anyUsage() {
        return usages.stream().findFirst();
    }
}
