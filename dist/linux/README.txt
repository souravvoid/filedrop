PeerLink for Linux — Installation Instructions

Option A: DEB Package (Debian, Ubuntu, Mint, Pop!_OS)
  sudo dpkg -i peerlink_1.0.0_amd64.deb
  (If there are dependency issues, run: sudo apt-get install -f)
  Launch via your application menu or by running `peerlink` in terminal.

Option B: RPM Package (Fedora, CentOS, RHEL, openSUSE)
  sudo dnf install peerlink-1.0.0-1.x86_64.rpm
  Launch via application menu.

Option C: AppImage (Universal, Portable)
  No installation required.
  chmod +x PeerLink-1.0.0-x86_64.AppImage
  ./PeerLink-1.0.0-x86_64.AppImage

Option D: Flatpak (Universal, Sandboxed)
  flatpak install PeerLink.flatpak
  flatpak run com.peerlink.PeerLink
