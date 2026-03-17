package com.networkdevicemonitor.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a log entry for application events.
 */
public class LogEntry {
    public enum LogLevel {
        INFO, WARNING, ERROR, SUCCESS
    }

    private final LogLevel level;
    private final String message;
    private final LocalDateTime timestamp;

    public LogEntry(LogLevel level, String message) {
        this.level = level;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return String.format("[%s] %s: %s", timestamp.format(formatter), level, message);
    }
}
