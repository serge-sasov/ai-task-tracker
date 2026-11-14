package ru.aitasktracker.api.dto;

import java.util.List;

/**
 * Task detail payload (contracts §4.3 / API-001): the task fields plus its
 * chronological {@code steps}, its {@code solutions} block, and the
 * {@code tokens} block.
 */
public class TaskDetailResponse {

    private final String taskId;
    private final String name;
    private final String startedAt;
    private final String finishedAt;
    private final String status;
    private final long durationS;
    private final List<StepItem> steps;
    private final SolutionsBlock solutions;
    private final TokensBlock tokens;

    public TaskDetailResponse(String taskId, String name, String startedAt,
                              String finishedAt, String status, long durationS,
                              List<StepItem> steps, SolutionsBlock solutions,
                              TokensBlock tokens) {
        this.taskId = taskId;
        this.name = name;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.status = status;
        this.durationS = durationS;
        this.steps = steps;
        this.solutions = solutions;
        this.tokens = tokens;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getName() {
        return name;
    }

    public String getStartedAt() {
        return startedAt;
    }

    public String getFinishedAt() {
        return finishedAt;
    }

    public String getStatus() {
        return status;
    }

    public long getDurationS() {
        return durationS;
    }

    public List<StepItem> getSteps() {
        return steps;
    }

    public SolutionsBlock getSolutions() {
        return solutions;
    }

    public TokensBlock getTokens() {
        return tokens;
    }

    /**
     * {@code solutions} block: the proposed list plus the chosen one (may be
     * {@code null}). Empty when a task has no solutions.
     */
    public static class SolutionsBlock {
        private final List<SolutionItem> proposed;
        private final SolutionItem chosen;

        public SolutionsBlock(List<SolutionItem> proposed, SolutionItem chosen) {
            this.proposed = proposed;
            this.chosen = chosen;
        }

        public List<SolutionItem> getProposed() {
            return proposed;
        }

        public SolutionItem getChosen() {
            return chosen;
        }
    }

    /**
     * {@code tokens} block: per-model rows, totals and the {@code has_data} flag.
     */
    public static class TokensBlock {
        private final List<TokenByModel> byModel;
        private final TokenTotals total;
        private final boolean hasData;

        public TokensBlock(List<TokenByModel> byModel, TokenTotals total, boolean hasData) {
            this.byModel = byModel;
            this.total = total;
            this.hasData = hasData;
        }

        public List<TokenByModel> getByModel() {
            return byModel;
        }

        public TokenTotals getTotal() {
            return total;
        }

        public boolean hasData() {
            return hasData;
        }
    }
}
