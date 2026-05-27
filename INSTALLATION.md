# INSTALLATION GUIDE

## Linux

### Installing DEB Package (Debian/Ubuntu)

1. Download the `.deb` file from the releases page
2. Open terminal and navigate to the download directory
3. Install the package:
   ```bash
   sudo dpkg -i PeerLink-1.0.0.deb
   ```
4. If you get dependency errors, fix them:
   ```bash
   sudo apt install -f
   ```
5. Launch the application:
   - From terminal: `peerlink`
   - From Applications menu: Search for "PeerLink"

### Installing RPM Package (Fedora/RHEL/openSUSE)

1. Download the `.rpm` file from the releases page
2. Open terminal and navigate to the download directory
3. Install the package:
   ```bash
   # Fedora/RHEL
   sudo dnf install PeerLink-1.0.0.rpm
   
   # openSUSE
   sudo zypper install PeerLink-1.0.0.rpm
   ```
4. Launch the application:
   - From terminal: `peerlink`
   - From Applications menu: Search for "PeerLink"

### Installing AppImage (All Linux Distros)

1. Download the `.AppImage` file from the releases page
2. Make it executable:
   ```bash
   chmod +x PeerLink-1.0.0-x86_64.AppImage
   ```
3. Run the application:
   ```bash
   ./PeerLink-1.0.0-x86_64.AppImage
   ```
4. To create a desktop shortcut:
   - Right-click the AppImage and select "Add to Desktop"
   - Or use AppImageLauncher for better integration

### Where App Icon Appears

After installation, the PeerLink icon will appear in your applications menu under:
- **Category**: Network
- **Subcategory**: File Transfer

The exact location depends on your desktop environment:
- **GNOME**: Activities → Show Applications → Network
- **KDE**: Application Menu → Internet → File Transfer
- **XFCE**: Applications → Internet
- **Unity**: Dash → Search for "PeerLink"

## Windows

### Running EXE Installer

1. Download the `.exe` file from the releases page
2. Double-click the installer file
3. If Windows SmartScreen appears:
   - Click "More info"
   - Click "Run anyway"
4. Follow the installation wizard:
   - Choose installation directory
   - Select whether to create desktop shortcut
   - Choose start menu folder
5. Complete the installation

### Installing MSI Package

1. Download the `.msi` file from the releases page
2. Double-click to start installation
3. Or install silently from command line:
   ```cmd
   msiexec /i PeerLink-1.0.0.msi
   ```
4. For enterprise deployment:
   ```cmd
   msiexec /i PeerLink-1.0.0.msi /qn
   ```

### Where Shortcut Appears

After installation, shortcuts will be created in:

1. **Start Menu**:
   - Open Start Menu
   - Look for "PeerLink" folder
   - Contains: PeerLink application and uninstaller

2. **Desktop** (if selected during installation):
   - Shortcut named "PeerLink"
   - Can be moved or deleted without affecting installation

3. **Quick Launch** (Windows 10/11):
   - Right-click taskbar
   - Select "PeerLink" from the list
   - Can be pinned to taskbar

### First Launch

When you first launch PeerLink:
1. The application will appear in your system tray
2. Main window opens with send/receive interface
3. No configuration needed - ready to use immediately
4. To uninstall:
   - Windows 10/11: Settings → Apps → PeerLink → Uninstall
   - Or use the uninstaller in Start Menu

## Verification

To verify your installation is working:

1. Launch PeerLink
2. The main window should appear with:
   - "Send File" panel on the left
   - "Receive File" panel on the right
   - Status bar at the bottom
3. Try dragging a small file to the drop zone
4. Check that the UI is responsive and themed correctly

## Troubleshooting

### Application Won't Launch

- **Linux**: Ensure you have Java 21+ installed
  ```bash
  java -version
  ```
- **Windows**: Check that Visual C++ Redistributable is installed
- **Both**: Try running from terminal/command prompt to see error messages

### SmartScreen Blocking Installation (Windows)

This is normal for new applications:
1. Click "More info"
2. Click "Run anyway"
3. The application is open source and can be audited

### Missing Dependencies (Linux)

If you get library errors:
```bash
# Install common dependencies
sudo apt install libfuse2  # For AppImage
sudo apt install libxss1   # For screen capture
```

### Firewall Blocking

PeerLink uses random high-numbered ports for file transfer. If transfers fail:
- **Windows**: Allow PeerLink through Windows Defender Firewall
- **Linux**: Ensure your firewall allows outgoing connections
- **Both**: No inbound rules needed - connections are initiated by receiver