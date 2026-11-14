package ru.aitasktracker.ingestion.lock;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the in-memory single-worker lock (no Redis infra, pom has none).
 */
class InMemoryTaskLockTest {

    @Test
    void acquireSucceedsThenIsHeld() {
        InMemoryTaskLock lock = new InMemoryTaskLock();
        assertTrue(lock.tryAcquire(60));
        assertTrue(lock.isHeld());
    }

    @Test
    void secondAcquireFailsWhileHeld() {
        InMemoryTaskLock lock = new InMemoryTaskLock();
        assertTrue(lock.tryAcquire(60));
        assertFalse(lock.tryAcquire(60));
        assertTrue(lock.isHeld());
    }

    @Test
    void releaseThenReacquire() {
        InMemoryTaskLock lock = new InMemoryTaskLock();
        assertTrue(lock.tryAcquire(60));
        lock.release();
        assertFalse(lock.isHeld());
        assertTrue(lock.tryAcquire(60));
        assertTrue(lock.isHeld());
    }

    @Test
    void releaseIsNoOpWhenNotHeld() {
        InMemoryTaskLock lock = new InMemoryTaskLock();
        // No prior acquire: must not throw.
        lock.release();
        assertFalse(lock.isHeld());
    }
}
