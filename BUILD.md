# PeerLink Build and Deployment Guide

## Prerequisites
To build and package PeerLink natively, you must have the following installed to your path:
1. `Java Development Kit 17+` (Provides `jpackage`)
2. `JavaFX 21+` (Automatically satisfied via Maven)
3. `Maven 3.x`
4. OS-Specific Build tools:
   - **Windows**: Install the [Wix Toolset](https://wixtoolset.org/) (for MSI output).
   - **Linux**: Install `fakeroot` and `dpkg-deb` (for `deb`), or `rpmbuild` (for `rpm`).
   - **macOS**: Installed XCode command line tools.

## Developer Execution
To test the active build locally without waiting on slow installer generation routines, execute:
```bash
mvn clean package
java -jar target/peerlink-1.0-SNAPSHOT-shaded.jar
```

## Creating Native Installers 
PeerLink employs native profiles embedded directly inside `pom.xml`. Calling `mvn install` combined with a profile flag generates an isolated Application Bundle containing the Java Runtime and PeerLink within `target/installers/`. You do not need Java installed on target machines to run these installers!

### Windows
```powershell
mvn clean install -P windows
```
- Locates in: `target/installers/PeerLink-1.0.exe`

### Linux
```bash
mvn clean install -P linux
```
- Locates in: `target/installers/peerlink_1.0_amd64.deb` and `target/installers/peerlink-1.0-1.x86_64.rpm`

### macOS
```bash
mvn clean install -P mac
```
- Locates in: `target/installers/PeerLink-1.0.dmg`
