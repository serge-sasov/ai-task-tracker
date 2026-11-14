package ru.aitasktracker.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Ingestion watermark + diagnostics key/value store. Data model {@code ingest_state},
 * ADR-0001 §2/R-025/R-026. Keyed by {@code key} (e.g. {@code last_file_mtime},
 * {@code last_sync_at}, {@code last_file_count}, {@code parse_errors}).
 */
@Entity
@Table(name = "ingest_state")
public class IngestStateEntity {

    @Id
    @Column(name = "key")
    private String key;

    @Column(name = "value", columnDefinition = "TEXT")
    private String value;

    public IngestStateEntity() {
    }

    public IngestStateEntity(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
