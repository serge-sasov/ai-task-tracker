package ru.aitasktracker.api.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import ru.aitasktracker.api.exception.ServiceUnavailableException;
import ru.aitasktracker.api.dto.HealthResponse;
import ru.aitasktracker.domain.entity.IngestStateEntity;
import ru.aitasktracker.domain.repository.IngestStateRepository;

/**
 * Unit tests for {@link HealthService}. Pure unit tests: JdbcTemplate,
 * StringRedisTemplate and OllamaChecker are all Mockito mocks. No real
 * PostgreSQL/Redis/Ollama (pom has neither). Covers contracts section 4.1 and 5:
 * PG or Redis down yields {@code ServiceUnavailableException}; Ollama down yields
 * {@code degraded}; everything healthy yields {@code ok}.
 */
class HealthServiceTest {

    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    private final IngestStateRepository ingestStateRepository =
            mock(IngestStateRepository.class);
    private final OllamaChecker ollamaChecker = mock(OllamaChecker.class);

    private HealthService service() {
        return new HealthService(jdbc, redis, ingestStateRepository, ollamaChecker);
    }

    private static IngestStateEntity wm(String value) {
        IngestStateEntity e = new IngestStateEntity();
        e.setValue(value);
        return e;
    }

    private void okDependencies() {
        when(jdbc.queryForObject(anyString(), any(Class.class)))
                .thenReturn(1);
        when(redis.execute(any(RedisCallback.class))).thenReturn("PONG");
        when(ollamaChecker.isUp()).thenReturn(true);
    }

    @Test
    void healthyWhenAllDependenciesUp() {
        okDependencies();
        when(ingestStateRepository.findByKey(anyString()))
                .thenReturn(Optional.of(wm("AVAILABLE")));

        HealthResponse r = service().check();

        Assertions.assertEquals("ok", r.getStatus());
        Assertions.assertEquals("ok", r.getChecks().getPostgres());
        Assertions.assertEquals("ok", r.getChecks().getRedis());
        Assertions.assertEquals("up", r.getOllamaStatus());
        Assertions.assertEquals("available", r.getSourceStatus());
    }

    @Test
    void degradedWhenOllamaDownButPgAndRedisOk() {
        okDependencies();
        when(ollamaChecker.isUp()).thenReturn(false);
        when(ingestStateRepository.findByKey(anyString())).thenReturn(Optional.empty());

        HealthResponse r = service().check();

        Assertions.assertEquals("degraded", r.getStatus());
        Assertions.assertEquals("down", r.getOllamaStatus());
    }

    @Test
    void unavailabilityWhenPostgresDown() {
        when(jdbc.queryForObject(anyString(), any(Class.class)))
                .thenThrow(new IllegalStateException("pg down"));
        when(redis.execute(any(RedisCallback.class))).thenReturn("PONG");
        when(ollamaChecker.isUp()).thenReturn(true);

        assertThrows(ServiceUnavailableException.class, () -> service().check());
    }

    @Test
    void unavailabilityWhenRedisDown() {
        when(jdbc.queryForObject(anyString(), any(Class.class)))
                .thenReturn(1);
        when(redis.execute(any(RedisCallback.class)))
                .thenThrow(new IllegalStateException("redis down"));
        when(ollamaChecker.isUp()).thenReturn(true);

        assertThrows(ServiceUnavailableException.class, () -> service().check());
    }

    @Test
    void sourceStatusDefaultsWhenWatermarkMissing() {
        okDependencies();
        when(ingestStateRepository.findByKey("source_status"))
                .thenReturn(Optional.empty());
        when(ingestStateRepository.findByKey("last_ingest_at"))
                .thenReturn(Optional.empty());

        HealthResponse r = service().check();

        Assertions.assertEquals("unavailable", r.getSourceStatus());
        Assertions.assertNull(r.getLastIngestAt());
    }

    @Test
    void lastIngestAtRoundTripsIsoLocalDateTime() {
        okDependencies();
        when(ingestStateRepository.findByKey("source_status"))
                .thenReturn(Optional.of(wm("AVAILABLE")));
        when(ingestStateRepository.findByKey("last_ingest_at"))
                .thenReturn(Optional.of(wm("2026-11-10T09:30:00")));

        HealthResponse r = service().check();

        Assertions.assertEquals("2026-11-10T09:30:00", r.getLastIngestAt());
    }

    private static void assertThrows(
            Class<? extends RuntimeException> type, Runnable body) {
        try {
            body.run();
        } catch (RuntimeException e) {
            Assertions.assertTrue(type.isInstance(e));
            return;
        }
        Assertions.fail("Expected " + type.getSimpleName() + " to be thrown");
    }
}
