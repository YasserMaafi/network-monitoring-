package com.networkdevicemonitor.services;

import com.networkdevicemonitor.models.PingResult;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for scanning a subnet and discovering reachable devices.
 * Performs parallel pings across a subnet using a thread pool.
 */
public class SubnetScannerService {
    private static final Logger logger = Logger.getLogger(SubnetScannerService.class.getName());
    private static final int THREAD_POOL_SIZE = 20; // Parallel ping threads

    private final PingService pingService;
    private volatile boolean scanInProgress = false;

    // Listener interface for scan progress
    public interface ScanListener {
        void onDeviceDiscovered(String ipAddress, String hostname);
        void onScanProgress(int current, int total);
        void onScanComplete(List<String> discoveredIps);
    }

    public SubnetScannerService(PingService pingService) {
        this.pingService = pingService;
    }

    public boolean isScanInProgress() {
        return scanInProgress;
    }

    /**
     * Scan a subnet and discover reachable devices.
     * This is a blocking call that runs on the calling thread.
     * Should be called from a background thread, not the EDT.
     */
    public List<String> scanSubnet(List<String> addressesToScan, ScanListener listener) {
        if (scanInProgress) {
            logger.warning("Scan already in progress");
            return new ArrayList<>();
        }

        scanInProgress = true;
        List<String> discoveredIps = new CopyOnWriteArrayList<>();

        try {
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            List<Future<?>> futures = new ArrayList<>();
            int total = addressesToScan.size();

            logger.info("Starting subnet scan for " + total + " addresses");

            // Submit all ping tasks
            for (int i = 0; i < addressesToScan.size(); i++) {
                String ip = addressesToScan.get(i);
                final int index = i;

                Future<?> future = executor.submit(() -> {
                    PingResult result = pingService.ping(ip);
                    if (result.isReachable()) {
                        String hostname = pingService.getHostname(ip);
                        discoveredIps.add(ip);
                        if (listener != null) {
                            listener.onDeviceDiscovered(ip, hostname);
                        }
                        logger.fine("Device discovered: " + ip + " (" + hostname + ")");
                    }
                    if (listener != null) {
                        listener.onScanProgress(index + 1, total);
                    }
                });

                futures.add(future);
            }

            // Wait for all tasks to complete
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    logger.log(Level.FINE, "Error during scan task", e);
                }
            }

            executor.shutdown();

            logger.info("Subnet scan complete. Found " + discoveredIps.size() + " devices");
            if (listener != null) {
                listener.onScanComplete(new ArrayList<>(discoveredIps));
            }

            return new ArrayList<>(discoveredIps);

        } finally {
            scanInProgress = false;
        }
    }

    /**
     * Scan subnet without listener (simple version).
     */
    public List<String> scanSubnet(List<String> addressesToScan) {
        return scanSubnet(addressesToScan, null);
    }
}
