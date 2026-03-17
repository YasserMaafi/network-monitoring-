# Quick Start Guide - Network Device Monitor

## 🚀 Get Running in 2 Minutes

### Option 1: Using Maven (Recommended)
```bash
cd c:\Users\2Much\Desktop\network-java
mvn clean compile exec:java -Dexec.mainClass="com.networkdevicemonitor.NetworkDeviceMonitor"
```

### Option 2: Using Pre-Compiled Classes
```bash
cd c:\Users\2Much\Desktop\network-java
java -cp bin com.networkdevicemonitor.NetworkDeviceMonitor
```

### Option 3: Using the Build Script (Windows)
```cmd
cd c:\Users\2Much\Desktop\network-java
build.bat
```

### Option 4: Using the Build Script (Linux/Mac)
```bash
cd ~/Desktop/network-java
chmod +x build.sh
./build.sh
```

---

## 📋 What You Get

A fully-functional network monitoring application with:

✅ **Automatic Network Detection** - Finds your local subnet automatically  
✅ **Device Discovery** - Scans and finds all reachable hosts  
✅ **Real-Time Monitoring** - Updates device status every 5 seconds  
✅ **Live Table** - Color-coded status (green=online, red=offline)  
✅ **Device Details** - Shows latency, ping history, statistics  
✅ **Action Buttons** - Scan, ping, refresh on demand  
✅ **Activity Log** - Events recorded with timestamps  
✅ **Responsive UI** - All network ops in background (never freezes)  

---

## 🎯 First Run Steps

1. **Launch the app**
2. **Click "Scan Network"** to discover devices
   - Takes 20-30 seconds for first scan
   - Shows progress in toolbar
   - Found devices appear in table
3. **Select a device** from the table
   - Details panel shows full information
4. **Click "Start Monitoring"** to enable periodic updates
   - Devices checked every 5 seconds
   - Latency and status updated live
5. **Try individual actions**:
   - Select device → **"Ping Selected"** for immediate ping
   - Select device → **"Refresh Selected"** for manual check
   - Watch the log panel for all events

---

## 📁 Project Contents

```
network-java/
├── bin/                          (compiled .class files)
├── src/main/java/                (all source code)
│   └── com/networkdevicemonitor/
│       ├── models/               (data classes)
│       ├── services/             (business logic)
│       ├── ui/                   (Swing components)
│       └── controller/           (orchestrator)
├── pom.xml                       (Maven config)
├── build.bat                     (Windows build script)
├── build.sh                      (Linux/Mac build script)
├── README.md                     (full documentation)
├── BUILD.md                      (build instructions)
└── QUICKSTART.md                 (this file)
```

---

## 🔧 Troubleshooting

### "Java not found"
```powershell
# Install Java 17+
# Verify: java -version
# Should show: openjdk version "17.x.x" or higher
```

### "Maven not found"
```powershell
# Option 1: Install Maven from https://maven.apache.org
# Option 2: Use javac command directly (see BUILD.md)
# Option 3: Use pre-compiled: java -cp bin com.networkdevicemonitor.NetworkDeviceMonitor
```

### "Network detection failed"
- This is normal on VirtualBox/WSL
- App still works - just use as a ping tool
- No scan needed - manually add IPs via future feature

### "App crashes on startup"
- Check Java version: `java -version` (need 17+)
- Try: `java -cp bin com.networkdevicemonitor.NetworkDeviceMonitor`
- Check event log for errors

### Network not detected (Windows)
- App uses `InetAddress.isReachable()` - requires proper network config
- Works on physical networks and most VMs
- On WSL: consider running native Windows app instead

---

## ⚙️ Key Features Explained

### Toolbar Buttons

| Button | Effect |
|--------|--------|
| **Scan Network** | Discovers all devices on your subnet (background) |
| **Refresh Selected** | Re-ping the selected device immediately |
| **Ping Selected** | Manual ping with result dialog |
| **View Details** | Focus on the details panel (auto-happens on select) |
| **Start Monitoring** | Begin periodic 5-second checks |
| **Stop Monitoring** | Pause automatic monitoring |

### Table Columns

| Column | Meaning |
|--------|---------|
| **IP Address** | Device IPv4 address |
| **Hostname** | Reverse DNS name or IP if not resolved |
| **Status** | Online (green), Offline (red), Unknown (gray) |
| **Latest (ms)** | Most recent ping latency in milliseconds |
| **Average (ms)** | Average of last 100 pings |
| **Last Seen** | Timestamp of most recent successful ping |

### Details Panel (Right Side)

Shows when you select a device:
- Full statistics (min/max/average latency)
- Check counts (successful and failed)
- Timestamps (first and last seen)
- Status indicator

### Activity Log (Bottom)

Records all events:
- Monitor started/stopped
- Devices discovered
- Status changes (online→offline)
- Scan events
- Manual actions

---

## 🎨 UI Layout

```
┌─────────────────────────────────────────────────────────────────┐
│  Toolbar buttons: [Scan] [Refresh] [Ping] [Start] [Stop] Status │
├─────────────────────────┬─────────────────────────────────────────┤
│                         │                                         │
│    Device Table         │     Device Details Panel                │
│  (IP, Host, Status,     │   (per-device statistics)               │
│   Latency, etc)         │                                         │
│                         │                                         │
├─────────────────────────────────────────────────────────────────┤
│  Activity Log                          [Clear]                  │
│  [INFO] Scan started                                            │
│  [SUCCESS] Device discovered: 192.168.1.100                     │
│  [INFO] Monitoring started                                      │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔌 Extensibility

This codebase is structured for easy enhancement:

### Add Port Scanning
```java
// Create PortScannerService.java
// Call from pingDevice() method
```

### Add Persistence
```java
// Enhance DeviceRepository with JSON save/load
// Add settings file for config
```

### Add Alerts
```java
// Implement threshold checking in MonitoringService
// Add sound/popup notifications
```

### Add Graphs
```java
// Use JFreeChart or custom JPanel
// Plot latency history over time
```

---

## 📊 Performance

On typical home network:
- **First scan**: 20-30 seconds (254 addresses)
- **Monitoring cycle**: 2-5 seconds (50 devices)
- **UI responsiveness**: Smooth (no freezing)
- **Memory**: ~50-100 MB typical

---

## 💡 Pro Tips

1. **Filter devices** - The table is sortable (click headers in future updates)
2. **Export data** - Copy-paste from table into Excel/CSV
3. **High latency** - Indicates network congestion or device load
4. **Repeated failures** - Device marked offline after 3 consecutive misses
5. **Hostname Resolution** - Takes a moment on first discovery (cached after)

---

## 📚 More Details

For comprehensive documentation, see:
- [README.md](README.md) - Full feature list and architecture
- [BUILD.md](BUILD.md) - Detailed build procedures
- Source comments - Implementation notes

---

**Ready?** Run one of the commands above and enjoy! 🎉
