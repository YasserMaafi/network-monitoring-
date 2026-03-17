package com.networkdevicemonitor.ui;

import com.networkdevicemonitor.models.NetworkDevice;
import javax.swing.*;
import java.awt.*;

/**
 * Panel displaying detailed information about the selected device.
 */
public class DeviceDetailsPanel extends JPanel {
    private final JLabel labelIp;
    private final JLabel labelHostname;
    private final JLabel labelStatus;
    private final JLabel labelLatestLatency;
    private final JLabel labelAvgLatency;
    private final JLabel labelMinLatency;
    private final JLabel labelMaxLatency;
    private final JLabel labelSuccessfulChecks;
    private final JLabel labelFailedChecks;
    private final JLabel labelFirstSeen;
    private final JLabel labelLastSeen;

    public DeviceDetailsPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Device Details"));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayout(11, 2, 5, 5));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create label pairs
        contentPanel.add(new JLabel("IP Address:"));
        labelIp = new JLabel("—");
        contentPanel.add(labelIp);

        contentPanel.add(new JLabel("Hostname:"));
        labelHostname = new JLabel("—");
        contentPanel.add(labelHostname);

        contentPanel.add(new JLabel("Status:"));
        labelStatus = new JLabel("—");
        contentPanel.add(labelStatus);

        contentPanel.add(new JLabel("Latest Latency:"));
        labelLatestLatency = new JLabel("—");
        contentPanel.add(labelLatestLatency);

        contentPanel.add(new JLabel("Average Latency:"));
        labelAvgLatency = new JLabel("—");
        contentPanel.add(labelAvgLatency);

        contentPanel.add(new JLabel("Min Latency:"));
        labelMinLatency = new JLabel("—");
        contentPanel.add(labelMinLatency);

        contentPanel.add(new JLabel("Max Latency:"));
        labelMaxLatency = new JLabel("—");
        contentPanel.add(labelMaxLatency);

        contentPanel.add(new JLabel("Successful Checks:"));
        labelSuccessfulChecks = new JLabel("0");
        contentPanel.add(labelSuccessfulChecks);

        contentPanel.add(new JLabel("Failed Checks:"));
        labelFailedChecks = new JLabel("0");
        contentPanel.add(labelFailedChecks);

        contentPanel.add(new JLabel("First Seen:"));
        labelFirstSeen = new JLabel("—");
        contentPanel.add(labelFirstSeen);

        contentPanel.add(new JLabel("Last Seen:"));
        labelLastSeen = new JLabel("—");
        contentPanel.add(labelLastSeen);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Display the details of a device. Pass null to clear.
     */
    public void displayDevice(NetworkDevice device) {
        if (device == null) {
            clearDisplay();
            return;
        }

        labelIp.setText(device.getIpAddress());
        labelHostname.setText(device.getHostname());
        labelStatus.setText(device.getStatus().getDisplayName());
        labelLatestLatency.setText(formatLatency(device.getLatestLatencyMs()));
        labelAvgLatency.setText(formatLatency(device.getAverageLatencyMs()));
        labelMinLatency.setText(formatLatency(device.getMinLatencyMs()));
        labelMaxLatency.setText(formatLatency(device.getMaxLatencyMs()));
        labelSuccessfulChecks.setText(String.valueOf(device.getSuccessfulChecks()));
        labelFailedChecks.setText(String.valueOf(device.getFailedChecks()));
        labelFirstSeen.setText(device.getFirstSeenTime().toString());
        labelLastSeen.setText(device.getLastSeenTime().toString());
    }

    /**
     * Clear the details display.
     */
    public void clearDisplay() {
        labelIp.setText("—");
        labelHostname.setText("—");
        labelStatus.setText("—");
        labelLatestLatency.setText("—");
        labelAvgLatency.setText("—");
        labelMinLatency.setText("—");
        labelMaxLatency.setText("—");
        labelSuccessfulChecks.setText("0");
        labelFailedChecks.setText("0");
        labelFirstSeen.setText("—");
        labelLastSeen.setText("—");
    }

    private String formatLatency(long latencyMs) {
        if (latencyMs < 0) {
            return "—";
        }
        return latencyMs + " ms";
    }
}
