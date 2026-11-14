package ru.aitasktracker.ingestion.lock;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Redis-backed single-worker lock using the {@code SET key value NX EX} pattern
 * (Letuce, in the pom). A cycle acquires the lock once per poll; if acquisition
 * fails — another worker holds it, or Redis is unreachable — the cycle is
 * skipped gracefully (contracts §3, R-026) and ingestion does not crash.
 *
 * <p>Registered as the sole {@code TaskLock} bean via {@code @Bean taskLock()}
 * in {@code IngestAutoConfiguration}. The class is not a Spring component so it
 * is not registered twice as a lock.
 */
public class RedisTaskLock implements TaskLock {

    private static final Logger log = LoggerFactory.getLogger(RedisTaskLock.class);

    private static final String LOCK_KEY = "aitasktracker:ingestion:lock";

    private final RedisTemplate<String, Object> redisTemplate;

    private volatile boolean held = false;

    public RedisTaskLock(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean tryAcquire(long leaseSeconds) {
        if (held) {
            return true;
        }
        try {
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(LOCK_KEY, "1", Duration.ofSeconds(leaseSeconds));
            if (Boolean.TRUE.equals(acquired)) {
                held = true;
                return true;
            }
            log.debug("Ingestion lock held by another worker; skipping cycle.");
            return false;
        } catch (Exception e) {
            log.warn("Could not acquire ingestion lock (Redis unavailable): {}; skipping cycle.",
                    e.getMessage());
            return false;
        }
    }

    @Override
    public void release() {
        if (!held) {
            return;
        }
        try {
            redisTemplate.delete(LOCK_KEY);
        } catch (Exception e) {
            log.warn("Could not release ingestion lock: {}", e.getMessage());
        } finally {
            held = false;
        }
    }

    @Override
    public boolean isHeld() {
        return held;
    }
}
