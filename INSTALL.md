# Installing PeerLink

## Linux

### Ubuntu / Debian (.deb)
  sudo dpkg -i peerlink_1.0.0_amd64.deb
  peerlink  # launch from terminal, or find in Applications menu

### Fedora / RHEL / openSUSE (.rpm)
  sudo dnf install peerlink-1.0.0-1.x86_64.rpm
  peerlink

### All Linux Distros (AppImage — no install required)
  chmod +x PeerLink-1.0.0-x86_64.AppImage
  ./PeerLink-1.0.0-x86_64.AppImage

### Flatpak (recommended for Fedora Kinoite, Silverblue, Bazzite)
  flatpak install PeerLink.flatpak
  flatpak run com.peerlink.PeerLink

### Snap
  sudo snap install peerlink

## macOS

### DMG (recommended)
  1. Download PeerLink-1.0.0.dmg
  2. Double-click to mount
  3. Drag PeerLink to Applications
  4. First launch: right-click → Open (bypasses Gatekeeper)

### Homebrew
  brew install --cask peerlink

## Windows

### EXE Installer
  1. Download PeerLink-1.0.0-Setup.exe
  2. Double-click and follow installer
  3. If SmartScreen appears: More info → Run anyway

### winget
  winget install ByteForge.PeerLink

### MSI (silent enterprise install)
  msiexec /i PeerLink-1.0.0.msi /qn

## Any Platform (requires Java 21+)
  java -jar peerlink-1.0.0-shaded.jar

## Terminal / CLI (requires JDK 21+)
  java PeerLink.java                           # interactive menu
  java PeerLink.java send /path/to/file        # send mode
  java PeerLink.java receive <invite-code>     # receive mode
