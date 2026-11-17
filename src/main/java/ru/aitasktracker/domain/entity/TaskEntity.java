package ru.aitasktracker.domain.entity;

import ru.aitasktracker.domain.TaskStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Convert;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * A tracked opencode session (one task). Data model {@code tasks}, ADR-0001 В§3.
 *
 * <p>{@code task_id} is the source-provided id when present (seed uses {@code TASK-001}вЂ¦),
 * otherwise a stable derived id from {@code session_id}; uniqueness enforced by the PK
 * and by ingestion (natural key {@code session_id}).
 */
@Entity
@Table(name = "tasks")
public class TaskEntity {

    @Id
    @Column(name = "task_id")
    private String taskId;

    @Column(name = "session_id", nullable = false, unique = true)
    private String sessionId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Convert(converter = ru.aitasktracker.domain.TaskStatusConverter.class)
    @Column(name = "status", nullable = false, length = 32)
    private TaskStatus status;

    @Column(name = "source_path")
    private String sourcePath;

    @Column(name = "embedding", columnDefinition = "vector(1024)")
    private float[] embedding;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public TaskEntity() {
    }

    public TaskEntity(String taskId, String sessionId, String name,
                      LocalDateTime startedAt, LocalDateTime finishedAt,
                      TaskStatus status) {
        this.taskId = taskId;
        this.sessionId = sessionId;
        this.name = name;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.status = status;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
