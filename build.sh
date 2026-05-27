#!/bin/bash
set -e

# 1. Detects current OS
OS=$(uname -s)
case "$OS" in
  Linux*)  PLATFORM="linux" ;;
  Darwin*) PLATFORM="mac"   ;;
  MINGW*|CYGWIN*|MSYS*) PLATFORM="windows" ;;
  *) echo "Unsupported OS: $OS"; exit 1 ;;
esac

# 2. Checks Java version is 21+
JAVA_VER=$(java -version 2>&1 | awk -F'"' '/version/ {print $2}' | cut -d. -f1)
if [ "$JAVA_VER" -lt 21 ]; then
  echo "ERROR: Java 21+ required. Found: $JAVA_VER"
  echo "Install from: https://adoptium.net/"
  exit 1
fi

# 3. Checks Maven is available
if ! command -v mvn &>/dev/null; then
  echo "ERROR: Maven is required."
  exit 1
fi

# 4. Checks jpackage is available
if ! command -v jpackage &>/dev/null; then
  echo "ERROR: jpackage is required."
  exit 1
fi

# 5. Creates dist/ directory structure
mkdir -p dist/linux dist/mac dist/windows

# 6. Builds fat JAR
echo "Building Fat JAR..."
mvn clean package -DskipTests --batch-mode

if [ "$PLATFORM" = "linux" ]; then
  # 7. On Linux: runs all Linux packaging steps from Section 6
  echo "Building for Linux..."
  make linux-all
elif [ "$PLATFORM" = "mac" ]; then
  # 8. On macOS: runs all macOS packaging steps from Section 7
  echo "Building for macOS..."
  make mac-all
  echo "Ad-hoc signing macOS binaries..."
  codesign --force --deep --sign - dist/mac/PeerLink-1.0.0.app || true
  codesign --force --sign - dist/mac/PeerLink-1.0.0.dmg || true
elif [ "$PLATFORM" = "windows" ]; then
  # 9. On Windows
  echo "Building for Windows... Please run this on Windows using PowerShell or adapt to Git Bash."
  jpackage --type exe --input target --name PeerLink --main-jar peerlink-1.0.0-shaded.jar --main-class peerlink.Main --app-version 1.0.0 --vendor "ByteForge" --description "Secure P2P File Transfer" --copyright "Copyright 2025 ByteForge" --icon src/main/resources/icon.ico --win-dir-chooser --win-menu --win-menu-group "PeerLink" --win-shortcut --win-shortcut-prompt --win-upgrade-uuid "a1b2c3d4-e5f6-7890-abcd-ef1234567890" --java-options "--add-opens java.base/java.lang=ALL-UNNAMED" --java-options "-Xmx512m" --java-options "-Xms64m" --dest dist/windows --verbose
  jpackage --type msi --input target --name PeerLink --main-jar peerlink-1.0.0-shaded.jar --main-class peerlink.Main --app-version 1.0.0 --vendor "ByteForge" --icon src/main/resources/icon.ico --win-dir-chooser --win-menu --win-menu-group "PeerLink" --win-shortcut --win-upgrade-uuid "a1b2c3d4-e5f6-7890-abcd-ef1234567890" --java-options "--add-opens java.base/java.lang=ALL-UNNAMED" --java-options "-Xmx512m" --dest dist/windows --verbose
fi

# 10. Computes SHA256 checksums
echo "Computing checksums..."
make checksums || true

# 11. Prints summary table
echo "════════════════════════════════════"
echo " PeerLink Build Complete"
echo "════════════════════════════════════"
find dist/ -type f | while read f; do
  SIZE=$(du -sh "$f" | cut -f1)
  echo "  $SIZE  $f"
done
