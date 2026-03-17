package com.networkdevicemonitor.services;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for detecting the local network interface and calculating subnet scan ranges.
 * Finds the active, non-loopback IPv4 network and determines the subnet to scan.
 */
public class NetworkInterfaceService {
    private static final Logger logger = Logger.getLogger(NetworkInterfaceService.class.getName());

    /**
     * Represents a detected network with its range and interface info.
     */
    public static class NetworkInfo {
        private final String interfaceName;
        private final InetAddress localAddress;
        private final String subnetBase; // e.g., "192.168.1"
        private final byte[] subnetBytes;
        private final int prefixLength;

        public NetworkInfo(String interfaceName, InetAddress localAddress, String subnetBase,
                          byte[] subnetBytes, int prefixLength) {
            this.interfaceName = interfaceName;
            this.localAddress = localAddress;
            this.subnetBase = subnetBase;
            this.subnetBytes = subnetBytes;
            this.prefixLength = prefixLength;
        }

        public String getInterfaceName() {
            return interfaceName;
        }

        public InetAddress getLocalAddress() {
            return localAddress;
        }

        public String getSubnetBase() {
            return subnetBase;
        }

        public byte[] getSubnetBytes() {
            return subnetBytes;
        }

        public int getPrefixLength() {
            return prefixLength;
        }

        @Override
        public String toString() {
            return String.format("NetworkInfo{interface=%s, local=%s, subnet=%s/%d}",
                    interfaceName, localAddress.getHostAddress(), subnetBase, prefixLength);
        }
    }

    /**
     * Auto-detect the active local network interface and return network info.
     * Prefers non-virtual, up, and non-loopback interfaces.
     * Returns null if no suitable interface is found.
     */
    public NetworkInfo detectLocalNetwork() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            List<NetworkInterface> candidateInterfaces = new ArrayList<>();

            // Collect all suitable candidates
            for (NetworkInterface iface : Collections.list(interfaces)) {
                if (iface.isUp() && !iface.isLoopback() && !iface.isVirtual()) {
                    candidateInterfaces.add(iface);
                }
            }

            // Try candidates
            for (NetworkInterface iface : candidateInterfaces) {
                NetworkInfo info = extractNetworkInfo(iface);
                if (info != null) {
                    logger.info("Detected network interface: " + info);
                    return info;
                }
            }

            // Fallback: try any up, non-loopback interface
            for (NetworkInterface iface : Collections.list(interfaces)) {
                if (iface.isUp() && !iface.isLoopback()) {
                    NetworkInfo info = extractNetworkInfo(iface);
                    if (info != null) {
                        logger.info("Detected network interface (fallback): " + info);
                        return info;
                    }
                }
            }

