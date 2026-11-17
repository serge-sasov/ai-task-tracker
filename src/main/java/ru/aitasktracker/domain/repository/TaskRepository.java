package ru.aitasktracker.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.aitasktracker.domain.TaskStatus;
import ru.aitasktracker.domain.entity.TaskEntity;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Task repository (data model {@code tasks}). Supports pagination (contracts В§1) and
 * lookup by external id / natural key for idempotent upsert (contracts В§3).
 */
@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, String> {

    Optional<TaskEntity> findBySessionId(String sessionId);

    boolean existsBySessionId(String sessionId);

    Page<TaskEntity> findByStatus(TaskStatus status, Pageable pageable);

    Page<TaskEntity> findByTaskIdContainingIgnoreCase(String partial, Pageable pageable);

    Page<TaskEntity> findByStartedAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);

    long countByStatus(TaskStatus status);

    long countByStartedAtBetween(LocalDateTime from, LocalDateTime to);

    /**
     * Combined nullable filter for the task list endpoint (contracts В§4.2). Any of
     * {@code from}/{@code to}/{@code status} may be {@code null}, in which case that
     * dimension is not filtered. Results are ordered by {@code started_at} descending
     * (contracts В§4.2). The count counterpart is {@link #countFilter(LocalDateTime,
     * LocalDateTime, TaskStatus)}.
     */
    Page<TaskEntity> findByStatusAndStartedAtBetween(
            TaskStatus status, LocalDateTime from, LocalDateTime to, Pageable pageable);

    long countByStatusAndStartedAtBetween(
            TaskStatus status, LocalDateTime from, LocalDateTime to);

    /**
     * Combined nullable filter for the task list endpoint (contracts 4.2). Avoids
     * nullable JPQL parameters (PostgreSQL cannot infer a type for {@code (:x is null)}
     * binds) by delegating to typed derived queries with wide default bounds.
     */
    default Page<TaskEntity> filter(LocalDateTime from, LocalDateTime to,
                                    TaskStatus status, Pageable pageable) {
        LocalDateTime min = LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime max = LocalDateTime.of(9999, 12, 31, 23, 59, 59);
        if (from == null && to == null) {
            return status == null ? findAll(pageable) : findByStatus(status, pageable);
        }
        LocalDateTime lo = from == null ? min : from;
        LocalDateTime hi = to == null ? max : to;
        return status == null
                ? findByStartedAtBetween(lo, hi, pageable)
                : findByStatusAndStartedAtBetween(status, lo, hi, pageable);
    }

    /** Total matching rows for {@link #filter(LocalDateTime, LocalDateTime, TaskStatus, Pageable)}. */
    default long countFilter(LocalDateTime from, LocalDateTime to, TaskStatus status) {
        LocalDateTime min = LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime max = LocalDateTime.of(9999, 12, 31, 23, 59, 59);
        if (from == null && to == null) {
            return status == null ? count() : countByStatus(status);
        }
        LocalDateTime lo = from == null ? min : from;
        LocalDateTime hi = to == null ? max : to;
        return status == null
                ? countByStartedAtBetween(lo, hi)
                : countByStatusAndStartedAtBetween(status, lo, hi);
    }
}
