package ru.aitasktracker.api.tokens.dto;

/**
 * Aggregate token totals for a period (contracts §4.6, API-002
 * TokenStatsResponse.total). Totals are always present and zeroed when the
 * period has no data, so the widget is never broken (contracts §4.6, S-05).
 */
public class TokenTotals {

    private final long inputTokens;
    private final long outputTokens;
    private final long totalTokens;

    public TokenTotals(long inputTokens, long outputTokens, long totalTokens) {
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.totalTokens = totalTokens;
    }

    public long getInputTokens() {
        return inputTokens;
    }

    public long getOutputTokens() {
        return outputTokens;
    }

    public long getTotalTokens() {
        return totalTokens;
    }
}
