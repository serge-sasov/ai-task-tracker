package ru.aitasktracker.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ru.aitasktracker.domain.TaskStatus;
import ru.aitasktracker.domain.entity.TaskEntity;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Task repository (data model {@code tasks}). Supports pagination (contracts §1) and
 * lookup by external id / natural key for idempotent upsert (contracts §3).
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
     * Combined nullable filter for the task list endpoint (contracts §4.2). Any of
     * {@code from}/{@code to}/{@code status} may be {@code null}, in which case that
     * dimension is not filtered. Results are ordered by {@code started_at} descending
     * (contracts §4.2). The count counterpart is {@link #countFilter(LocalDateTime,
     * LocalDateTime, TaskStatus)}.
     */
    @Query("""
            select t from TaskEntity t
            where (:from is null or t.startedAt >= :from)
              and (:to is null or t.startedAt <= :to)
              and (:status is null or t.status = :status)
            order by t.startedAt desc
            """)
    Page<TaskEntity> filter(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("status") TaskStatus status,
            Pageable pageable);

    /** Total matching rows for {@link #filter(LocalDateTime, LocalDateTime, TaskStatus, Pageable)}. */
    @Query("""
            select count(t) from TaskEntity t
            where (:from is null or t.startedAt >= :from)
              and (:to is null or t.startedAt <= :to)
              and (:status is null or t.status = :status)
            """)
    long countFilter(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("status") TaskStatus status);
}
