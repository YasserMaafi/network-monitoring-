# Network Device Monitor - Build Instructions

## Prerequisites
- **Java 17 or later** (check with `java -version`)
- **Maven 3.8+** (check with `mvn -version`)
- On Windows, ensure Java and Maven are in your PATH

## Build with Maven

### Quick Start
```bash
cd c:\Users\2Much\Desktop\network-java
mvn clean compile
mvn exec:java -Dexec.mainClass="com.networkdevicemonitor.NetworkDeviceMonitor"
```

### Package as JAR
```bash
mvn clean package
java -jar target/network-device-monitor.jar
```

### Create Fat JAR (all dependencies in one file)
```bash
mvn clean package shade:shade
java -jar target/network-device-monitor-fat.jar
```

## Build with Plain Javac (No Maven)

### On Windows PowerShell
```powershell
# Create output directory
New-Item -ItemType Directory -Path "bin" -Force

# Compile all Java files
$files = Get-ChildItem -Path "src\main\java" -Recurse -Filter "*.java" -File
$fileList = ($files | ForEach-Object { $_.FullName }) -join " "
javac -d bin -sourcepath "src\main\java" $fileList

# Run the application
java -cp "bin" com.networkdevicemonitor.NetworkDeviceMonitor
```

### On Linux/Mac Terminal
```bash
# Create output directory
mkdir -p bin

# Compile
find src/main/java -name "*.java" | xargs javac -d bin -sourcepath src/main/java

# Run
java -cp bin com.networkdevicemonitor.NetworkDeviceMonitor
```

## Project Structure
```
network-java/
├── pom.xml
├── src/main/java/com/networkdevicemonitor/
│   ├── NetworkDeviceMonitor.java         (Entry point)
│   ├── models/                            (Data models)
│   │   ├── NetworkDevice.java
│   │   ├── DeviceStatus.java
│   │   ├── PingResult.java
│   │   └── LogEntry.java
│   ├── services/                          (Business logic)
│   │   ├── NetworkInterfaceService.java
│   │   ├── PingService.java
│   │   ├── SubnetScannerService.java
│   │   ├── MonitoringService.java
│   │   ├── DeviceRepository.java
│   │   └── LogService.java
│   ├── ui/                                (Swing UI components)
│   │   ├── MainFrame.java
│   │   ├── DeviceTableModel.java
│   │   ├── DeviceStatusCellRenderer.java
│   │   ├── DeviceTablePanel.java
│   │   ├── DeviceDetailsPanel.java
│   │   ├── LogPanel.java
│   │   └── ToolbarPanel.java
│   └── controller/                        (Application controller)
│       └── AppController.java
└── BUILD.md (this file)
```

## Running in VS Code

### Prerequisites
1. Install Java extension for VS Code
2. Install Extension Pack for Java
3. Ensure Java 17+ is installed and VS Code can find it

### Steps
1. Open the workspace: `File > Open Folder > c:\Users\2Much\Desktop\network-java`
2. VS Code should detect the Maven project
3. Right-click pom.xml and select "Run Maven command" or use:
   - Open terminal (Ctrl+`)
   - `mvn clean compile`
   - `mvn exec:java -Dexec.mainClass="com.networkdevicemonitor.NetworkDeviceMonitor"`

### Or use VS Code Task
Press Ctrl+Shift+B to build, then press F5 to debug.

## Running in IntelliJ IDEA

### Steps
1. Open project: `File > Open` → select `c:\Users\2Much\Desktop\network-java`
2. IntelliJ auto-detects Maven project
3. Wait for Maven indexing to complete
4. Right-click `NetworkDeviceMonitor.java` → `Run NetworkDeviceMonitor.main()`

Or use Run Configuration:
1. Top-right: click "Add Configuration"
2. Select "Application"
3. Main class: `com.networkdevicemonitor.NetworkDeviceMonitor`
4. Click Run

## Running in Command Line

### Windows
```cmd
cd c:\Users\2Much\Desktop\network-java
mvn clean compile exec:java -Dexec.mainClass="com.networkdevicemonitor.NetworkDeviceMonitor"
```

### PowerShell
```powershell
cd c:\Users\2Much\Desktop\network-java
mvn clean compile | Out-String
mvn exec:java -Dexec.mainClass="com.networkdevicemonitor.NetworkDeviceMonitor"
```

## Troubleshooting

### Java not found
- Ensure Java 17+ is installed: `java -version`
- Add Java bin directory to PATH environment variable

### Maven not found
- Install Maven from https://maven.apache.org/
- Set MAVEN_HOME environment variable
- Add %MAVEN_HOME%\bin to PATH

### Network detection fails
- This is normal on virtual machines or restricted networks
- The app logs "Could not detect local network interface"
- You can still manually add devices or configure scan range in future updates

### Slow scanning
- First scan may take time as it probes all addresses (1-254 per subnet)
- Subsequent monitoring is faster (only known devices)
- Typical scan: 20-30 seconds for /24 subnet

### Permission issues
- Some network operations may require elevated privileges depending on OS
- Try running as administrator if needed

## Features

### Current
- ✅ Automatic local network detection
- ✅ Subnet scanning with parallel ping
- ✅ Real-time device monitoring (5-second refresh)
- ✅ Live table with color-coded status
- ✅ Device details panel
- ✅ Activity log
- ✅ Manual ping and refresh
- ✅ Background threading (no UI freeze)
- ✅ Responsive UI

### Extensible Architecture
Future additions:
- [ ] Port scanning
- [ ] Performance graphs
- [ ] Alert system
- [ ] Config persistence
- [ ] Export data
- [ ] DNS name resolution
- [ ] Service detection

## Performance Notes
- Parallel scanning uses 20 threads (configurable in SubnetScannerService)
- Default monitoring interval: 5 seconds (configurable)
- Device history limited to 100 latency samples
- Log entries capped at 1000 lines
- All network operations run off EDT (UI never blocks)
