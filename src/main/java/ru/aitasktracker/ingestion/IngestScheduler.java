package ru.aitasktracker.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ru.aitasktracker.ingestion.lock.TaskLock;

/**
 * Scheduled poller (contracts §3): invokes {@link IngestService#ingestAll()} on a
 * fixed cadence derived from {@code tracker.ingest-interval-seconds}. A
 * {@link TaskLock} guards each cycle so only one worker ingests at a time; if the
 * lock cannot be acquired — another worker holds it, or Redis is unreachable —
 * the cycle is skipped gracefully without error (R-026).
 */
@Component
public class IngestScheduler {

    private static final Logger log = LoggerFactory.getLogger(IngestScheduler.class);

    private final IngestService ingestService;
    private final TaskLock taskLock;
    private final int intervalSeconds;

    public IngestScheduler(IngestService ingestService, TaskLock taskLock,
                           @Value("${tracker.ingest-interval-seconds:60}") int intervalSeconds) {
        this.ingestService = ingestService;
        this.taskLock = taskLock;
        this.intervalSeconds = intervalSeconds;
    }

    @Scheduled(fixedDelayString = "${tracker.ingest-interval-seconds:60}000",
            initialDelayString = "${tracker.ingest-interval-seconds:60}000")
    public void poll() {
        boolean acquired = taskLock.tryAcquire(Math.max(60, intervalSeconds));
        if (!acquired) {
            return;
        }
        try {
            ingestService.ingestAll(intervalSeconds);
        } catch (RuntimeException e) {
            log.error("Ingestion cycle failed: {}", e.getMessage());
        } finally {
            taskLock.release();
        }
    }
}
