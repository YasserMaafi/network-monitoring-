# Network Device Monitor

A real-time local network monitoring desktop application built with **Java 17+ and Swing**. Automatically discovers devices on your local network, displays them in a live table, and enables real-time monitoring with periodic refresh and manual ping capabilities.

## Overview

This is a **production-grade** network monitoring tool (not a CRUD app) that:
- Automatically detects your active IPv4 local network
- Scans the subnet for reachable devices in parallel
- Displays discovered devices in a live Swing table with color-coded status
- Updates device reachability and latency every 5 seconds (configurable)
- Shows detailed statistics per device
- Maintains activity logs
- Provides manual actions (scan, ping, refresh)
- Runs all network operations in background threads (UI always responsive)

## Architecture Highlights

### Clean Separation of Concerns
- **Models**: Data classes (NetworkDevice, DeviceStatus, PingResult, LogEntry)
- **Services**: Business logic (PingService, SubnetScannerService, MonitoringService, etc.)
- **UI**: Swing components (MainFrame, DeviceTablePanel, DeviceDetailsPanel, etc.)
- **Controller**: AppController orchestrates services and UI updates

### Threading
- All network operations run in background thread pools
- ScheduledExecutorService for periodic monitoring
- SwingUtilities.invokeLater for safe UI updates from worker threads
- UI never blocks

### Collections & Thread Safety
- ConcurrentHashMap and CopyOnWriteArrayList for thread-safe shared state
- NetworkDevice uses volatile fields for atomic reads (latency, status)
- Listener pattern for loose coupling between services and UI

## Features

### Automatic Network Detection
- Detects active IPv4 network interface (non-loopback, non-virtual preferred)
- Calculates subnet range from network prefix
- Supports /8, /16, /24 subnets (with scaling for larger scans)
- Gracefully falls back if no primary interface detected

### Device Discovery
Uses Java's `InetAddress.isReachable()` for cross-platform compatibility:
- Parallel scanning with 20 concurrent ping threads
- Hostname resolution via reverse DNS lookup
- Progress tracking during scan
- Discovered devices automatically added to table

### Real-Time Monitoring
- Periodic refresh of all known devices (default 5 seconds)
- Tracks latency history (up to 100 samples per device)
- Computes min, max, average latency on-the-fly
- Counts successful/failed checks per device
- Marks devices offline after 3 consecutive failures
- Logs status changes (online/offline transitions)

### Rich UI
- **Toolbar**: Scan Network, Refresh Selected, Ping Selected, Start/Stop Monitoring buttons
- **Table**: Color-coded by status (green=online, red=offline, gray=unknown)
- **Details Panel**: Comprehensive per-device statistics
- **Activity Log**: Real-time event log with timestamp
- **Responsive Layout**: Adjustable split panes

### Extensible Logging
- Timestamped log entries (INFO, WARNING, ERROR, SUCCESS)
- Bounded to 1000 entries (FIFO eviction)
- Listener pattern for real-time updates
- Can be extended for file logging

## Configuration

### Monitoring Interval
In [AppController.java](src/main/java/com/networkdevicemonitor/controller/AppController.java), adjust:
```java
private static final long DEFAULT_INTERVAL_SECONDS = 5;
```

### Offline Threshold
In [MonitoringService.java](src/main/java/com/networkdevicemonitor/services/MonitoringService.java):
```java
private static final int OFFLINE_THRESHOLD = 3; // Consecutive failures
```

### Parallel Scan Threads
In [SubnetScannerService.java](src/main/java/com/networkdevicemonitor/services/SubnetScannerService.java):
```java
private static final int THREAD_POOL_SIZE = 20;
```

### Latency History Size
In [NetworkDevice.java](src/main/java/com/networkdevicemonitor/models/NetworkDevice.java):
```java
private static final int MAX_LATENCY_HISTORY = 100;
```

## Build & Run

### Prerequisites
- **Java 17+** (verify: `java -version`)
- **Maven 3.8+** (verify: `mvn -version`)

### Quick Start
```bash
cd c:\Users\2Much\Desktop\network-java
mvn clean compile
mvn exec:java -Dexec.mainClass="com.networkdevicemonitor.NetworkDeviceMonitor"
```

### Build Executable JAR
```bash
mvn clean package
java -jar target/network-device-monitor.jar
```

### Without Maven (Plain Javac)
See [BUILD.md](BUILD.md) for detailed instructions.

### In VS Code
1. Install "Extension Pack for Java" (Microsoft)
2. Open folder: `c:\Users\2Much\Desktop\network-java`
3. Open terminal and run:
   ```bash
   mvn clean compile exec:java -Dexec.mainClass="com.networkdevicemonitor.NetworkDeviceMonitor"
   ```

### In IntelliJ IDEA
1. Open project: `File > Open` → select project folder
2. Right-click `NetworkDeviceMonitor.java` → `Run NetworkDeviceMonitor.main()`

## Project Structure

