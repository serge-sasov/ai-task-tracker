package ru.aitasktracker.api.dto;

/**
 * Aggregate token totals for a task, plus a {@code has_data} flag that is true
 * when the task carries any token_usage rows (contracts §4.3).
 */
public class TokenTotals {

    private final long inputTokens;
    private final long outputTokens;
    private final long cacheTokens;
    private final long totalTokens;
    private final boolean hasData;

    public TokenTotals(long inputTokens, long outputTokens, long cacheTokens,
                       boolean hasData) {
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.cacheTokens = cacheTokens;
        this.totalTokens = inputTokens + outputTokens + cacheTokens;
        this.hasData = hasData;
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

    public long getTotalTokens() {
        return totalTokens;
    }

    public boolean hasData() {
        return hasData;
    }
}
