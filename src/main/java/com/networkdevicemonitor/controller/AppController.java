package com.networkdevicemonitor.controller;

import com.networkdevicemonitor.models.LogEntry;
import com.networkdevicemonitor.models.NetworkDevice;
import com.networkdevicemonitor.models.DeviceStatus;
import com.networkdevicemonitor.services.*;
import com.networkdevicemonitor.ui.MainFrame;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Main application controller that orchestrates all services and UI updates.
 * Handles threading and ensures all UI updates happen on the EDT.
 */
public class AppController {
    private static final Logger logger = Logger.getLogger(AppController.class.getName());

    // Services
    private final PingService pingService;
    private final NetworkInterfaceService networkInterfaceService;
    private final SubnetScannerService subnetScannerService;
    private final MonitoringService monitoringService;
    private final DeviceRepository deviceRepository;
    private final LogService logService;

    // UI
    private MainFrame mainFrame;

    // Threading
    private final ExecutorService backgroundExecutor;
    private volatile boolean isScanning = false;

    public AppController() {
        // Initialize services
        this.pingService = new PingService();
        this.networkInterfaceService = new NetworkInterfaceService();
        this.subnetScannerService = new SubnetScannerService(pingService);
        this.deviceRepository = new DeviceRepository();
        this.monitoringService = new MonitoringService(pingService, deviceRepository);
        this.logService = new LogService();

        this.backgroundExecutor = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "AppController-Worker");
            t.setDaemon(true);
            return t;
        });

        // Setup service listeners
        setupServiceListeners();

        logService.logInfo("Application started");
    }

    /**
     * Set the main UI frame (called by MainFrame after creation).
     */
    public void setMainFrame(MainFrame frame) {
        this.mainFrame = frame;

        // Load initial log entries
        frame.getLogPanel().setLogEntries(logService.getAllEntries());
    }

    /**
     * Get device repository for UI access.
     */
    public DeviceRepository getDeviceRepository() {
        return deviceRepository;
    }

    /**
     * Scan the local network for devices.
     * Runs in background thread.
     */
    public void scanNetwork() {
        if (isScanning) {
            logService.logWarning("Scan already in progress");
            JOptionPane.showMessageDialog(mainFrame,
                    "A scan is already in progress. Please wait.", "Scan In Progress",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        backgroundExecutor.submit(this::performNetworkScan);
    }

    private void performNetworkScan() {
        isScanning = true;
        updateToolbarScanState();

        logService.logInfo("Network scan started");

        try {
            // Detect ALL local networks (for multi-subnet support)
            List<NetworkInterfaceService.NetworkInfo> allNetworks =
                    networkInterfaceService.detectAllLocalNetworks();

            if (allNetworks.isEmpty()) {
                logService.logError("Could not detect any local network interface");
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(mainFrame,
                            "Could not detect local network. Check your network configuration.",
                            "Network Detection Failed", JOptionPane.ERROR_MESSAGE)
                );
                return;
            }

            logService.logSuccess("Detected " + allNetworks.size() + " network interface(s)");

            // Scan all local subnets
            List<String> allDiscoveredIps = new ArrayList<>();
            int totalAddresses = 0;

            for (NetworkInterfaceService.NetworkInfo networkInfo : allNetworks) {
                logService.logInfo("Scanning network: " + networkInfo);

                // Generate addresses to scan for this subnet
                List<String> addressesToScan = networkInterfaceService
                        .generateSubnetAddresses(networkInfo);

                totalAddresses += addressesToScan.size();
                logService.logInfo("  - Scanning " + addressesToScan.size() + " addresses in subnet");

                // Perform subnet scan
                List<String> discoveredIps = subnetScannerService.scanSubnet(addressesToScan,
                        new SubnetScannerService.ScanListener() {
                            @Override
                            public void onDeviceDiscovered(String ipAddress, String hostname) {
                                NetworkDevice device = deviceRepository.getOrCreateDevice(ipAddress);
                                device.setHostname(hostname);
                                device.setStatus(DeviceStatus.ONLINE);
                                logService.logSuccess("Device discovered: " + ipAddress + " (" + hostname + ")");
                            }

                            @Override
                            public void onScanProgress(int current, int total) {
                                // Optional: Could update a progress bar if needed
                            }

                            @Override
                            public void onScanComplete(List<String> discoveredIps) {
                                // Logged per-subnet
                            }
                        });

                allDiscoveredIps.addAll(discoveredIps);
            }

            logService.logSuccess("Scan complete. Scanned " + totalAddresses + " addresses, found " +
                    allDiscoveredIps.size() + " device(s)");
            updateTableFromRepository();

        } catch (Exception e) {
            logger.severe("Error during network scan: " + e.getMessage());
            logService.logError("Scan failed: " + e.getMessage());
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(mainFrame,
                        "Scan failed: " + e.getMessage(), "Scan Error",
                        JOptionPane.ERROR_MESSAGE)
            );
        } finally {
            isScanning = false;
            updateToolbarScanState();
        }
    }

    /**
     * Start continuous monitoring of known devices.
     */
    public void startMonitoring() {
        logService.logInfo("Monitoring started");
        monitoringService.startMonitoring();
        updateMonitoringState();
    }

    /**
     * Stop continuous monitoring.
     */
    public void stopMonitoring() {
        logService.logInfo("Monitoring stopped");
        monitoringService.stopMonitoring();
        updateMonitoringState();
    }

    /**
     * Refresh a specific device by IP.
     */
    public void refreshDevice(String ipAddress) {
        backgroundExecutor.submit(() -> {
            logService.logInfo("Refreshing device: " + ipAddress);
            monitoringService.pingDeviceNow(ipAddress);
        });
    }

    /**
     * Perform an immediate ping of a device and show result.
     */
    public void pingDevice(String ipAddress) {
        backgroundExecutor.submit(() -> {
            logService.logInfo("Manual ping to: " + ipAddress);
            var result = pingService.ping(ipAddress);

            String message;
            if (result.isReachable()) {
                message = "Success: " + result.getLatencyMs() + " ms";
                logService.logSuccess("Ping to " + ipAddress + ": " + result.getLatencyMs() + " ms");
            } else {
                message = "Timeout or unreachable";
                logService.logWarning("Ping to " + ipAddress + " failed");
            }

            String finalMessage = message;
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(mainFrame,
                        "Ping result: " + finalMessage, "Ping Result",
                        JOptionPane.INFORMATION_MESSAGE)
            );

            // Update device in repository
            NetworkDevice device = deviceRepository.getDevice(ipAddress);
            if (device != null) {
                if (result.isReachable()) {
                    device.recordSuccessfulPing(result.getLatencyMs());
                } else {
                    device.recordFailedPing();
                }
                deviceRepository.notifyDeviceUpdated(device);
            }
        });
    }

    /**
     * Update the table from repository data.
     */
    private void updateTableFromRepository() {
        SwingUtilities.invokeLater(() -> {
            mainFrame.getTablePanel().refreshTable();
        });
    }

    /**
     * Setup listeners on services for UI updates.
     */
    private void setupServiceListeners() {
        // Device repository listener
        deviceRepository.addListener(new DeviceRepository.RepositoryListener() {
            @Override
            public void onDeviceAdded(NetworkDevice device) {
                updateTableFromRepository();
            }

            @Override
            public void onDeviceUpdated(NetworkDevice device) {
                updateTableFromRepository();
            }

            @Override
            public void onDeviceRemoved(NetworkDevice device) {
                updateTableFromRepository();
            }
        });

        // Monitoring service listener
        monitoringService.addListener(new MonitoringService.MonitoringListener() {
            @Override
            public void onDeviceStatusChanged(NetworkDevice device, DeviceStatus oldStatus,
                                             DeviceStatus newStatus) {
                if (newStatus == DeviceStatus.ONLINE) {
                    logService.logSuccess("Device came online: " + device.getIpAddress());
                } else if (newStatus == DeviceStatus.OFFLINE) {
                    logService.logWarning("Device went offline: " + device.getIpAddress());
                }
                updateTableFromRepository();
            }

            @Override
            public void onMonitoringStarted() {
                updateMonitoringState();
            }

            @Override
            public void onMonitoringStopped() {
                updateMonitoringState();
            }
        });

        // Log service listener
        logService.addListener(entry ->
            SwingUtilities.invokeLater(() ->
                mainFrame.getLogPanel().addLogEntry(entry)
            )
        );
    }

    private void updateMonitoringState() {
        SwingUtilities.invokeLater(() ->
            mainFrame.getToolbarPanel().setMonitoringActive(monitoringService.isMonitoring())
        );
    }

    private void updateToolbarScanState() {
        SwingUtilities.invokeLater(() ->
            mainFrame.getToolbarPanel().setScanning(isScanning)
        );
    }

    /**
     * Clean shutdown of the application.
     */
    public void shutdown() {
        logService.logInfo("Application shutting down");
        monitoringService.shutdown();
        backgroundExecutor.shutdown();
    }
}
