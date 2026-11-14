package ru.aitasktracker.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.aitasktracker.domain.entity.IngestStateEntity;

import java.util.Optional;

/**
 * Ingestion state / watermark repository (data model {@code ingest_state}),
 * keyed by {@code key} (ADR-0001 §2, contracts §3).
 */
@Repository
public interface IngestStateRepository extends JpaRepository<IngestStateEntity, String> {

    Optional<IngestStateEntity> findByKey(String key);
}