            logger.warning("No suitable network interface found");
            return null;

        } catch (SocketException e) {
            logger.log(Level.SEVERE, "Error detecting network interface", e);
            return null;
        }
    }

    /**
     * Detect ALL local network interfaces (for multi-subnet scanning).
     * Returns list of all IPv4 networks available on this machine.
     */
    public List<NetworkInfo> detectAllLocalNetworks() {
        List<NetworkInfo> networks = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

            for (NetworkInterface iface : Collections.list(interfaces)) {
                if (iface.isUp() && !iface.isLoopback()) {
                    try {
                        for (InterfaceAddress addr : iface.getInterfaceAddresses()) {
                            InetAddress inetAddr = addr.getAddress();

                            // Only consider IPv4 addresses
                            if (inetAddr.getAddress().length != 4) {
                                continue;
                            }

                            int prefixLength = addr.getNetworkPrefixLength();
                            if (prefixLength < 8 || prefixLength > 30) {
                                continue;
                            }

                            byte[] addrBytes = inetAddr.getAddress();
                            byte[] subnetBytes = calculateSubnetBytes(addrBytes, prefixLength);
                            String subnetBase = formatSubnetBase(subnetBytes, prefixLength);

                            NetworkInfo info = new NetworkInfo(iface.getName(), inetAddr, subnetBase, subnetBytes, prefixLength);
                            networks.add(info);
                            logger.info("Found network interface: " + info);
                        }
                    } catch (Exception e) {
                        logger.log(Level.FINE, "Error extracting info from " + iface.getName(), e);
                    }
                }
            }

        } catch (SocketException e) {
            logger.log(Level.SEVERE, "Error detecting network interfaces", e);
        }

        if (networks.isEmpty()) {
            logger.warning("No network interfaces detected");
        }

        return networks;
    }

    /**
     * Extract IPv4 network information from a network interface.
     */
    private NetworkInfo extractNetworkInfo(NetworkInterface iface) {
        try {
            for (InterfaceAddress addr : iface.getInterfaceAddresses()) {
                InetAddress inetAddr = addr.getAddress();

                // Only consider IPv4 addresses
                if (inetAddr.getAddress().length != 4) {
                    continue;
                }

                int prefixLength = addr.getNetworkPrefixLength();
                if (prefixLength < 8 || prefixLength > 30) {
                    continue; // Invalid or unusual prefix
                }

                // Calculate subnet base
                byte[] addrBytes = inetAddr.getAddress();
                byte[] subnetBytes = calculateSubnetBytes(addrBytes, prefixLength);
                String subnetBase = formatSubnetBase(subnetBytes, prefixLength);

                return new NetworkInfo(iface.getName(), inetAddr, subnetBase, subnetBytes, prefixLength);
            }
        } catch (Exception e) {
            logger.log(Level.FINE, "Error extracting network info from " + iface.getName(), e);
        }
        return null;
    }

    /**
     * Calculate the network address bytes (e.g., 192.168.1.0 for /24).
     */
    private byte[] calculateSubnetBytes(byte[] addressBytes, int prefixLength) {
        byte[] subnetBytes = new byte[4];
        int fullBytes = prefixLength / 8;
        int remainingBits = prefixLength % 8;

        // Copy full bytes
        System.arraycopy(addressBytes, 0, subnetBytes, 0, fullBytes);

        // Handle partial byte
        if (fullBytes < 4 && remainingBits > 0) {
            byte mask = (byte) (0xFF << (8 - remainingBits));
            subnetBytes[fullBytes] = (byte) (addressBytes[fullBytes] & mask);
        }

        return subnetBytes;
    }

    /**
     * Format subnet base string (e.g., "192.168.1" for /24).
     */
    private String formatSubnetBase(byte[] subnetBytes, int prefixLength) {
        int bytes = (prefixLength + 7) / 8;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < bytes; i++) {
            if (i > 0) sb.append(".");
            sb.append(subnetBytes[i] & 0xFF);
        }

        return sb.toString();
    }

    /**
     * Generate all IP addresses in the given subnet range.
     * For /24 subnet, generates 1 to 254 (skips network and broadcast).
     * For /16 and /8, uses the local IP to determine the appropriate /24 subnet to scan.
     */
    public List<String> generateSubnetAddresses(NetworkInfo networkInfo) {
        List<String> addresses = new ArrayList<>();
        int prefixLength = networkInfo.getPrefixLength();
        byte[] localIpBytes = networkInfo.getLocalAddress().getAddress();

        // For /24, generate .1 to .254
        if (prefixLength == 24) {
            String base = networkInfo.getSubnetBase();
            for (int i = 1; i <= 254; i++) {
                addresses.add(base + "." + i);
            }
        }
        // For /16, scan the /24 subnet containing the local IP
        else if (prefixLength == 16) {
            String octet1 = String.valueOf(localIpBytes[0] & 0xFF);
            String octet2 = String.valueOf(localIpBytes[1] & 0xFF);
            String octet3 = String.valueOf(localIpBytes[2] & 0xFF);
            String base = octet1 + "." + octet2 + "." + octet3;
            for (int i = 1; i <= 254; i++) {
                addresses.add(base + "." + i);
            }
            logger.info("Scanning /16 subnet - local /24: " + base + ".0/24");
        }
        // For /8, scan the /24 subnet containing the local IP
        else if (prefixLength == 8) {
            String octet1 = String.valueOf(localIpBytes[0] & 0xFF);
            String octet2 = String.valueOf(localIpBytes[1] & 0xFF);
            String octet3 = String.valueOf(localIpBytes[2] & 0xFF);
            String base = octet1 + "." + octet2 + "." + octet3;
            for (int i = 1; i <= 254; i++) {
                addresses.add(base + "." + i);
            }
            logger.info("Scanning /8 subnet - local /24: " + base + ".0/24");
        }
        // Generic fallback for other sizes
        else {
            String base = networkInfo.getSubnetBase();
            for (int i = 1; i <= 254; i++) {
                addresses.add(base + "." + i);
            }
        }

        return addresses;
    }
}
