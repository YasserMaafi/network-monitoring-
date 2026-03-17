package com.networkdevicemonitor.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Toolbar with action buttons and controls.
 */
public class ToolbarPanel extends JPanel {
    private final JButton buttonScanNetwork;
    private final JButton buttonRefreshSelected;
    private final JButton buttonPingSelected;
    private final JButton buttonViewDetails;
    private final JButton buttonStartMonitoring;
    private final JButton buttonStopMonitoring;
    private final JLabel labelMonitoringStatus;

    public interface ToolbarListener {
        void onScanNetworkClicked();
        void onRefreshSelectedClicked();
        void onPingSelectedClicked();
        void onViewDetailsClicked();
        void onStartMonitoringClicked();
        void onStopMonitoringClicked();
    }

    private ToolbarListener listener;

    public ToolbarPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        setBorder(BorderFactory.createEtchedBorder());
        setPreferredSize(new Dimension(0, 45));

        // Buttons
        buttonScanNetwork = new JButton("Scan Network");
        buttonScanNetwork.addActionListener(e -> {
            if (listener != null) listener.onScanNetworkClicked();
        });
        add(buttonScanNetwork);

        add(new JSeparator(JSeparator.VERTICAL));

        buttonRefreshSelected = new JButton("Refresh Selected");
        buttonRefreshSelected.addActionListener(e -> {
            if (listener != null) listener.onRefreshSelectedClicked();
        });
        add(buttonRefreshSelected);

        buttonPingSelected = new JButton("Ping Selected");
        buttonPingSelected.addActionListener(e -> {
            if (listener != null) listener.onPingSelectedClicked();
        });
        add(buttonPingSelected);

        buttonViewDetails = new JButton("View Details");
        buttonViewDetails.addActionListener(e -> {
            if (listener != null) listener.onViewDetailsClicked();
        });
        add(buttonViewDetails);

        add(new JSeparator(JSeparator.VERTICAL));

        buttonStartMonitoring = new JButton("Start Monitoring");
        buttonStartMonitoring.addActionListener(e -> {
            if (listener != null) listener.onStartMonitoringClicked();
        });
        add(buttonStartMonitoring);

        buttonStopMonitoring = new JButton("Stop Monitoring");
        buttonStopMonitoring.setEnabled(false);
        buttonStopMonitoring.addActionListener(e -> {
            if (listener != null) listener.onStopMonitoringClicked();
        });
        add(buttonStopMonitoring);

        add(Box.createHorizontalGlue());

        labelMonitoringStatus = new JLabel("Status: Idle");
        add(labelMonitoringStatus);
    }

    public void setToolbarListener(ToolbarListener listener) {
        this.listener = listener;
    }

    public void setMonitoringActive(boolean active) {
        buttonStartMonitoring.setEnabled(!active);
        buttonStopMonitoring.setEnabled(active);
        labelMonitoringStatus.setText(active ? "Status: Monitoring" : "Status: Idle");
    }

    public void setScanning(boolean scanning) {
        buttonScanNetwork.setEnabled(!scanning);
        labelMonitoringStatus.setText(scanning ? "Status: Scanning..." : "Status: Idle");
    }

    public JButton getButtonScanNetwork() {
        return buttonScanNetwork;
    }

    public JButton getButtonRefreshSelected() {
        return buttonRefreshSelected;
    }

    public JButton getButtonPingSelected() {
        return buttonPingSelected;
    }

    public JButton getButtonViewDetails() {
        return buttonViewDetails;
    }

    public JButton getButtonStartMonitoring() {
        return buttonStartMonitoring;
    }

    public JButton getButtonStopMonitoring() {
        return buttonStopMonitoring;
    }
}