```
network-java/
├── pom.xml                                 # Maven build config
├── README.md                               # This file
├── BUILD.md                                # Detailed build instructions
├── src/main/java/com/networkdevicemonitor/
│   ├── NetworkDeviceMonitor.java           # Main entry point
│   ├── models/
│   │   ├── NetworkDevice.java              # Device model with statistics
│   │   ├── DeviceStatus.java               # Enum: ONLINE, OFFLINE, UNKNOWN
│   │   ├── PingResult.java                 # Result of single ping
│   │   └── LogEntry.java                   # Log event model
│   ├── services/
│   │   ├── NetworkInterfaceService.java    # Detects local network
│   │   ├── PingService.java                # ICMP ping via isReachable()
│   │   ├── SubnetScannerService.java       # Parallel subnet scan
│   │   ├── MonitoringService.java          # Periodic device monitoring
│   │   ├── DeviceRepository.java           # In-memory device storage
│   │   └── LogService.java                 # Event logging
│   ├── ui/
│   │   ├── MainFrame.java                  # Top-level Swing frame
│   │   ├── DeviceTableModel.java           # Custom AbstractTableModel
│   │   ├── DeviceStatusCellRenderer.java   # Color-coded table rows
│   │   ├── DeviceTablePanel.java           # Table + scroll + listener
│   │   ├── DeviceDetailsPanel.java         # Right sidebar details
│   │   ├── LogPanel.java                   # Bottom activity log
│   │   └── ToolbarPanel.java               # Top action buttons
│   └── controller/
│       └── AppController.java              # Service orchestrator & threading
└── target/                                 # Build output (after mvn build)
```

## Usage Guide

### Scanning the Network
1. Click **"Scan Network"** button in toolbar
2. App auto-detects your local network
3. Scans all addresses in subnet (e.g., 192.168.1.1-254)
4. Discovered devices appear in table
5. Check activity log for progress

### Starting Monitoring
1. Click **"Start Monitoring"** button
2. App periodically re-checks all known devices every 5 seconds
3. Updates table with latest status and latency
4. Watch in real-time as devices go online/offline
5. Click **"Stop Monitoring"** to pause

### Inspecting a Device
1. Click a device row in the table
2. Right panel shows full statistics:
   - IP address, hostname, status
   - Latest/average/min/max latency
   - Successful/failed check counts
   - Timestamps
3. Status colors help identify health at a glance

### Manual Ping
1. Select a device in the table
2. Click **"Ping Selected"** button
3. Dialog shows ping result or timeout
4. Device stats updated immediately

### Refresh Selected Device
1. Select a device
2. Click **"Refresh Selected"** button
3. Single device checked immediately (doesn't wait for monitoring cycle)

## Technical Details

### Network Detection
- Enumerates network interfaces via `NetworkInterface.getNetworkInterfaces()`
- Prefers up, non-virtual, non-loopback interfaces
- Extracts IPv4 address and CIDR prefix
- Calculates subnet base (e.g., 192.168.1.0 for /24)

### Ping Implementation
- Uses `InetAddress.isReachable(3000ms)` for cross-platform compatibility
- Fallback mechanism (can extend with OS ping command if needed)
- Measures elapsed time for latency
- Handles timeouts and exceptions gracefully

### Monitoring Algorithm
```
Every 5 seconds:
  For each known device:
    Ping device
    If reachable:
      - Record latency
      - Mark ONLINE
      - Reset failure counter
      - Update table
    If unreachable:
      - Increment failure counter
      - If failures >= 3:
        - Mark OFFLINE
        - Log status change
      - Update table
```

### Threading Model
- **Main (EDT)**: All Swing operations, UI rendering
- **Background Pool** (2 threads): Network scans and immediate pings
- **Monitoring Thread**: ScheduledExecutorService for periodic checks
- **SafeUpdates**: All service→UI calls use SwingUtilities.invokeLater

## Known Limitations & Future Enhancements

### Current Limitations
- MAC address retrieval not yet implemented (cross-platform complexity)
- No persistence (devices lost on app restart)
- No alert/notification system yet
- Limited to local subnet (no inter-subnet routing)
- Large subnets (/8, /16) scanned partially to avoid excessive runtime

### Extensible For
- [ ] Port scanning and service detection
- [ ] DNS name resolution and lookup
- [ ] Performance graphs and historical data
- [ ] Alert system (beep, popup, email)
- [ ] Data export (CSV, JSON)
- [ ] Configuration file persistence
- [ ] Whitelist/blacklist devices
- [ ] Network interface selection UI
- [ ] Latency thresholds and alerts
- [ ] Multiple subnet monitoring

## Performance Characteristics

| Operation | Typical Time |
|---|---|
| Detect network interface | <50ms |
| Scan /24 subnet (254 IPs, parallel) | 20-30s |
| Scan /16 subnet (partial, first 254 addrs) | 20-30s |
| Periodic monitoring cycle (50 devices) | 2-5s |
| Hostname resolution (per device) | 0-500ms |

The UI remains **fully responsive** during all operations thanks to background threading.

## Code Quality

### Design Principles
✅ Single Responsibility: Each class has one reason to change  
✅ Dependency Injection: Services composed in AppController  
✅ MVC-like Pattern: Models, Services, UI, Controller  
✅ Thread Safety: ConcurrentHashMap, volatile fields, SwingUtilities  
✅ Listener Pattern: Loose coupling between layers  
✅ No External Dependencies: Pure Java 17 + Swing  

### Logging
- Built-in Java logger for development debugging
- Application event log for user-facing activity
- All major operations recorded

### Comments
- Clear class-level documentation
- Method summaries
- Platform-sensitive operations marked
- Config constants documented

## CLI Arguments (Future Enhancement)

Currently no command-line arguments. Future versions could support:
```
java -jar app.jar --interface eth0 --interval 10 --subnet 10.0.0.0/24
```

## License
This is a demo/educational project. Use freely for learning.

## Contact & Support
For issues or questions, review:
- [BUILD.md](BUILD.md) - Build troubleshooting
- Service class comments - Implementation details
- Activity log in app - Runtime diagnostics

---

**Ready to build?** Start with `mvn clean compile exec:java -Dexec.mainClass="com.networkdevicemonitor.NetworkDeviceMonitor"`
