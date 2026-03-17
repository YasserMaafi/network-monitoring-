#!/bin/bash

# Network Device Monitor - Build Script for Linux/Mac
# This script compiles and runs the application

echo ""
echo "===== Network Device Monitor Build Script ====="
echo ""

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven not found. Please install Maven or add it to PATH."
    echo "Visit: https://maven.apache.org/download.cgi"
    exit 1
fi

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "ERROR: Java not found. Please install Java 17+ or add it to PATH."
    exit 1
fi

# Show Java version
echo "Java version:"
java -version
echo ""

# Parse command line arguments
case "${1:-}" in
    "")
        TARGET="clean compile exec:java -Dexec.mainClass=com.networkdevicemonitor.NetworkDeviceMonitor"
        ;;
    "clean")
        TARGET="clean"
        echo "Cleaning build artifacts..."
        ;;
    "compile")
        TARGET="compile"
        echo "Compiling..."
        ;;
    "package")
        TARGET="clean package"
        echo "Packaging JAR..."
        ;;
    "run")
        echo "Running application..."
        java -cp target/classes com.networkdevicemonitor.NetworkDeviceMonitor
        exit $?
        ;;
    *)
        echo "Unknown option: $1"
        echo ""
        echo "Usage:"
        echo "  ./build.sh              - Build and run"
        echo "  ./build.sh clean        - Clean only"
        echo "  ./build.sh compile      - Compile only"
        echo "  ./build.sh package      - Create JAR"
        echo "  ./build.sh run          - Run compiled app"
        exit 1
        ;;
esac

# Run Maven
echo "Running: mvn $TARGET"
echo ""
mvn $TARGET

if [ $? -eq 0 ]; then
    echo ""
    echo "===== Build Successful ====="
    echo ""
    if [ "$1" = "package" ]; then
        echo "JAR created at: target/network-device-monitor.jar"
        echo "Run with: java -jar target/network-device-monitor.jar"
    fi
else
    echo ""
    echo "===== Build Failed ====="
    echo ""
    exit 1
fi
