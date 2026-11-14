package ru.aitasktracker.api.tokens.dto;

/**
 * One per-model token row for {@code GET /api/tokens} (contracts §4.6, API-002
 * TokenByModel). {@code tokens_total} is input + output + cache (contracts §4.3
 * semantics reused here).
 */
public class TokenByModel {

    private final String model;
    private final long inputTokens;
    private final long outputTokens;
    private final long cacheTokens;
    private final long totalTokens;

    public TokenByModel(String model, long inputTokens, long outputTokens,
                        long cacheTokens, long totalTokens) {
        this.model = model;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.cacheTokens = cacheTokens;
        this.totalTokens = totalTokens;
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

    public long getTotalTokens() {
        return totalTokens;
    }
}
