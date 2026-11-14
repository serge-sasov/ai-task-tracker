package ru.aitasktracker.ingestion;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import ru.aitasktracker.ingestion.lock.TaskLock;

/**
 * Unit tests for {@link IngestScheduler}, covering the lock-guard contract
 * (contracts §3, R-026): the scheduler acquires a {@link TaskLock} before every
 * ingest cycle, skips the cycle without ingesting or releasing when the lock is
 * unavailable, still releases on failure, and floors the lease at 60 seconds.
 */
class IngestSchedulerTest {

    private final IngestService ingestService = Mockito.mock(IngestService.class);
    private final TaskLock taskLock = Mockito.mock(TaskLock.class);
    private final IngestScheduler scheduler = new IngestScheduler(ingestService, taskLock, 60);

    @Test
    void pollAcquiredIngestsAndReleases() {
        when(taskLock.tryAcquire(60L)).thenReturn(true);

        scheduler.poll();

        verify(taskLock, times(1)).tryAcquire(60L);
        verify(ingestService, times(1)).ingestAll(60);
        verify(taskLock, times(1)).release();
    }

    @Test
    void pollDeniedSkipsIngestAndRelease() {
        when(taskLock.tryAcquire(60L)).thenReturn(false);

        scheduler.poll();

        verify(ingestService, never()).ingestAll(anyInt());
        verify(taskLock, never()).release();
    }

    @Test
    void pollFailureStillReleases() {
        when(taskLock.tryAcquire(60L)).thenReturn(true);
        Mockito.doThrow(new RuntimeException("boom"))
                .when(ingestService).ingestAll(60);

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(scheduler::poll);

        verify(taskLock, times(1)).release();
    }

    @Test
    void pollFloorsLeaseAtSixtySeconds() {
        IngestScheduler small = new IngestScheduler(ingestService, taskLock, 30);
        when(taskLock.tryAcquire(60L)).thenReturn(true);

        small.poll();

        verify(taskLock).tryAcquire(60L);
    }
}
