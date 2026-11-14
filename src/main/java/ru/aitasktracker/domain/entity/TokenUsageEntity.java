package ru.aitasktracker.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Per-message or per-session token accounting. Data model {@code token_usage},
 * ADR-0001 §4. Keyed by {@code (task_id, message_id, model, scope)} (data-model UNIQUE).
 */
@Entity
@Table(
        name = "token_usage",
        indexes = {
                @Index(name = "idx_token_usage_task", columnList = "task_id"),
                @Index(name = "idx_token_usage_task_msg_model_scope",
                        columnList = "task_id, message_id, model, scope", unique = true)
        })
public class TokenUsageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usage_id")
    private Long usageId;

    @Column(name = "task_id", nullable = false)
    private String taskId;

    @Column(name = "message_id")
    private String messageId;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "input_tokens", nullable = false)
    private long inputTokens = 0;

    @Column(name = "output_tokens", nullable = false)
    private long outputTokens = 0;

    @Column(name = "cache_tokens", nullable = false)
    private long cacheTokens = 0;

    @Column(name = "scope", nullable = false, length = 16)
    private String scope;

    public TokenUsageEntity() {
    }

    public TokenUsageEntity(String taskId, String messageId, String model,
                            long inputTokens, long outputTokens, long cacheTokens, String scope) {
        this.taskId = taskId;
        this.messageId = messageId;
        this.model = model;
        this.inputTokens = inputTokens;
        this.outputTokens = outputTokens;
        this.cacheTokens = cacheTokens;
        this.scope = scope;
    }

    public Long getUsageId() {
        return usageId;
    }

    public void setUsageId(Long usageId) {
        this.usageId = usageId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public long getInputTokens() {
        return inputTokens;
    }

    public void setInputTokens(long inputTokens) {
        this.inputTokens = inputTokens;
    }

    public long getOutputTokens() {
        return outputTokens;
    }

    public void setOutputTokens(long outputTokens) {
        this.outputTokens = outputTokens;
    }

    public long getCacheTokens() {
        return cacheTokens;
    }

    public void setCacheTokens(long cacheTokens) {
        this.cacheTokens = cacheTokens;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
