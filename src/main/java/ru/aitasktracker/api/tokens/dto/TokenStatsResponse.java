package ru.aitasktracker.api.tokens.dto;

import java.util.List;

/**
 * Response body for {@code GET /api/tokens} (contracts §4.6, API-002
 * TokenStatsResponse). {@code period} echoes the resolved period label
 * ({@code all} / {@code YYYY-MM-DD} / {@code from..to}), {@code has_data} marks
 * whether the period carried any usage (contracts §4.6, S-05).
 */
public class TokenStatsResponse {

    private final String period;
    private final List<TokenByModel> byModel;
    private final TokenTotals total;
    private final boolean hasData;

    public TokenStatsResponse(String period, List<TokenByModel> byModel,
                              TokenTotals total, boolean hasData) {
        this.period = period;
        this.byModel = byModel;
        this.total = total;
        this.hasData = hasData;
    }

    public String getPeriod() {
        return period;
    }

    public List<TokenByModel> getByModel() {
        return byModel;
    }

    public TokenTotals getTotal() {
        return total;
    }

    public boolean isHasData() {
        return hasData;
    }
}
