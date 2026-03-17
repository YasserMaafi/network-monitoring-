package com.networkdevicemonitor.services;

import com.networkdevicemonitor.models.LogEntry;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Service for logging application events.
 * Maintains a bounded log of entries for UI display.
 */
public class LogService {
    private static final Logger logger = Logger.getLogger(LogService.class.getName());
    private static final int MAX_LOG_SIZE = 1000;

    private final List<LogEntry> entries = new CopyOnWriteArrayList<>();
    private final List<LogListener> listeners = Collections.synchronizedList(new ArrayList<>());

    public interface LogListener {
        void onLogEntryAdded(LogEntry entry);
    }

    public void addListener(LogListener listener) {
        listeners.add(listener);
    }

    public void removeListener(LogListener listener) {
        listeners.remove(listener);
    }

    /**
     * Add a log entry.
     */
    public void log(LogEntry.LogLevel level, String message) {
        LogEntry entry = new LogEntry(level, message);
        entries.add(entry);

        // Keep log bounded
        if (entries.size() > MAX_LOG_SIZE) {
            entries.remove(0);
        }

        logger.info("[" + level + "] " + message);

        // Notify listeners
        for (LogListener listener : listeners) {
            listener.onLogEntryAdded(entry);
        }
    }

    public void logInfo(String message) {
        log(LogEntry.LogLevel.INFO, message);
    }

    public void logWarning(String message) {
        log(LogEntry.LogLevel.WARNING, message);
    }

    public void logError(String message) {
        log(LogEntry.LogLevel.ERROR, message);
    }

    public void logSuccess(String message) {
        log(LogEntry.LogLevel.SUCCESS, message);
    }

    /**
     * Get all log entries as an unmodifiable list.
     */
    public List<LogEntry> getAllEntries() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    /**
     * Clear all log entries.
     */
    public void clear() {
        entries.clear();
    }

    public int size() {
        return entries.size();
    }
}
