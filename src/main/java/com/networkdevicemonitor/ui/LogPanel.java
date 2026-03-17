package com.networkdevicemonitor.ui;

import com.networkdevicemonitor.models.LogEntry;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel for displaying application log entries.
 */
public class LogPanel extends JPanel {
    private final DefaultListModel<String> listModel;
    private final JList<String> logList;

    public LogPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Activity Log"));

        listModel = new DefaultListModel<>();
        logList = new JList<>(listModel);
        logList.setFont(new Font("Monospaced", Font.PLAIN, 11));

        JScrollPane scrollPane = new JScrollPane(logList);
        add(scrollPane, BorderLayout.CENTER);

        // Clear button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton clearButton = new JButton("Clear Log");
        clearButton.addActionListener(e -> clearLog());
        buttonPanel.add(clearButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Add a log entry to the display.
     */
    public void addLogEntry(LogEntry entry) {
        SwingUtilities.invokeLater(() -> {
            listModel.addElement(entry.toString());
            // Keep view at bottom
            logList.ensureIndexIsVisible(listModel.getSize() - 1);
        });
    }

    /**
     * Load initial log entries.
     */
    public void setLogEntries(List<LogEntry> entries) {
        SwingUtilities.invokeLater(() -> {
            listModel.clear();
            for (LogEntry entry : entries) {
                listModel.addElement(entry.toString());
            }
            if (!entries.isEmpty()) {
                logList.ensureIndexIsVisible(listModel.getSize() - 1);
            }
        });
    }

    /**
     * Clear all log entries.
     */
    public void clearLog() {
        listModel.clear();
    }
}
