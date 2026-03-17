# Running Network Device Monitor in VS Code

## Prerequisites

1. **VS Code** installed
2. **Extension Pack for Java** by Microsoft (VS Code will suggest this)
3. **Java 17+** installed on your system
4. **Maven 3.8+** (recommended) OR use javac directly

## Setup Steps

### Step 1: Install Java Extension
1. Open VS Code
2. Go to **Extensions** (Ctrl+Shift+X)
3. Search for "Extension Pack for Java"
4. Click **Install** (by Microsoft)
5. Reload VS Code

### Step 2: Open the Project
1. **File** → **Open Folder**
2. Navigate to: `c:\Users\2Much\Desktop\network-java`
3. Click **Select Folder**
4. VS Code will auto-detect the Maven project

### Step 3: Wait for Indexing
- VS Code will download dependencies and indexes the project
- Wait for "Java Language Server" notification to complete
- Should take 30-60 seconds on first open

---

## Method 1: Use Integrated Terminal

### Compile & Run
1. Press **Ctrl + `** to open integrated terminal
2. Paste this command:
   ```bash
   javac -d bin -sourcepath "src/main/java" (Get-ChildItem -Path "src/main/java" -Recurse -Filter "*.java" -File | ForEach-Object { $_.FullName })
   ```
3. Then run:
   ```bash
   java -cp bin com.networkdevicemonitor.NetworkDeviceMonitor
   ```

### Or use Maven
1. Terminal: Ctrl + `
2. Run:
   ```bash
   mvn clean compile exec:java -Dexec.mainClass="com.networkdevicemonitor.NetworkDeviceMonitor"
   ```

---

## Method 2: Use Run Configuration

### Create a Run Configuration
1. Click the **Run & Debug** icon (Ctrl+Shift+D)
2. Click **create a launch.json file**
3. Select **Java** environment
4. Choose **"Application"** (or main class)
5. Paste this configuration:

```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Network Device Monitor",
            "request": "launch",
            "mainClass": "com.networkdevicemonitor.NetworkDeviceMonitor",
            "projectName": "network-device-monitor",
            "cwd": "${workspaceFolder}"
        }
    ]
}
```

Then:
1. Press **F5** to run
2. App starts in debug mode
3. Use VS Code debugger if needed

---

## Method 3: Use Maven Explorer

If Maven is installed:

1. Look for **Maven** icon in left sidebar (or Ctrl+Shift+M)
2. Navigate to the project
3. Expand **network-device-monitor**
4. Right-click **Plugins** → **exec** → **java**
5. Choose the exec:java goal
6. Or manually: right-click and select **Run Maven command**

---

## Method 4: Use Task Runner

### Create Tasks
1. **Terminal** → **Configure Tasks** → **Create tasks.json from template**
2. Select **Maven** if available, or **Others**
3. Add this task:

```json
{
    "version": "2.0.0",
    "tasks": [
        {
            "label": "Compile & Run Network Monitor",
            "type": "shell",
            "command": "mvn",
            "args": [
                "clean",
                "compile",
                "exec:java",
                "-Dexec.mainClass=com.networkdevicemonitor.NetworkDeviceMonitor"
            ],
            "group": {
                "kind": "build",
                "isDefault": true
            },
            "presentation": {
                "reveal": "always"
            },
            "problemMatcher": []
        },
        {
            "label": "Compile Only",
            "type": "shell",
            "command": "mvn",
            "args": ["clean", "compile"],
            "presentation": {
                "reveal": "always"
            }
        }
    ]
}
```

Then:
- Press **Ctrl+Shift+B** to run default task
- Or **Terminal** → **Run Task** to pick which one

---

## Debugging in VS Code

### Set Breakpoints
1. Open any Java file
2. Click in the left margin to set a breakpoint (red dot appears)
3. Breakpoints common in:
   - `AppController.java` - For control flow
   - `MonitoringService.java` - For monitoring logic
   - `PingService.java` - For network ops

