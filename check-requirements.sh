#!/bin/bash

echo "================================"
echo "Download Manager - Requirements Check"
echo "================================"
echo ""

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check Java version
echo "Checking Java version..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    
    if [ "$JAVA_VERSION" == "17" ]; then
        echo -e "${GREEN}✓ Java 17 detected${NC}"
    else
        echo -e "${RED}✗ Java $JAVA_VERSION detected (Java 17 required)${NC}"
        echo "  Download from: https://adoptium.net/"
    fi
else
    echo -e "${RED}✗ Java not found${NC}"
    echo "  Download from: https://adoptium.net/"
fi
echo ""

# Check Android SDK
echo "Checking Android SDK..."
if [ -n "$ANDROID_HOME" ]; then
    echo -e "${GREEN}✓ ANDROID_HOME is set: $ANDROID_HOME${NC}"
elif [ -n "$ANDROID_SDK_ROOT" ]; then
    echo -e "${GREEN}✓ ANDROID_SDK_ROOT is set: $ANDROID_SDK_ROOT${NC}"
else
    echo -e "${YELLOW}⚠ Android SDK environment variable not set${NC}"
    echo "  Set ANDROID_HOME or ANDROID_SDK_ROOT"
fi
echo ""

# Check RAM
echo "Checking system RAM..."
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    TOTAL_RAM=$(free -g | awk '/^Mem:/{print $2}')
    if [ "$TOTAL_RAM" -ge 8 ]; then
        echo -e "${GREEN}✓ ${TOTAL_RAM}GB RAM detected${NC}"
    else
        echo -e "${YELLOW}⚠ Only ${TOTAL_RAM}GB RAM (8GB+ recommended)${NC}"
    fi
elif [[ "$OSTYPE" == "darwin"* ]]; then
    TOTAL_RAM=$(sysctl -n hw.memsize | awk '{print int($1/1024/1024/1024)}')
    if [ "$TOTAL_RAM" -ge 8 ]; then
        echo -e "${GREEN}✓ ${TOTAL_RAM}GB RAM detected${NC}"
    else
        echo -e "${YELLOW}⚠ Only ${TOTAL_RAM}GB RAM (8GB+ recommended)${NC}"
    fi
fi
echo ""

# Check disk space
echo "Checking disk space..."
AVAILABLE_SPACE=$(df -h . | awk 'NR==2 {print $4}' | sed 's/G//')
if [ "${AVAILABLE_SPACE%.*}" -ge 10 ]; then
    echo -e "${GREEN}✓ ${AVAILABLE_SPACE}G available${NC}"
else
    echo -e "${YELLOW}⚠ Only ${AVAILABLE_SPACE}G available (10GB+ recommended)${NC}"
fi
echo ""

# Check Gradle wrapper
echo "Checking Gradle wrapper..."
if [ -f "gradlew" ]; then
    echo -e "${GREEN}✓ Gradle wrapper found${NC}"
    chmod +x gradlew 2>/dev/null
else
    echo -e "${RED}✗ Gradle wrapper not found${NC}"
fi
echo ""

# Check Android Studio (optional)
echo "Checking Android Studio..."
if command -v studio &> /dev/null || [ -d "/Applications/Android Studio.app" ]; then
    echo -e "${GREEN}✓ Android Studio found${NC}"
else
    echo -e "${YELLOW}⚠ Android Studio not detected (recommended)${NC}"
    echo "  Download from: https://developer.android.com/studio"
fi
echo ""

echo "================================"
echo "Summary:"
echo "================================"
echo "If all checks passed (✓), you're ready to build!"
echo "If some checks failed (✗), install the missing requirements."
echo ""
echo "Next steps:"
echo "1. Run: ./build-fix.sh"
echo "2. Or open project in Android Studio"
echo ""
