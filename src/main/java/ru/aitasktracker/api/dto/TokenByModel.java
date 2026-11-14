package ru.aitasktracker.api.dto;

/**
 * Per-model token totals (contracts §4.3 / API-001 TokenByModel).
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
