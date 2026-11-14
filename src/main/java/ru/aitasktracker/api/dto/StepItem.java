package ru.aitasktracker.api.dto;

/**
 * One step row of a task detail (contracts §4.3).
 */
public class StepItem {

    private final String messageId;
    private final String at;
    private final String text;

    public StepItem(String messageId, String at, String text) {
        this.messageId = messageId;
        this.at = at;
        this.text = text;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getAt() {
        return at;
    }

    public String getText() {
        return text;
    }
}
