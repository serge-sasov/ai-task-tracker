package ru.aitasktracker.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ru.aitasktracker.domain.entity.StepEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Step repository (data model {@code steps}). Chronological session messages,
 * keyed by {@code (task_id, message_id)} (contracts section 3).
 */
@Repository
public interface StepRepository extends JpaRepository<StepEntity, Long> {

    List<StepEntity> findByTaskIdOrderByAtAsc(String taskId);

    Optional<StepEntity> findByTaskIdAndMessageId(String taskId, String messageId);

    long countByTaskId(String taskId);

    void deleteByTaskId(String taskId);

    /**
     * The latest {@code at} of any step for a task (contracts section 2:
     * {@code duration_s} uses {@code COALESCE(finished_at, last_step_at)}). Empty
     * when the task has no steps.
     */
    @Query("select max(s.at) from StepEntity s where s.taskId = :taskId")
    Optional<LocalDateTime> lastStepTime(@Param("taskId") String taskId);
}
