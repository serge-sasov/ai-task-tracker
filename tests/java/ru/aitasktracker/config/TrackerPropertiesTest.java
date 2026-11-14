package ru.aitasktracker.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrackerPropertiesTest {

    @Test
    void defaultsMatchConfigSpec() {
        TrackerProperties props = new TrackerProperties();

        assertEquals(60, props.getIngestIntervalSeconds());
        assertEquals(5, props.getSearchTopK());
        assertEquals("./data/opencode", props.getOpencode().getDataDir());
        assertEquals("http://localhost:11434", props.getOllama().getBaseUrl());
    }

    @Test
    void settersChangeValues() {
        TrackerProperties props = new TrackerProperties();

        props.setIngestIntervalSeconds(30);
        props.setSearchTopK(10);
        props.getOpencode().setDataDir("/tmp/opencode");
        props.getOllama().setBaseUrl("http://ollama.example:11434");

        assertEquals(30, props.getIngestIntervalSeconds());
        assertEquals(10, props.getSearchTopK());
        assertEquals("/tmp/opencode", props.getOpencode().getDataDir());
        assertEquals("http://ollama.example:11434", props.getOllama().getBaseUrl());
    }
}
