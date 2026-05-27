# PROJECT ANALYSIS

## 1. Project Summary

PeerLink is a secure peer-to-peer file transfer application that enables direct file sharing between devices on the same network without requiring any central server or cloud storage. The application uses end-to-end encryption to ensure file transfers are private and secure.

Key features:
- Drag-and-drop file sending
- Invite code system for receiving files
- Real-time transfer progress with speed and ETA
- Cross-platform support (Windows, Linux, macOS)
- No account or registration required
- End-to-end encryption using ECDH key exchange and AES-256-GCM

Conceptually, file transfer works by:
1. Sender selects a file and generates an invite code containing their IP address and port
2. Receiver enters the invite code to establish a direct TCP connection
3. ECDH key exchange establishes a secure session
4. File is transferred with AES-256-GCM encryption
5. Progress is displayed in real-time with speed and ETA calculations

## 2. Project Architecture

### Folder Structure

```
src/main/java/com/peerlink/
├── ui/
│   ├── Main.java
│   ├── MainController.java
│   └── Launcher.java
├── security/
│   ├── HandshakeManager.java
│   └── CryptoUtils.java
└── logic/
    ├── TransferStats.java
    ├── PortUtils.java
    ├── NetworkUtils.java
    ├── InviteCode.java
    ├── FileSender.java
    └── FileReceiver.java
```

### JavaFX UI Flow

The application follows a standard JavaFX pattern:
- `Main.java` extends `Application` and serves as the entry point
- `MainView.fxml` defines the UI layout with two main panels (Send/Receive)
- `MainController.java` handles all UI events and business logic
- FXML file references controller with `fx:controller="com.peerlink.ui.MainController"`

### Controllers + FXML Mapping

The UI is defined in `src/main/resources/com/peerlink/ui/MainView.fxml` and controlled by `MainController.java`. Key components:
- Send panel with drag-and-drop zone and file selection
- Receive panel with invite code input
- Status panel with progress bar and transfer metrics
- Action buttons (Send, Receive, Cancel, Copy, Paste)

### Service / Utility Layer

Located in `com.peerlink.logic` package:
- `TransferStats`: Tracks transfer speed, progress, and ETA
- `PortUtils`: Finds available ports for file transfer
- `NetworkUtils`: Handles network interface discovery
- `InviteCode`: Generates and parses invite codes (IP:port format)

### Networking Layer

The application uses direct TCP sockets for peer-to-peer communication:
- `FileSender.java`: Listens for incoming connections and sends files
- `FileReceiver.java`: Connects to sender and receives files
- Both use `HandshakeManager` for secure session establishment

### Crypto Layer

Located in `com.peerlink.security`:
- `CryptoUtils`: Provides AES-256-GCM encryption/decryption
- `HandshakeManager`: Implements ECDH key exchange for forward secrecy
- Uses JDK's built-in `javax.crypto` without external dependencies

## 3. Entry Point Verification

The actual entry point class is `com.peerlink.ui.Main` located at `src/main/java/com/peerlink/ui/Main.java`.

Key details:
- Package: `com.peerlink.ui`
- Class: `Main` extends `javafx.application.Application`
- Main method: `public static void main(String[] args)`
- Start method: Overrides `start(Stage stage)` to load FXML and display UI

The entry point correctly launches the JavaFX stage by:
1. Loading `MainView.fxml` using `getClass().getResource("MainView.fxml")`
2. Creating `FXMLLoader` with the FXML resource
3. Setting up scene with 700x500 dimensions
4. Configuring stage title and scene
5. Displaying the stage with `stage.show()`

## 4. Resource Loading Check

The code uses proper resource loading methods that will work in packaged mode:

- FXML loading: `getClass().getResource("MainView.fxml")` - ✅ Correct
- CSS loading: Referenced in FXML as `@styles.css` - ✅ Correct
- Images: Not currently used beyond potential icon loading in packaging

All resource references are relative and use the class loader, which will work correctly when packaged in a JAR file. No absolute paths are used.

## 5. Build System Status

### Dependencies
- JavaFX Controls 21.0.3
- JavaFX FXML 21.0.3
- No external dependencies (uses JDK crypto)

### Plugins
- Maven Compiler Plugin 3.11.0
- Maven Shade Plugin 3.5.0
- JavaFX Maven Plugin 0.0.8
- Maven Resources Plugin 3.3.1
- Exec Maven Plugin 3.1.0

### Build Status
1. `mvn clean compile` - WORKS
2. `mvn clean package -Pjar-only` - WORKS (shaded JAR created with 4858 classes)
3. `mvn javafx:run` - WORKS (correct way to run during development)
4. Native packaging - Requires system tools (dpkg-deb, rpmbuild, WiX)

### Key Finding: Standalone JAR Limitation
The shaded JAR cannot be run with `java -jar` on Java 25 due to JavaFX runtime detection issues. This is a known limitation of Java 25 with modular JavaFX. Use `mvn javafx:run` for development or use jpackage to create native installers.