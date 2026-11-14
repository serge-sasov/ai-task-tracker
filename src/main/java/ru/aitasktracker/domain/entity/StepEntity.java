package ru.aitasktracker.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * A chronological session message. Data model {@code steps}, ADR-0001 §3.
 * Keyed by {@code (task_id, message_id)} (data-model UNIQUE).
 */
@Entity
@Table(
        name = "steps",
        indexes = {
                @Index(name = "idx_steps_task", columnList = "task_id"),
                @Index(name = "idx_steps_task_message", columnList = "task_id, message_id", unique = true)
        })
public class StepEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "step_id")
    private Long stepId;

    @Column(name = "task_id", nullable = false)
    private String taskId;

    @Column(name = "message_id", nullable = false)
    private String messageId;

    @Column(name = "at")
    private LocalDateTime at;

    @Column(name = "text")
    private String text;

    public StepEntity() {
    }

    public StepEntity(String taskId, String messageId, LocalDateTime at, String text) {
        this.taskId = taskId;
        this.messageId = messageId;
        this.at = at;
        this.text = text;
    }

    public Long getStepId() {
        return stepId;
    }

    public void setStepId(Long stepId) {
        this.stepId = stepId;
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

    public LocalDateTime getAt() {
        return at;
    }

    public void setAt(LocalDateTime at) {
        this.at = at;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
