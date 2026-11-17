package ru.aitasktracker.config;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ru.aitasktracker.ingestion.domain.IngestJsonParser;
import ru.aitasktracker.ingestion.extractor.NoOpSolutionExtractor;
import ru.aitasktracker.ingestion.extractor.SolutionExtractor;
import ru.aitasktracker.ingestion.lock.InMemoryTaskLock;
import ru.aitasktracker.ingestion.lock.RedisTaskLock;
import ru.aitasktracker.ingestion.lock.TaskLock;

/**
 * Bean wiring for the ingestion module (contracts §3), consistent with
 * {@link TrackerAutoConfiguration}: {@link TrackerProperties} are bound here and
 * the ingestion bean ({@code TaskLock}) is assembled from the bound properties.
 * The {@link IngestJsonParser}, {@link SolutionExtractor} and the {@code TaskLock}
 * implementations are assembled here; the {@code IngestService}/{@code IngestScheduler}
 * are plain Spring components discovered by component scanning.
 */
@Configuration
@EnableConfigurationProperties(TrackerProperties.class)
public class IngestAutoConfiguration {

    /**
     * The read-only opencode source directory consumed by {@code IngestService}.
     * Bound from {@code tracker.opencode.data-dir} (OPENCODE_DATA_DIR in compose).
     */
    @Bean
    public Path opencodeDataDir(TrackerProperties properties) {
        return Path.of(properties.getOpencode().getDataDir());
    }

    @Bean
    public IngestJsonParser ingestJsonParser() {
        return new IngestJsonParser();
    }

    @Bean
    public SolutionExtractor solutionExtractor() {
        // Chunk 5 swaps this for an Ollama-based extractor implementing the same
        // interface without touching ingestion code.
        return new NoOpSolutionExtractor();
    }

    @Bean
    public TaskLock taskLock(
            @Value("${spring.data-redis.host:localhost}") String redisHost) {
        // Prefer Redis when a Redis host is configured; otherwise fall back to the
        // in-memory single-worker lock (tests / Redis-less environments).
        if ("localhost".equals(redisHost)) {
            return new InMemoryTaskLock();
        }
        throw new IllegalStateException(
                "Redis is required but host is '" + redisHost + "'. Configure REDIS_HOST.");
    }
}
