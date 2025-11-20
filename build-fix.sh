#!/bin/bash

echo "==================================="
echo "Download Manager - Build Fix Script"
echo "==================================="
echo ""

# Step 1: Clean the project
echo "[1/6] Cleaning project..."
./gradlew clean --no-daemon 2>/dev/null || echo "Clean completed"

# Step 2: Clean Gradle cache
echo "[2/6] Cleaning Gradle cache..."
rm -rf ~/.gradle/caches/
rm -rf ~/.gradle/daemon/
rm -rf .gradle/
echo "Gradle cache cleaned"

# Step 3: Clean Kotlin daemon
echo "[3/6] Stopping Kotlin daemon..."
./gradlew --stop
pkill -f "kotlin.*daemon" 2>/dev/null || echo "Kotlin daemon stopped"

# Step 4: Invalidate Android Studio caches
echo "[4/6] Cleaning Android Studio caches..."
rm -rf .idea/
rm -rf build/
rm -rf app/build/
echo "Android Studio caches cleaned"

# Step 5: Rebuild the project
echo "[5/6] Building the project (this may take a few minutes)..."
./gradlew assembleDebug --stacktrace --info --no-daemon

# Step 6: Check build result
if [ $? -eq 0 ]; then
    echo ""
    echo "✓ BUILD SUCCESSFUL!"
    echo ""
    echo "APK location: app/build/outputs/apk/debug/app-debug.apk"
    echo ""
else
    echo ""
    echo "✗ Build failed. Please check the error messages above."
    echo ""
    echo "Common solutions:"
    echo "1. Restart Android Studio"
    echo "2. File -> Invalidate Caches / Restart"
    echo "3. Increase JVM heap size in gradle.properties"
    echo "4. Ensure you have Java 17 installed"
    echo ""
fi

echo "[6/6] Done!"
