package ru.aitasktracker.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persists {@link TaskStatus} as its canonical RU string ({@link TaskStatus#value()}),
 * matching the {@code tasks_status_check} constraint, instead of the enum name.
 */
@Converter
public class TaskStatusConverter implements AttributeConverter<TaskStatus, String> {

    @Override
    public String convertToDatabaseColumn(TaskStatus attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public TaskStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return TaskStatus.fromValue(dbData).orElse(null);
    }
}
