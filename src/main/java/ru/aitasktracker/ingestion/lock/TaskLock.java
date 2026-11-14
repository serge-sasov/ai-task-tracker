package ru.aitasktracker.ingestion.lock;

/**
 * A minimal distributed-lock abstraction used to guarantee a single worker
 * ingests at a time (contracts §3, R-026).
 *
 * <p>The production implementation is backed by Redis (Lettuce). It is designed
 * as a thin seam so tests can assert acquisition/release and graceful no-op on
 * failure without real Redis infrastructure.
 */
public interface TaskLock {

    /**
     * Try to acquire the lock.
     *
     * @param leaseSeconds how long the lock should be held before it expires,
     *                     guarding against a stuck worker releasing later workers
     * @return {@code true} when the lock was acquired; {@code false} when it could
     *          not (another worker holds it, or the backend is unavailable)
     */
    boolean tryAcquire(long leaseSeconds);

    /**
     * Release the lock previously acquired with {@link #tryAcquire(long)}. No-op
     * when the lock is not held. Never throws — a failed release must not break
     * the poll cycle.
     */
    void release();

    /** {@code true} when the lock is currently held. */
    boolean isHeld();
}
