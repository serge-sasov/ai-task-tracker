package ru.aitasktracker.ingestion.lock;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the Redis-backed single-worker lock. No real Redis is required:
 * {@link RedisTemplate} and its {@link ValueOperations} are mocked with Mockito.
 */
class RedisTaskLockTest {

    private final RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
    private final ValueOperations<String, Object> valueOps = mock(ValueOperations.class);

    @Test
    void acquireSuccess() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(true);

        RedisTaskLock lock = new RedisTaskLock(redisTemplate);
        assertTrue(lock.tryAcquire(60));
        assertTrue(lock.isHeld());
        verify(valueOps).setIfAbsent(any(), any(), any(Duration.class));
    }

    @Test
    void acquireFailsWhenAlreadyHeld() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(false);

        RedisTaskLock lock = new RedisTaskLock(redisTemplate);
        assertFalse(lock.tryAcquire(60));
        assertFalse(lock.isHeld());
    }

    @Test
    void acquireFailsGracefullyWhenRedisDown() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent(any(), any(), any(Duration.class)))
                .thenThrow(new RuntimeException("redis down"));

        RedisTaskLock lock = new RedisTaskLock(redisTemplate);
        // Must not throw out; must report not-held.
        assertFalse(lock.tryAcquire(60));
        assertFalse(lock.isHeld());
    }

    @Test
    void releaseDeletesKeyAndResets() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(true);

        RedisTaskLock lock = new RedisTaskLock(redisTemplate);
        assertTrue(lock.tryAcquire(60));
        assertTrue(lock.isHeld());

        lock.release();

        verify(redisTemplate).delete(anyString());
        assertFalse(lock.isHeld());
        verify(valueOps, times(1)).setIfAbsent(any(), any(), any(Duration.class));
    }

    @Test
    void releaseIsNoOpWhenNotHeld() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(true);

        RedisTaskLock lock = new RedisTaskLock(redisTemplate);
        // No prior acquire: release must not touch Redis.
        lock.release();

        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void tryAcquireNoOpWhenAlreadyHeldInJvm() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(true);

        RedisTaskLock lock = new RedisTaskLock(redisTemplate);
        assertTrue(lock.tryAcquire(60));
        // Second attempt while held returns true without hitting Redis again.
        assertTrue(lock.tryAcquire(60));
        verify(valueOps, times(1)).setIfAbsent(any(), any(), any(Duration.class));
    }
}
