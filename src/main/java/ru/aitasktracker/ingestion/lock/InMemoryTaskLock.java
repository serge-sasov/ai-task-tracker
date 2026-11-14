package ru.aitasktracker.ingestion.lock;

import org.springframework.context.annotation.Profile;

import ru.aitasktracker.ingestion.domain.IngestedSession;

/**
 * In-memory single-worker lock used when the application runs without Redis
 * (tests, and environments where {@code spring.data-redis} is not configured).
 *
 * <p>This is <em>not</em> truly distributed — across JVMs it only guarantees a
 * single worker <em>inside this JVM</em> (the Spring Boot singleton) — which is
 * sufficient for local runs and unit tests. The production bean is the
 * {@code @Bean TaskLock} declared in {@code IngestAutoConfiguration} (Redis when a
 * host is configured, this in-memory instance otherwise). The class is not a
 * Spring component so it is not registered twice as a lock.
 */
@Profile("!test")
public class InMemoryTaskLock implements TaskLock {

    private boolean held = false;

    @Override
    public synchronized boolean tryAcquire(long leaseSeconds) {
        if (held) {
            return false;
        }
        held = true;
        return true;
    }

    @Override
    public synchronized void release() {
        held = false;
    }

    @Override
    public synchronized boolean isHeld() {
        return held;
    }
}
