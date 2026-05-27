#!/bin/bash
set -e
echo "PeerLink Linux Build Script"
echo "=========================="

# Check if running on Linux
if [[ "$(uname -s)" != "Linux" ]]; then
    echo "Error: This script is for Linux only"
    exit 1
fi

# Check Java 21+
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed"
    echo "Install OpenJDK 21: sudo apt install openjdk-21-jdk"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d. -f1)
if [[ $JAVA_VERSION -lt 21 ]]; then
    echo "Error: Java 21+ required, found Java $JAVA_VERSION"
    echo "Install OpenJDK 21: sudo apt install openjdk-21-jdk"
    exit 1
fi

echo "✓ Java $JAVA_VERSION detected"

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed"
    echo "Install Maven: sudo apt install maven"
    exit 1
fi
echo "✓ Maven detected"

# Check jpackage
if ! command -v jpackage &> /dev/null; then
    echo "Error: jpackage not found"
    echo "Ensure JDK 21+ is installed and in PATH"
    exit 1
fi
echo "✓ jpackage detected"

# Check required tools for packaging
if ! command -v dpkg-deb &> /dev/null; then
    echo "Warning: dpkg-deb not found - DEB packaging will fail"
    echo "Install: sudo apt install dpkg-dev"
fi

if ! command -v rpmbuild &> /dev/null; then
    echo "Warning: rpmbuild not found - RPM packaging will fail"
    echo "Install: sudo apt install rpm"
fi

if ! command -v appimagetool &> /dev/null; then
    echo "Warning: appimagetool not found - AppImage packaging will fail"
    echo "Install: sudo apt install appimagetool"
fi

# Create dist directory
mkdir -p dist/linux

echo "Building PeerLink..."

# Build the project
mvn clean package -DskipTests --batch-mode

echo "Build completed successfully!"
echo "Installers created in dist/linux/"

echo "Available packages:" 
find dist/linux -type f -name "*.deb" -o -name "*.rpm" -o -name "*.AppImage" | sort | while read file; do
    size=$(du -h "$file" | cut -f1)
    echo "  $size  $(basename "$file")"
done

echo ""
echo "Installation instructions:"
echo "  DEB (Debian/Ubuntu): sudo dpkg -i dist/linux/*.deb"
echo "  RPM (Fedora/RHEL): sudo dnf install dist/linux/*.rpm"
echo "  AppImage: chmod +x dist/linux/*.AppImage && ./dist/linux/*.AppImage"