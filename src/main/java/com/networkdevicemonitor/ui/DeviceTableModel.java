package com.networkdevicemonitor.ui;

import com.networkdevicemonitor.models.NetworkDevice;
import com.networkdevicemonitor.models.DeviceStatus;
import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Table model for displaying network devices.
 * Provides columns: IP, Hostname, Status, Latest Latency, Avg Latency, Last Seen
 */
public class DeviceTableModel extends AbstractTableModel {
    private static final String[] COLUMN_NAMES = {
            "IP Address",
            "Hostname",
            "Status",
            "Latest (ms)",
            "Average (ms)",
            "Last Seen"
    };

    private static final int COLUMN_IP = 0;
    private static final int COLUMN_HOSTNAME = 1;
    private static final int COLUMN_STATUS = 2;
    private static final int COLUMN_LATEST = 3;
    private static final int COLUMN_AVERAGE = 4;
    private static final int COLUMN_LASTSEEN = 5;

    private final List<NetworkDevice> devices = new ArrayList<>();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public int getRowCount() {
        return devices.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return COLUMN_NAMES[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= devices.size()) {
            return null;
        }

        NetworkDevice device = devices.get(rowIndex);

        return switch (columnIndex) {
            case COLUMN_IP -> device.getIpAddress();
            case COLUMN_HOSTNAME -> device.getHostname();
            case COLUMN_STATUS -> device.getStatus().getDisplayName();
            case COLUMN_LATEST -> formatLatency(device.getLatestLatencyMs());
            case COLUMN_AVERAGE -> formatLatency(device.getAverageLatencyMs());
            case COLUMN_LASTSEEN -> device.getLastSeenTime().format(timeFormatter);
            default -> null;
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == COLUMN_LATEST || columnIndex == COLUMN_AVERAGE) {
            return Long.class;
        }
        return String.class;
    }

    /**
     * Set the list of devices to display.
     */
    public void setDevices(List<NetworkDevice> newDevices) {
        this.devices.clear();
        this.devices.addAll(newDevices);
        fireTableDataChanged();
    }

    /**
     * Add or update a device in the table.
     */
    public int addOrUpdateDevice(NetworkDevice device) {
        int index = findDeviceIndex(device.getIpAddress());

        if (index >= 0) {
            // Update existing
            devices.set(index, device);
            fireTableRowsUpdated(index, index);
            return index;
        } else {
            // Add new
            devices.add(device);
            int newIndex = devices.size() - 1;
            fireTableRowsInserted(newIndex, newIndex);
            return newIndex;
        }
    }

    /**
     * Remove a device from the table.
     */
    public void removeDevice(String ipAddress) {
        int index = findDeviceIndex(ipAddress);
        if (index >= 0) {
            devices.remove(index);
            fireTableRowsDeleted(index, index);
        }
    }

    /**
     * Get the device at the specified row.
     */
    public NetworkDevice getDeviceAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < devices.size()) {
            return devices.get(rowIndex);
        }
        return null;
    }

    /**
     * Find the row index of a device by IP address.
     */
    public int findDeviceIndex(String ipAddress) {
        for (int i = 0; i < devices.size(); i++) {
            if (devices.get(i).getIpAddress().equals(ipAddress)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get status of device at row.
     */
    public DeviceStatus getStatusAt(int rowIndex) {
        NetworkDevice device = getDeviceAt(rowIndex);
        return device != null ? device.getStatus() : null;
    }

    /**
     * Sort by IP address ascending.
     */
    public void sortByIp() {
        devices.sort(Comparator.comparing(NetworkDevice::getIpAddress));
        fireTableDataChanged();
    }

    /**
     * Sort by status (online first).
     */
    public void sortByStatus() {
        devices.sort((d1, d2) -> {
            // Online first
            if (d1.getStatus() == DeviceStatus.ONLINE && d2.getStatus() != DeviceStatus.ONLINE) return -1;
            if (d1.getStatus() != DeviceStatus.ONLINE && d2.getStatus() == DeviceStatus.ONLINE) return 1;
            return d1.getIpAddress().compareTo(d2.getIpAddress());
        });
        fireTableDataChanged();
    }

    private String formatLatency(long latencyMs) {
        if (latencyMs < 0) {
            return "—";
        }
        return latencyMs + "";
    }

    public void clear() {
        devices.clear();
        fireTableDataChanged();
    }

    public int size() {
        return devices.size();
    }
}
