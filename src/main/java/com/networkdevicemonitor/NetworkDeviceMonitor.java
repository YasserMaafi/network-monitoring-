package com.networkdevicemonitor;

import com.networkdevicemonitor.controller.AppController;
import com.networkdevicemonitor.ui.MainFrame;
import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main entry point for the Network Device Monitor application.
 * Java 17+ with Swing.
 *
 * Usage:
 *   javac -d bin @sources.txt
 *   java -cp bin com.networkdevicemonitor.NetworkDeviceMonitor
 */
public class NetworkDeviceMonitor {
    private static final Logger logger = Logger.getLogger(NetworkDeviceMonitor.class.getName());

    public static void main(String[] args) {
        // Configure logger
        LoggerConfiguration.configure();

        // Run on EDT
        SwingUtilities.invokeLater(() -> {
            try {
                // Set look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                // Create app controller
                AppController appController = new AppController();

                // Create and show main frame
                MainFrame frame = new MainFrame(appController);

                // Setup shutdown hook
                Runtime.getRuntime().addShutdownHook(new Thread(appController::shutdown));

                logger.info("Application UI initialized");

            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to start application", e);
                JOptionPane.showMessageDialog(null,
                        "Failed to start application: " + e.getMessage(),
                        "Startup Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}

/**
 * Configures Java logging for the application.
 */
class LoggerConfiguration {
    public static void configure() {
        // Configure root logger
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.INFO);

        // You can add handlers/formatters here if needed
    }
}
