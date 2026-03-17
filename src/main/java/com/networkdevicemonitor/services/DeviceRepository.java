package com.networkdevicemonitor.services;

import com.networkdevicemonitor.models.NetworkDevice;
import com.networkdevicemonitor.models.DeviceStatus;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Service for managing the collection of known devices.
 */
public class DeviceRepository {
    private static final Logger logger = Logger.getLogger(DeviceRepository.class.getName());

    private final Map<String, NetworkDevice> devices = new ConcurrentHashMap<>();
    private final List<RepositoryListener> listeners = Collections.synchronizedList(new ArrayList<>());

    public interface RepositoryListener {
        void onDeviceAdded(NetworkDevice device);
        void onDeviceUpdated(NetworkDevice device);
        void onDeviceRemoved(NetworkDevice device);
    }

    public void addListener(RepositoryListener listener) {
        listeners.add(listener);
    }

    public void removeListener(RepositoryListener listener) {
        listeners.remove(listener);
    }

    /**
     * Get or create a device by IP address.
     */
    public NetworkDevice getOrCreateDevice(String ipAddress) {
        return devices.computeIfAbsent(ipAddress, ip -> {
            NetworkDevice device = new NetworkDevice(ip);
            notifyDeviceAdded(device);
            return device;
        });
    }

    /**
     * Get a device by IP address. Returns null if not found.
     */
    public NetworkDevice getDevice(String ipAddress) {
        return devices.get(ipAddress);
    }

    /**
     * Get all devices as an unmodifiable list.
     */
    public List<NetworkDevice> getAllDevices() {
        return new ArrayList<>(devices.values());
    }

    /**
     * Remove a device by IP address.
     */
    public void removeDevice(String ipAddress) {
        NetworkDevice device = devices.remove(ipAddress);
        if (device != null) {
            notifyDeviceRemoved(device);
        }
    }

    /**
     * Clear all devices.
     */
    public void clearAll() {
        List<NetworkDevice> devicesCopy = new ArrayList<>(devices.values());
        devices.clear();
        devicesCopy.forEach(this::notifyDeviceRemoved);
    }

    /**
     * Notify listeners of device changes.
     */
    public void notifyDeviceUpdated(NetworkDevice device) {
        for (RepositoryListener listener : listeners) {
            listener.onDeviceUpdated(device);
        }
    }

    private void notifyDeviceAdded(NetworkDevice device) {
        for (RepositoryListener listener : listeners) {
            listener.onDeviceAdded(device);
        }
    }

    private void notifyDeviceRemoved(NetworkDevice device) {
        for (RepositoryListener listener : listeners) {
            listener.onDeviceRemoved(device);
        }
    }

    public int size() {
        return devices.size();
    }

    public boolean isEmpty() {
        return devices.isEmpty();
    }
}
