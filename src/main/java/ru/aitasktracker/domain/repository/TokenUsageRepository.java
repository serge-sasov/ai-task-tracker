package ru.aitasktracker.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ru.aitasktracker.domain.entity.TokenUsageEntity;

import java.util.List;
import java.util.Optional;

/**
 * Token usage repository (data model {@code token_usage}), keyed by
 * {@code (task_id, message_id, model, scope)} (contracts §2 / ADR-0001 §4).
 */

/**
 * Token usage repository (data model {@code token_usage}), keyed by
 * {@code (task_id, message_id, model, scope)} (contracts §2 / ADR-0001 §4).
 */
@Repository
public interface TokenUsageRepository extends JpaRepository<TokenUsageEntity, Long> {

    List<TokenUsageEntity> findByTaskId(String taskId);

    void deleteByTaskId(String taskId);

    long countByTaskId(String taskId);

    /**
     * Look up an existing token_usage row by its natural key (contracts §3 / §2):
     * {@code (task_id, message_id, model, scope)}. {@code scope} is the entity id
     * and needs no explicit predicate.
     */
    Optional<TokenUsageEntity> findByTaskIdAndMessageIdAndModel(
            String taskId, String messageId, String model);

    /**
     * Aggregate token totals for a single model across the selected task set.
     * Optional {@code from}/{@code to} window on the session {@code started_at}
     * (contracts §4.6).
     */
    @Query("""
            select sum(t.inputTokens), sum(t.outputTokens), sum(t.cacheTokens)
            from TokenUsageEntity t
            where t.model = :model
            and t.taskId in :taskIds
            """)
    long[] aggregateByModel(
            @Param("model") String model,
            @Param("taskIds") List<String> taskIds);
}
