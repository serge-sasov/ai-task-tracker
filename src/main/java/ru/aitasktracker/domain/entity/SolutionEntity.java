package ru.aitasktracker.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * A proposed or chosen solution derived from the session. Data model {@code solutions},
 * ADR-0001 §3. At most one {@code kind='chosen'} row per task (contracts §2).
 * Keyed by {@code (task_id, kind, text, source_message_id)} (data-model UNIQUE).
 */
@Entity
@Table(
        name = "solutions",
        indexes = {
                @Index(name = "idx_solutions_task", columnList = "task_id"),
                @Index(name = "idx_solutions_task_kind_text_source",
                        columnList = "task_id, kind, text, source_message_id", unique = true)
        })
public class SolutionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "solution_id")
    private Long solutionId;

    @Column(name = "task_id", nullable = false)
    private String taskId;

    @Column(name = "kind", nullable = false, length = 16)
    private String kind;

    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "source_message_id")
    private String sourceMessageId;

    @Column(name = "extracted_by")
    private String extractedBy;

    public SolutionEntity() {
    }

    public SolutionEntity(String taskId, String kind, String text, String sourceMessageId) {
        this.taskId = taskId;
        this.kind = kind;
        this.text = text;
        this.sourceMessageId = sourceMessageId;
    }

    public Long getSolutionId() {
        return solutionId;
    }

    public void setSolutionId(Long solutionId) {
        this.solutionId = solutionId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSourceMessageId() {
        return sourceMessageId;
    }

    public void setSourceMessageId(String sourceMessageId) {
        this.sourceMessageId = sourceMessageId;
    }

    public String getExtractedBy() {
        return extractedBy;
    }

    public void setExtractedBy(String extractedBy) {
        this.extractedBy = extractedBy;
    }
}
