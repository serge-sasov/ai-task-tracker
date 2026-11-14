package ru.aitasktracker.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.aitasktracker.domain.entity.SolutionEntity;

import java.util.List;
import java.util.Optional;

/**
 * Solution repository (data model {@code solutions}). At most one {@code kind='chosen'}
 * row per task (contracts §2), keyed by {@code (task_id, kind, text, source_message_id)}.
 */
@Repository
public interface SolutionRepository extends JpaRepository<SolutionEntity, Long> {

    List<SolutionEntity> findByTaskIdOrderBySolutionIdAsc(String taskId);

    Optional<SolutionEntity> findByTaskIdAndKindAndText(String taskId, String kind, String text);

    long countByTaskIdAndKind(String taskId, String kind);

    void deleteByTaskId(String taskId);

    Optional<SolutionEntity> findByTaskIdAndKindAndTextAndSourceMessageId(
            String taskId, String kind, String text, String sourceMessageId);
}
