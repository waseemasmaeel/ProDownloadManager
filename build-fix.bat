@echo off
echo ===================================
echo Download Manager - Build Fix Script
echo ===================================
echo.

REM Step 1: Clean the project
echo [1/6] Cleaning project...
call gradlew.bat clean --no-daemon
echo Clean completed
echo.

REM Step 2: Clean Gradle cache
echo [2/6] Cleaning Gradle cache...
rmdir /s /q %USERPROFILE%\.gradle\caches 2>nul
rmdir /s /q %USERPROFILE%\.gradle\daemon 2>nul
rmdir /s /q .gradle 2>nul
echo Gradle cache cleaned
echo.

REM Step 3: Stop Gradle daemon
echo [3/6] Stopping Gradle daemon...
call gradlew.bat --stop
echo Gradle daemon stopped
echo.

REM Step 4: Clean build directories
echo [4/6] Cleaning build directories...
rmdir /s /q build 2>nul
rmdir /s /q app\build 2>nul
rmdir /s /q .idea 2>nul
echo Build directories cleaned
echo.

REM Step 5: Build the project
echo [5/6] Building the project (this may take a few minutes)...
call gradlew.bat assembleDebug --stacktrace --info --no-daemon

REM Step 6: Check result
if %ERRORLEVEL% EQU 0 (
    echo.
    echo BUILD SUCCESSFUL!
    echo.
    echo APK location: app\build\outputs\apk\debug\app-debug.apk
    echo.
) else (
    echo.
    echo Build failed. Please check the error messages above.
    echo.
    echo Common solutions:
    echo 1. Restart Android Studio
    echo 2. File -^> Invalidate Caches / Restart
    echo 3. Increase JVM heap size in gradle.properties
    echo 4. Ensure you have Java 17 installed
    echo.
)

echo [6/6] Done!
pause
