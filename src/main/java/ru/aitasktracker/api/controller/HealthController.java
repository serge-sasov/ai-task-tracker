package ru.aitasktracker.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ru.aitasktracker.api.dto.HealthResponse;
import ru.aitasktracker.api.service.HealthService;

/**
 * Health endpoint (contracts section 4.1, API-004). The response shape follows
 * contracts section 4.1, not the OpenAPI API-004 spec (known divergence, ice-set):
 * {@code {"status","checks":{"postgres","redis"},"source_status","ollama_status",
 * "last_ingest_at"}}. A 503 is returned (envelope body) when PostgreSQL or Redis
 * is down; Ollama down yields a 200 with {@code status: "degraded"}.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping
    public HealthResponse health() {
        return healthService.check();
    }
}
