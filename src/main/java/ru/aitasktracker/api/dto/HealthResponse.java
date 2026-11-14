package ru.aitasktracker.api.dto;

/**
 * Health response body (contracts section 4.1). Note the shape is governed by
 * contracts section 4.1, not by the OpenAPI API-004 spec (known divergence).
 */
public class HealthResponse {

    private final String status;
    private final Checks checks;
    private final String sourceStatus;
    private final String ollamaStatus;
    private final String lastIngestAt;

    public HealthResponse(String status, Checks checks, String sourceStatus,
                          String ollamaStatus, String lastIngestAt) {
        this.status = status;
        this.checks = checks;
        this.sourceStatus = sourceStatus;
        this.ollamaStatus = ollamaStatus;
        this.lastIngestAt = lastIngestAt;
    }

    public String getStatus() {
        return status;
    }

    public Checks getChecks() {
        return checks;
    }

    public String getSourceStatus() {
        return sourceStatus;
    }

    public String getOllamaStatus() {
        return ollamaStatus;
    }

    public String getLastIngestAt() {
        return lastIngestAt;
    }

    /** The nested {@code checks} object: {@code postgres} and {@code redis} are
     * each {@code ok} or {@code down}. */
    public static class Checks {
        private final String postgres;
        private final String redis;

        public Checks(String postgres, String redis) {
            this.postgres = postgres;
            this.redis = redis;
        }

        public String getPostgres() {
            return postgres;
        }

        public String getRedis() {
            return redis;
        }
    }
}
