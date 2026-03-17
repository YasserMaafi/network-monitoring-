package com.networkdevicemonitor.models;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents a discovered network device with monitoring statistics.
 * Thread-safe for concurrent access from monitoring threads.
 */
public class NetworkDevice {
    private static final int MAX_LATENCY_HISTORY = 100;

    private final String ipAddress;
    private volatile String hostname;
    private volatile DeviceStatus status;
    private volatile long latestLatencyMs;
    private volatile LocalDateTime lastSeenTime;
    private volatile LocalDateTime firstSeenTime;
    private volatile int consecutiveFailures;

    // Thread-safe collections
    private final List<Long> latencyHistory = Collections.synchronizedList(new ArrayList<>());
    private volatile int successfulChecks;
    private volatile int failedChecks;

    public NetworkDevice(String ipAddress) {
        this.ipAddress = ipAddress;
        this.hostname = "Unknown";
        this.status = DeviceStatus.UNKNOWN;
        this.latestLatencyMs = -1;
        this.lastSeenTime = LocalDateTime.now();
        this.firstSeenTime = LocalDateTime.now();
        this.consecutiveFailures = 0;
        this.successfulChecks = 0;
        this.failedChecks = 0;
    }

    // Getters
    public String getIpAddress() {
        return ipAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public long getLatestLatencyMs() {
        return latestLatencyMs;
    }

    public long getAverageLatencyMs() {
        if (latencyHistory.isEmpty()) {
            return -1;
        }
        return latencyHistory.stream().mapToLong(Long::longValue).sum() / latencyHistory.size();
    }

    public long getMinLatencyMs() {
        if (latencyHistory.isEmpty()) {
            return -1;
        }
        return latencyHistory.stream().mapToLong(Long::longValue).min().orElse(-1);
    }

    public long getMaxLatencyMs() {
        if (latencyHistory.isEmpty()) {
            return -1;
        }
        return latencyHistory.stream().mapToLong(Long::longValue).max().orElse(-1);
    }

    public LocalDateTime getLastSeenTime() {
        return lastSeenTime;
    }

    public LocalDateTime getFirstSeenTime() {
        return firstSeenTime;
    }

    public int getConsecutiveFailures() {
        return consecutiveFailures;
    }

    public int getSuccessfulChecks() {
        return successfulChecks;
    }

    public int getFailedChecks() {
        return failedChecks;
    }

    public List<Long> getLatencyHistory() {
        return new ArrayList<>(latencyHistory);
    }

    // Setters
    public void setHostname(String hostname) {
        if (hostname != null && !hostname.isEmpty()) {
            this.hostname = hostname;
        }
    }

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }

    /**
     * Records a successful ping result.
     */
    public void recordSuccessfulPing(long latencyMs) {
        this.latestLatencyMs = latencyMs;
        this.status = DeviceStatus.ONLINE;
        this.lastSeenTime = LocalDateTime.now();
        this.consecutiveFailures = 0;
        this.successfulChecks++;

        // Maintain capped history
        latencyHistory.add(latencyMs);
        if (latencyHistory.size() > MAX_LATENCY_HISTORY) {
            latencyHistory.remove(0);
        }
    }

    /**
     * Records a failed ping attempt.
     */
    public void recordFailedPing() {
        this.consecutiveFailures++;
        this.failedChecks++;
        this.latestLatencyMs = -1;
        // Status only changes to OFFLINE after multiple consecutive failures
    }

    /**
     * Mark device as offline after configurable consecutive failures.
     * Typically called after consecutiveFailures reaches a threshold.
     */
    public void markOffline() {
        this.status = DeviceStatus.OFFLINE;
    }

    /**
     * Reset consecutive failure counter.
     */
    public void resetConsecutiveFailures() {
        this.consecutiveFailures = 0;
    }

    @Override
    public String toString() {
        return String.format("NetworkDevice{ip=%s, hostname=%s, status=%s, latency=%dms}",
                ipAddress, hostname, status, latestLatencyMs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NetworkDevice device = (NetworkDevice) o;
        return Objects.equals(ipAddress, device.ipAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ipAddress);
    }
}
