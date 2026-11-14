package ru.aitasktracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tracker")
public class TrackerProperties {

    private final Opencode opencode = new Opencode();
    private final Ollama ollama = new Ollama();
    private int ingestIntervalSeconds = 60;
    private int searchTopK = 5;

    public Opencode getOpencode() {
        return opencode;
    }

    public Ollama getOllama() {
        return ollama;
    }

    public int getIngestIntervalSeconds() {
        return ingestIntervalSeconds;
    }

    public void setIngestIntervalSeconds(int ingestIntervalSeconds) {
        this.ingestIntervalSeconds = ingestIntervalSeconds;
    }

    public int getSearchTopK() {
        return searchTopK;
    }

    public void setSearchTopK(int searchTopK) {
        this.searchTopK = searchTopK;
    }

    public static class Opencode {
        private String dataDir = "./data/opencode";

        public String getDataDir() {
            return dataDir;
        }

        public void setDataDir(String dataDir) {
            this.dataDir = dataDir;
        }
    }

    public static class Ollama {
        private String baseUrl = "http://localhost:11434";

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }
    }
}
