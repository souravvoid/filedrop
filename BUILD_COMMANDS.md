# BUILD COMMANDS

## Linux Commands

### Build JAR
```bash
mvn clean package -DskipTests --batch-mode
```
- Output: `target/peerlink-1.0.0-shaded.jar`
- Purpose: Create a fat JAR for development testing

### Build DEB (Debian/Ubuntu)
```bash
mvn clean package -DskipLinux=false
# or simply
mvn clean package  # on Linux
```
- Output: `dist/linux/PeerLink-1.0.0.deb`
- Requires: `fakeroot` and `dpkg-deb` tools
- Note: Automatically activated by Linux OS profile

### Build RPM (Fedora/RHEL/openSUSE)
```bash
mvn clean package -DskipLinux=false
# or simply
mvn clean package  # on Linux
```
- Output: `dist/linux/PeerLink-1.0.0.rpm`
- Requires: `rpm-build` tools
- Note: Automatically activated by Linux OS profile

### Build AppImage (All Linux Distros)
```bash
mvn clean package -DskipLinux=false
# or simply
mvn clean package  # on Linux
```
- Output: `dist/linux/PeerLink-1.0.0-x86_64.AppImage`
- Requires: `appimagetool` installed
- Note: AppImage is generated as part of the Linux packaging

### Run Packaged Output
```bash
# Run DEB package
sudo dpkg -i dist/linux/PeerLink-1.0.0.deb
peerlink

# Run RPM package
sudo dnf install dist/linux/PeerLink-1.0.0.rpm
peerlink

# Run AppImage
chmod +x dist/linux/PeerLink-1.0.0-x86_64.AppImage
./dist/linux/PeerLink-1.0.0-x86_64.AppImage

# Run JAR directly
java -jar target/peerlink-1.0.0-shaded.jar
```

## Windows Commands

### Build EXE
```cmd
mvn clean package -DskipWindows=false
# or simply
mvn clean package  # on Windows
```
- Output: `dist/windows/PeerLink-1.0.0.exe`
- Requires: WiX Toolset for MSI generation
- Note: Automatically activated by Windows OS profile

### Build MSI
```cmd
mvn clean package -DskipWindows=false
# or simply
mvn clean package  # on Windows
```
- Output: `dist/windows/PeerLink-1.0.0.msi`
- Requires: WiX Toolset
- Note: MSI is generated alongside EXE when Windows profile is active

### Run Packaged Output
```cmd
# Run EXE installer
PeerLink-1.0.0.exe

# Run MSI installer
msiexec /i PeerLink-1.0.0.msi

# Run after installation
Start Menu → PeerLink → PeerLink

# Run JAR directly
java -jar target\peerlink-1.0.0-shaded.jar
```

## Expected Output Folder Paths

```
dist/
├── linux/
│   ├── PeerLink-1.0.0.deb
│   ├── PeerLink-1.0.0.rpm
│   └── PeerLink-1.0.0-x86_64.AppImage
└── windows/
    ├── PeerLink-1.0.0.exe
    └── PeerLink-1.0.0.msi
```

## Build Prerequisites

### Linux
- Java 21+ JDK
- Apache Maven 3.6+
- jpackage (included with JDK 14+)
- For DEB: `fakeroot`, `dpkg-deb`
- For RPM: `rpm-build`, `rpmbuild`
- For AppImage: `appimagetool`

### Windows
- Java 21+ JDK
- Apache Maven 3.6+
- jpackage (included with JDK 14+)
- WiX Toolset (for MSI generation)

## Build Process Flow

1. Clean previous builds: `mvn clean`
2. Compile and package: `mvn package`
3. Maven automatically:
   - Compiles Java sources
   - Packages into shaded JAR
   - Activates OS-specific jpackage execution
   - Generates native installer
4. Output installers in platform-specific directories

## Verification Commands

```bash
# Verify JAR was created
ls -la target/peerlink-*-shaded.jar

# Verify Linux packages
ls -la dist/linux/

# Verify Windows packages
dir dist\windows\

# Check Java version
java -version

# Check Maven version
mvn -version
```

## Troubleshooting

### Common Issues

**jpackage not found**
- Ensure JDK 14+ is installed
- Verify `jpackage` is in PATH
- On Ubuntu: `sudo apt install openjdk-21-jdk`

**Missing build tools**
- Linux DEB: `sudo apt install fakeroot dpkg-dev`
- Linux RPM: `sudo dnf install rpm-build rpm-devel`
- Windows MSI: Install WiX Toolset from https://wixtoolset.org/

**Icon not found**
- Ensure `src/main/resources/icon.png` (Linux) and `src/main/resources/icon.ico` (Windows) exist
- Run `./create_icons.sh` to generate icons

**Permission denied on AppImage**
- Make executable: `chmod +x PeerLink-1.0.0-x86_64.AppImage`

**SmartScreen blocking Windows installer**
- Click "More info" → "Run anyway"
- This is normal for first-time developers