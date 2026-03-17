package com.networkdevicemonitor.ui;

import com.networkdevicemonitor.models.DeviceStatus;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Custom cell renderer for the device table to display status with colors.
 */
public class DeviceStatusCellRenderer extends DefaultTableCellRenderer {
    private static final Color COLOR_ONLINE = new Color(144, 238, 144); // Light green
    private static final Color COLOR_OFFLINE = new Color(255, 127, 127); // Light red
    private static final Color COLOR_UNKNOWN = new Color(200, 200, 200); // Light gray

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                  boolean hasFocus, int row, int column) {
        Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (!isSelected) {
            // Extract the model and status
            if (table.getModel() instanceof DeviceTableModel) {
                DeviceTableModel model = (DeviceTableModel) table.getModel();
                DeviceStatus status = model.getStatusAt(row);

                if (status != null) {
                    switch (status) {
                        case ONLINE -> comp.setBackground(COLOR_ONLINE);
                        case OFFLINE -> comp.setBackground(COLOR_OFFLINE);
                        case UNKNOWN -> comp.setBackground(COLOR_UNKNOWN);
                    }
                } else {
                    comp.setBackground(Color.WHITE);
                }
            }
        } else {
            comp.setBackground(table.getSelectionBackground());
        }

        return comp;
    }
}
