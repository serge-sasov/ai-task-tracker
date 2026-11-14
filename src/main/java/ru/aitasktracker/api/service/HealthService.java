package ru.aitasktracker.api.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import ru.aitasktracker.api.dto.HealthResponse;
import ru.aitasktracker.api.dto.HealthResponse.Checks;
import ru.aitasktracker.api.exception.ServiceUnavailableException;
import ru.aitasktracker.domain.entity.IngestStateEntity;
import ru.aitasktracker.domain.repository.IngestStateRepository;

/**
 * Readiness checks for {@code GET /api/health} (contracts section 4.1). Combines
 * a PostgreSQL ping via {@link JdbcTemplate}, a Redis ping via
 * {@code StringRedisTemplate}, the ingestion watermark keys
 * ({@code source_status} / {@code last_ingest_at}) read from the
 * {@code ingest_state} repository, and an {@link OllamaChecker}.
 *
 * <p>PostgreSQL or Redis down raises {@link ServiceUnavailableException} with the
 * error envelope body. Ollama down (with healthy PG/Redis) yields a 200 with
 * {@code status: "degraded"}; everything healthy yields {@code status: "ok"}.
 */
@Service
public class HealthService {

    private static final Logger log = LoggerFactory.getLogger(HealthService.class);

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final String KEY_SOURCE_STATUS = "source_status";
    private static final String KEY_LAST_INGEST = "last_ingest_at";
    private static final String DEFAULT_SOURCE_STATUS = "unavailable";

    private final JdbcTemplate jdbc;
    private final org.springframework.data.redis.core.StringRedisTemplate redis;
    private final IngestStateRepository ingestStateRepository;
    private final OllamaChecker ollamaChecker;

    public HealthService(JdbcTemplate jdbc,
                         org.springframework.data.redis.core.StringRedisTemplate redis,
                         IngestStateRepository ingestStateRepository,
                         OllamaChecker ollamaChecker) {
        this.jdbc = jdbc;
        this.redis = redis;
        this.ingestStateRepository = ingestStateRepository;
        this.ollamaChecker = ollamaChecker;
    }

    /**
     * Build the health response. PostgreSQL and Redis pings are performed first;
     * PG/Redis down raises {@link ServiceUnavailableException}. Ollama is optional
     * and only affects the {@code status} field (ok / degraded).
     */
    public HealthResponse check() {
        String postgres = postgresStatus();
        String redisStatus = redisStatus();
        boolean ok = "ok".equals(postgres) && "ok".equals(redisStatus);
        if (!ok) {
            throw new ServiceUnavailableException(ServiceUnavailableException.MESSAGE);
        }

        String ollama = ollamaChecker.isUp() ? "up" : "down";
        Checks checks = new Checks(postgres, redisStatus);
        return new HealthResponse(
                "up".equals(ollama) ? "ok" : "degraded",
                checks,
                sourceStatus(),
                ollama,
                lastIngestAt());
    }

    private String postgresStatus() {
        try {
            jdbc.queryForObject("SELECT 1", Integer.class);
            return "ok";
        } catch (Exception e) {
            log.warn("PostgreSQL health check failed: {}", e.getMessage());
            return "down";
        }
    }

    private String redisStatus() {
        try {
            String ping = redis.execute(
                    (org.springframework.data.redis.core.RedisCallback<String>)
                            connection -> {
                                String r = connection.ping();
                                return r;
                            });
            return "PONG".equalsIgnoreCase(ping) ? "ok" : "down";
        } catch (Exception e) {
            log.warn("Redis health check failed: {}", e.getMessage());
            return "down";
        }
    }

    /** Lower-case {@code source_status} stored in the watermark, defaulting to {@code unavailable}. */
    private String sourceStatus() {
        Optional<IngestStateEntity> state = ingestStateRepository.findByKey(KEY_SOURCE_STATUS);
        if (state.isEmpty()) {
            return DEFAULT_SOURCE_STATUS;
        }
        String value = state.get().getValue();
        if (value == null || value.isBlank()) {
            return DEFAULT_SOURCE_STATUS;
        }
        String upper = value.trim().toUpperCase();
        if ("AVAILABLE".equals(upper)) {
            return "available";
        }
        if ("STALE".equals(upper)) {
            return "stale";
        }
        return "unavailable";
    }

    /** The ISO 8601 local wall-clock {@code last_ingest_at} timestamp, or {@code null}. */
    private String lastIngestAt() {
        Optional<IngestStateEntity> state = ingestStateRepository.findByKey(KEY_LAST_INGEST);
        if (state.isEmpty()) {
            return null;
        }
        String value = state.get().getValue();
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.trim(), TS_FMT).format(TS_FMT);
        } catch (Exception e) {
            return null;
        }
    }
}
