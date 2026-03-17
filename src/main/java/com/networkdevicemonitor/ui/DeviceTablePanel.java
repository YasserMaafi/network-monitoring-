package com.networkdevicemonitor.ui;

import com.networkdevicemonitor.models.NetworkDevice;
import com.networkdevicemonitor.services.DeviceRepository;
import javax.swing.*;
import java.awt.*;

/**
 * Panel containing the scrollable device table with custom renderers.
 */
public class DeviceTablePanel extends JPanel {
    private final JTable deviceTable;
    private final DeviceTableModel tableModel;
    private final DeviceRepository deviceRepository;

    public interface TableSelectionListener {
        void onSelectionChanged(NetworkDevice selectedDevice);
    }

    private TableSelectionListener selectionListener;

    public DeviceTablePanel(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
        this.tableModel = new DeviceTableModel();

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Network Devices"));

        // Create table
        deviceTable = new JTable(tableModel);
        deviceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        deviceTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        deviceTable.setRowHeight(20);

        // Apply custom renderer to status column
        deviceTable.getColumnModel().getColumn(2)
                .setCellRenderer(new DeviceStatusCellRenderer());

        // Listen to selection changes
        deviceTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = deviceTable.getSelectedRow();
                if (selectedRow >= 0 && selectionListener != null) {
                    NetworkDevice device = tableModel.getDeviceAt(selectedRow);
                    selectionListener.onSelectionChanged(device);
                }
            }
        });

        // Add to scroll pane
        JScrollPane scrollPane = new JScrollPane(deviceTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setSelectionListener(TableSelectionListener listener) {
        this.selectionListener = listener;
    }

    public JTable getTable() {
        return deviceTable;
    }

    public DeviceTableModel getModel() {
        return tableModel;
    }

    /**
     * Get the currently selected device.
     */
    public NetworkDevice getSelectedDevice() {
        int selectedRow = deviceTable.getSelectedRow();
        if (selectedRow >= 0) {
            return tableModel.getDeviceAt(selectedRow);
        }
        return null;
    }

    /**
     * Update the table with fresh device data.
     */
    public void refreshTable() {
        tableModel.setDevices(deviceRepository.getAllDevices());
    }

    /**
     * Clear the table.
     */
    public void clearTable() {
        tableModel.clear();
    }
}
