package com.networkdevicemonitor.services;

import com.networkdevicemonitor.models.NetworkDevice;
import com.networkdevicemonitor.models.DeviceStatus;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for continuous monitoring of known devices.
 * Periodically pings devices and updates their status.
 * All monitoring runs in background threads.
 */
public class MonitoringService {
    private static final Logger logger = Logger.getLogger(MonitoringService.class.getName());
    private static final int OFFLINE_THRESHOLD = 3; // Mark offline after 3 consecutive failures
    private static final long DEFAULT_INTERVAL_SECONDS = 5;

    private final PingService pingService;
    private final DeviceRepository deviceRepository;
    private final ScheduledExecutorService scheduler;
    private long intervalSeconds = DEFAULT_INTERVAL_SECONDS;
    private volatile boolean isMonitoring = false;

    // Listener for monitoring events
    public interface MonitoringListener {
        void onDeviceStatusChanged(NetworkDevice device, DeviceStatus oldStatus, DeviceStatus newStatus);
        void onMonitoringStarted();
        void onMonitoringStopped();
    }

    private final List<MonitoringListener> listeners = Collections.synchronizedList(new ArrayList<>());

    public MonitoringService(PingService pingService, DeviceRepository deviceRepository) {
        this.pingService = pingService;
        this.deviceRepository = deviceRepository;
        this.scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "MonitoringService-Thread");
            t.setDaemon(true);
            return t;
        });
    }

    public void addListener(MonitoringListener listener) {
        listeners.add(listener);
    }

    public void removeListener(MonitoringListener listener) {
        listeners.remove(listener);
    }

    public void setIntervalSeconds(long seconds) {
        this.intervalSeconds = Math.max(1, seconds);
        logger.info("Monitoring interval set to " + intervalSeconds + " seconds");
    }

    public long getIntervalSeconds() {
        return intervalSeconds;
    }

    public boolean isMonitoring() {
        return isMonitoring;
    }

    /**
     * Start continuous monitoring of all known devices.
     */
    public void startMonitoring() {
        if (isMonitoring) {
            logger.warning("Monitoring already running");
            return;
        }

        isMonitoring = true;
        logger.info("Starting device monitoring");

        scheduler.scheduleAtFixedRate(
                this::performMonitoringCycle,
                1, // Initial delay
                intervalSeconds,
                TimeUnit.SECONDS
        );

        notifyMonitoringStarted();
    }

    /**
     * Stop continuous monitoring.
     */
    public void stopMonitoring() {
        if (!isMonitoring) {
            return;
        }

        isMonitoring = false;
        scheduler.shutdownNow();
        try {
            if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                logger.warning("Scheduler didn't terminate");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        logger.info("Stopped device monitoring");
        notifyMonitoringStopped();

        // Restart scheduler for future calls
        // (can't restart a shutdown scheduler)
    }

    /**
     * Perform a single monitoring cycle - ping all known devices.
     * Runs in background thread pool.
     */
    private void performMonitoringCycle() {
        try {
            List<NetworkDevice> devices = deviceRepository.getAllDevices();

            for (NetworkDevice device : devices) {
                try {
                    var result = pingService.ping(device.getIpAddress());

                    DeviceStatus oldStatus = device.getStatus();

                    if (result.isReachable()) {
                        device.recordSuccessfulPing(result.getLatencyMs());
                        if (oldStatus != DeviceStatus.ONLINE) {
                            notifyStatusChanged(device, oldStatus, DeviceStatus.ONLINE);
                            logger.info("Device came online: " + device.getIpAddress());
                        }
                    } else {
                        device.recordFailedPing();
                        if (device.getConsecutiveFailures() >= OFFLINE_THRESHOLD) {
                            device.markOffline();
                            if (oldStatus != DeviceStatus.OFFLINE) {
                                notifyStatusChanged(device, oldStatus, DeviceStatus.OFFLINE);
                                logger.info("Device went offline: " + device.getIpAddress());
                            }
                        }
                    }

                    // Notify update
                    deviceRepository.notifyDeviceUpdated(device);

                } catch (Exception e) {
                    logger.log(Level.FINE, "Error monitoring device " + device.getIpAddress(), e);
                }
            }

        } catch (Exception e) {
            logger.log(Level.WARNING, "Error during monitoring cycle", e);
        }
    }

    /**
     * Ping a specific device immediately.
     * Assumes device is already in repository. Can be called from any thread.
     */
    public void pingDeviceNow(String ipAddress) {
        scheduler.submit(() -> {
            try {
                NetworkDevice device = deviceRepository.getDevice(ipAddress);
                if (device == null) {
                    logger.warning("Device not found: " + ipAddress);
                    return;
                }

                var result = pingService.ping(ipAddress);
                DeviceStatus oldStatus = device.getStatus();

                if (result.isReachable()) {
                    device.recordSuccessfulPing(result.getLatencyMs());
                    if (oldStatus != DeviceStatus.ONLINE) {
                        notifyStatusChanged(device, oldStatus, DeviceStatus.ONLINE);
                    }
                } else {
                    device.recordFailedPing();
                    if (device.getConsecutiveFailures() >= OFFLINE_THRESHOLD) {
                        device.markOffline();
                        if (oldStatus != DeviceStatus.OFFLINE) {
                            notifyStatusChanged(device, oldStatus, DeviceStatus.OFFLINE);
                        }
                    }
                }

                deviceRepository.notifyDeviceUpdated(device);

            } catch (Exception e) {
                logger.log(Level.WARNING, "Error pinging device " + ipAddress, e);
            }
        });
    }

    private void notifyStatusChanged(NetworkDevice device, DeviceStatus oldStatus, DeviceStatus newStatus) {
        for (MonitoringListener listener : listeners) {
            listener.onDeviceStatusChanged(device, oldStatus, newStatus);
        }
    }

    private void notifyMonitoringStarted() {
        for (MonitoringListener listener : listeners) {
            listener.onMonitoringStarted();
        }
    }

    private void notifyMonitoringStopped() {
        for (MonitoringListener listener : listeners) {
            listener.onMonitoringStopped();
        }
    }

    /**
     * Gracefully shutdown all resources.
     */
    public void shutdown() {
        stopMonitoring();
        scheduler.shutdownNow();
    }
}