### Debug Mode
1. **F5** to start debugger
2. App stops at breakpoints
3. Use **Debug Console** (Ctrl+Shift+Y) to inspect:
   - Variables panel shows locals and fields
   - Call stack shows execution path
   - Hover over variables for value

### Debug Controls
- **Continue** (F5): Resume execution
- **Step Over** (F10): Execute current line
- **Step Into** (F11): Enter function call
- **Step Out** (Shift+F11): Exit function

---

## Troubleshooting in VS Code

### "Java not recognized"
1. Check command palette: **Ctrl+Shift+P**
2. Type: **Java: Configure Java Runtime**
3. Ensure Java 17+ is listed and set as default

### "Maven not found"
1. Terminal: `mvn -v` to check
2. Install Maven: https://maven.apache.org/download.cgi
3. Add Maven bin to system PATH
4. Reload VS Code

### "Cannot find main class"
1. Rebuild: **Ctrl+Shift+B**
2. Check that compile succeeded (no errors)
3. Verify classpath: `java -cp bin -version` (should work)

### Code Red Underlines
- These are usually false positives
- Click the lightbulb icon for quick fix suggestions
- Or reload workspace: **Ctrl+K Ctrl+Ctrl+L**

### Very Slow Indexing
- Close other projects in workspace
- Disable unused extensions
- Increase heap: Settings → Java: Configure Boot Runtime

---

## Tips & Tricks

### Quick Compile
- **Ctrl+Shift+B** - Builds project (if tasks configured)
- VS Code auto-saves (Ctrl+S manually if needed)

### Code Navigation
- **Ctrl+Click** on class name to go to definition
- **Ctrl+Shift+O** to go to symbol
- **F12** to peek definition

### Formatting
- **Shift+Alt+F** to format document
- Right-click → Format Selection

### Terminal Shortcuts
- **Ctrl+`` ** - Toggle terminal
- **Ctrl+Shift+`` ** - New terminal
- Click terminal tab to switch

### Run Recent
- **Ctrl+F5** - Run last configuration again
- Saves clicking through menus

---

## Project Structure in Explorer

VS Code file tree will show:
```
network-java
├── bin/ (compiled classes)
├── src/
│   └── main/java/com/networkdevicemonitor/
│       ├── models/
│       ├── services/
│       ├── ui/
│       ├── controller/
│       └── NetworkDeviceMonitor.java
├── target/ (Maven build, ignored by default)
├── pom.xml
├── build.bat
├── build.sh
├── README.md
└── QUICKSTART.md
```

---

## Recommended Extensions

Install these for better experience:
- **Extension Pack for Java** (Microsoft) - Primary
- **Maven for Java** (Microsoft) - Maven support
- **Debugger for Java** (Microsoft) - Debugging
- **Project Manager** (Alessandro Fragnani) - Multi-project support
- **Better Comments** (Aaron Bond) - Comment highlighting

---

## Performance Notes

- First startup: ~5-10 seconds (language server boot)
- Compilation: ~2-3 seconds
- App runtime: Smooth, responsive UI

---

## Common Workflows

### Daily Development
```
1. Open folder
2. Make code changes
3. Ctrl+Shift+B to compile
4. F5 to run/debug
5. Check debug output
```

### Testing Changes
```
1. Edit service/UI class
2. Ctrl+S auto-saves
3. F5 to restart
4. Observe behavior
```

### Performance Profiling
```
1. Run with: -Xmx512m -XX:+PrintGCDetails
2. Not configured by default - edit launch.json vmArgs
3. Check Terminal output for GC logs
```

---

## Next Steps

- Read [QUICKSTART.md](QUICKSTART.md) for feature overview
- Check [README.md](README.md) for architecture details
- Explore source in VS Code
- Try the debugger on MonitoringService

**Happy hacking!** 🚀
