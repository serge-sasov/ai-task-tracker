package ru.aitasktracker.api.dto;

/**
 * A proposed or chosen solution row (contracts §4.3).
 */
public class SolutionItem {

    private final String text;
    private final String sourceMessageId;

    public SolutionItem(String text, String sourceMessageId) {
        this.text = text;
        this.sourceMessageId = sourceMessageId;
    }

    public String getText() {
        return text;
    }

    public String getSourceMessageId() {
        return sourceMessageId;
    }
}
