package com.networkdevicemonitor.models;

/**
 * Enum representing the current status of a network device.
 */
public enum DeviceStatus {
    ONLINE("Online"),
    OFFLINE("Offline"),
    UNKNOWN("Unknown");

    private final String displayName;

    DeviceStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
