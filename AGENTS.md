# PeerLink Developer Notes

## Build Commands

```bash
# Build shaded JAR
mvn clean package -DskipTests

# Run the application
java -jar target/peerlink-1.0.0-shaded.jar

# Build native installers (by OS)
mvn clean install -P linux   # .deb, .rpm in dist/linux/
mvn clean install -P mac    # .dmg, .pkg in dist/mac/
mvn clean install -P windows # .exe, .msi in dist/windows/
```

## Key Details

- **Java version**: 21 (required for JavaFX 21 and jpackage)
- **Framework**: JavaFX 21 (Maven managed)
- **Main class**: `peerlink.Main` (defined in pom.xml manifest)
- **No tests**: No test sources exist in this repo

## Architecture

- Entry point: `src/main/java/com/peerlink/ui/Main.java`
- UI logic: `MainController.java` + FXML (`src/main/resources/com/peerlink/ui/MainView.fxml`)
- Security: `security/` package (ECDH handshake, AES-256-GCM)
- Transfer logic: `logic/` package (FileSender, FileReceiver, NetworkUtils)

## Native Packaging Requirements

- **Linux**: `fakeroot`, `dpkg-deb` (for .deb), `rpmbuild` (for .rpm), `appimagetool` (for AppImage)
- **macOS**: Xcode command line tools
- **Windows**: WiX Toolset (for MSI)

## CI

Release builds on tag push via `.github/workflows/release.yml`. Uses Maven with Java 21 (Temurin).