@echo off
REM Network Device Monitor - Build Script for Windows
REM This script compiles and runs the application

setlocal enabledelayedexpansion

echo.
echo ===== Network Device Monitor Build Script =====
echo.

REM Check if Maven is available
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven not found. Please install Maven or add it to PATH.
    echo Visit: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

REM Check if Java is available
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java not found. Please install Java 17+ or add it to PATH.
    pause
    exit /b 1
)

REM Show Java version
echo Java version:
java -version 2>&1
echo.

REM Parse command line arguments
if "%1"=="" (
    echo Usage:
    echo   build.bat              - Build and run
    echo   build.bat clean        - Clean only
    echo   build.bat compile      - Compile only
    echo   build.bat package      - Create JAR
    echo   build.bat run          - Run compiled app
    echo.
    set TARGET=compile exec:java -Dexec.mainClass=com.networkdevicemonitor.NetworkDeviceMonitor
) else if "%1"=="clean" (
    set TARGET=clean
    echo Cleaning build artifacts...
) else if "%1"=="compile" (
    set TARGET=compile
    echo Compiling...
) else if "%1"=="package" (
    set TARGET=clean package
    echo Packaging JAR...
) else if "%1"=="run" (
    echo Running application...
    java -cp target\classes com.networkdevicemonitor.NetworkDeviceMonitor
    exit /b
) else (
    echo Unknown option: %1
    exit /b 1
)

REM Run Maven
echo Running: mvn %TARGET%
echo.
mvn %TARGET%

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ===== Build Successful =====
    echo.
    if "%1"=="package" (
        echo JAR created at: target\network-device-monitor.jar
        echo Run with: java -jar target\network-device-monitor.jar
    )
) else (
    echo.
    echo ===== Build Failed =====
    echo.
)

pause
