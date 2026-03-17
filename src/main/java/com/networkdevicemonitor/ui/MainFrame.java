package com.networkdevicemonitor.ui;

import com.networkdevicemonitor.controller.AppController;
import com.networkdevicemonitor.models.NetworkDevice;
import javax.swing.*;
import java.awt.*;

/**
 * Main application frame. Orchestrates the layout and components.
 */
public class MainFrame extends JFrame {
    private final AppController appController;
    private final ToolbarPanel toolbarPanel;
    private final DeviceTablePanel tablePanel;
    private final DeviceDetailsPanel detailsPanel;
    private final LogPanel logPanel;

    public MainFrame(AppController appController) {
        this.appController = appController;

        setTitle("Network Device Monitor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        // Create components
        toolbarPanel = new ToolbarPanel();
        tablePanel = new DeviceTablePanel(appController.getDeviceRepository());
        detailsPanel = new DeviceDetailsPanel();
        logPanel = new LogPanel();

        // Setup layout
        setLayout(new BorderLayout());
        add(toolbarPanel, BorderLayout.NORTH);

        // Center: split pane with table and details
        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                tablePanel, detailsPanel);
        centerSplit.setDividerLocation(700);
        centerSplit.setResizeWeight(0.7);

        // Main: split pane with center and log panel
        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                centerSplit, logPanel);
        mainSplit.setDividerLocation(500);
        mainSplit.setResizeWeight(0.8);

        add(mainSplit, BorderLayout.CENTER);

        // Wire listeners
        setupListeners();

        setVisible(true);
    }

    private void setupListeners() {
        // Toolbar listener
        toolbarPanel.setToolbarListener(new ToolbarPanel.ToolbarListener() {
            @Override
            public void onScanNetworkClicked() {
                appController.scanNetwork();
            }

            @Override
            public void onRefreshSelectedClicked() {
                NetworkDevice selected = tablePanel.getSelectedDevice();
                if (selected != null) {
                    appController.refreshDevice(selected.getIpAddress());
                }
            }

            @Override
            public void onPingSelectedClicked() {
                NetworkDevice selected = tablePanel.getSelectedDevice();
                if (selected != null) {
                    appController.pingDevice(selected.getIpAddress());
                }
            }

            @Override
            public void onViewDetailsClicked() {
                // Details are already shown in the panel
                NetworkDevice selected = tablePanel.getSelectedDevice();
                if (selected != null) {
                    detailsPanel.displayDevice(selected);
                }
            }

            @Override
            public void onStartMonitoringClicked() {
                appController.startMonitoring();
            }

            @Override
            public void onStopMonitoringClicked() {
                appController.stopMonitoring();
            }
        });

        // Table selection listener
        tablePanel.setSelectionListener(device -> {
            detailsPanel.displayDevice(device);
        });

        // Register with app controller
        appController.setMainFrame(this);
    }

    // Accessors for app controller to update UI
    public DeviceTablePanel getTablePanel() {
        return tablePanel;
    }

    public DeviceDetailsPanel getDetailsPanel() {
        return detailsPanel;
    }

    public LogPanel getLogPanel() {
        return logPanel;
    }

    public ToolbarPanel getToolbarPanel() {
        return toolbarPanel;
    }
}
