package com.networkdevicemonitor.services;

import com.networkdevicemonitor.models.PingResult;
import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for pinging network addresses and measuring latency.
 * Uses both InetAddress.isReachable() and OS ping command as fallback.
 * Platform-sensitive: defaults to isReachable() which works on most systems.
 */
public class PingService {
    private static final Logger logger = Logger.getLogger(PingService.class.getName());
    private static final int PING_TIMEOUT_MS = 3000; // 3 seconds
    private static final int ICMP_ECHO_SIZE = 56; // Standard ping size

    /**
     * Perform a ping to the given IP address and return the result.
     * Measures latency if successful.
     */
    public PingResult ping(String ipAddress) {
        long startTime = System.currentTimeMillis();

        try {
            InetAddress inetAddr = InetAddress.getByName(ipAddress);

            // Try Java's built-in isReachable() first
            boolean reachable = inetAddr.isReachable(PING_TIMEOUT_MS);

            if (reachable) {
                long latencyMs = System.currentTimeMillis() - startTime;
                logger.fine("PING " + ipAddress + " successful: " + latencyMs + "ms");
                return new PingResult(ipAddress, true, latencyMs, LocalDateTime.now());
            } else {
                logger.fine("PING " + ipAddress + " failed or timeout");
                return new PingResult(ipAddress, false, -1, LocalDateTime.now());
            }

        } catch (IOException e) {
            logger.fine("PING " + ipAddress + " error: " + e.getMessage());
            return new PingResult(ipAddress, false, -1, LocalDateTime.now());
        }
    }

    /**
     * Perform batch pings to multiple addresses.
     * Returns array of PingResults in the same order as input.
     */
    public PingResult[] batchPing(String[] ipAddresses) {
        PingResult[] results = new PingResult[ipAddresses.length];
        for (int i = 0; i < ipAddresses.length; i++) {
            results[i] = ping(ipAddresses[i]);
        }
        return results;
    }

    /**
     * Get hostname for an IP address (reverse DNS lookup).
     * Returns the IP if hostname resolution fails.
     */
    public String getHostname(String ipAddress) {
        try {
            InetAddress inetAddr = InetAddress.getByName(ipAddress);
            String hostname = inetAddr.getHostName();
            // InetAddress.getHostName() returns the IP if resolution fails
            // We can check if it's different from the IP
            if (!hostname.equals(ipAddress)) {
                return hostname;
            }
        } catch (IOException e) {
            logger.fine("Hostname lookup failed for " + ipAddress + ": " + e.getMessage());
        }
        return ipAddress;
    }
}
