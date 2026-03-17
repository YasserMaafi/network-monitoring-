package com.networkdevicemonitor.models;

import java.time.LocalDateTime;

/**
 * Represents a single ping result with latency information.
 */
public class PingResult {
    private final String ipAddress;
    private final boolean reachable;
    private final long latencyMs; // -1 if unreachable
    private final LocalDateTime timestamp;

    public PingResult(String ipAddress, boolean reachable, long latencyMs, LocalDateTime timestamp) {
        this.ipAddress = ipAddress;
        this.reachable = reachable;
        this.latencyMs = latencyMs;
        this.timestamp = timestamp;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public boolean isReachable() {
        return reachable;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("PingResult{ip=%s, reachable=%s, latency=%dms, time=%s}",
                ipAddress, reachable, latencyMs, timestamp);
    }
}
