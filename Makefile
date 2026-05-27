.PHONY: all clean jar linux-deb linux-rpm linux-appimage linux-flatpak linux-all mac-dmg mac-pkg mac-all windows-exe windows-msi windows-all icons checksums help

APP_NAME    = PeerLink
VERSION     = 1.0.0
MAIN_CLASS  = peerlink.Main
JAR         = target/peerlink-$(VERSION)-shaded.jar
DIST_LINUX  = dist/linux
DIST_MAC    = dist/mac
DIST_WIN    = dist/windows

help:
	@echo "Available targets:"
	@echo "  all             - Build everything (depends on OS)"
	@echo "  clean           - Clean maven build and dist/ directory"
	@echo "  icons           - Generate icons for all platforms"
	@echo "  jar             - Build the shaded fat JAR"
	@echo "  linux-deb       - Build Debian package (.deb)"
	@echo "  linux-rpm       - Build RPM package (.rpm)"
	@echo "  linux-appimage  - Build AppImage package"
	@echo "  linux-flatpak   - Build Flatpak package"
	@echo "  linux-all       - Build all Linux packages"
	@echo "  mac-dmg         - Build macOS DMG installer"
	@echo "  mac-pkg         - Build macOS PKG installer"
	@echo "  mac-all         - Build all macOS packages"
	@echo "  windows-exe     - Build Windows EXE installer"
	@echo "  windows-msi     - Build Windows MSI installer"
	@echo "  windows-all     - Build all Windows packages"
	@echo "  checksums       - Generate SHA256 checksums for all dist artifacts"

all: icons jar linux-all mac-all windows-all checksums

clean:
	mvn clean
	rm -rf dist/

icons:
	bash ./create_icons.sh

jar:
	mvn clean package -DskipTests --batch-mode

linux-deb: jar
	mkdir -p $(DIST_LINUX)
	jpackage \
		--type deb \
		--input target \
		--name $(APP_NAME) \
		--main-jar peerlink-$(VERSION)-shaded.jar \
		--main-class $(MAIN_CLASS) \
		--app-version $(VERSION) \
		--vendor "ByteForge" \
		--description "Secure P2P File Transfer — No Server Required" \
		--copyright "Copyright 2025 ByteForge" \
		--icon src/main/resources/icon.png \
		--linux-package-name peerlink \
		--linux-app-category net \
		--linux-menu-group "Network;FileTransfer;" \
		--linux-shortcut \
		--linux-deb-maintainer "semwalsourav4@gmail.com" \
		--java-options "--add-opens java.base/java.lang=ALL-UNNAMED" \
		--java-options "-Xmx512m" \
		--java-options "-Xms64m" \
		--dest $(DIST_LINUX) \
		--verbose

linux-rpm: jar
	mkdir -p $(DIST_LINUX)
	jpackage \
		--type rpm \
		--input target \
		--name $(APP_NAME) \
		--main-jar peerlink-$(VERSION)-shaded.jar \
		--main-class $(MAIN_CLASS) \
		--app-version $(VERSION) \
		--vendor "ByteForge" \
		--description "Secure P2P File Transfer — No Server Required" \
		--copyright "Copyright 2025 ByteForge" \
		--icon src/main/resources/icon.png \
		--linux-package-name peerlink \
		--linux-app-category net \
		--linux-menu-group "Network;FileTransfer;" \
		--linux-shortcut \
		--linux-rpm-license-type MIT \
		--java-options "--add-opens java.base/java.lang=ALL-UNNAMED" \
		--java-options "-Xmx512m" \
		--java-options "-Xms64m" \
		--dest $(DIST_LINUX) \
		--verbose

linux-appimage: jar
	mkdir -p $(DIST_LINUX)
	jpackage \
		--type app-image \
		--input target \
		--name $(APP_NAME) \
		--main-jar peerlink-$(VERSION)-shaded.jar \
		--main-class $(MAIN_CLASS) \
		--app-version $(VERSION) \
		--vendor "ByteForge" \
		--icon src/main/resources/icon.png \
		--java-options "--add-opens java.base/java.lang=ALL-UNNAMED" \
		--java-options "-Xmx512m" \
		--dest dist/appimage-staging \
		--verbose
	@echo "Creating AppDir structure..."
	@mkdir -p dist/appimage-staging/$(APP_NAME)
	@echo '#!/bin/bash' > dist/appimage-staging/$(APP_NAME)/AppRun
	@echo 'SELF=$$(readlink -f "$$0")' >> dist/appimage-staging/$(APP_NAME)/AppRun
	@echo 'HERE=$$(dirname "$$SELF")' >> dist/appimage-staging/$(APP_NAME)/AppRun
	@echo 'export PATH="$$HERE/bin:$$PATH"' >> dist/appimage-staging/$(APP_NAME)/AppRun
	@echo 'export LD_LIBRARY_PATH="$$HERE/lib:$$LD_LIBRARY_PATH"' >> dist/appimage-staging/$(APP_NAME)/AppRun
	@echo 'exec "$$HERE/bin/$(APP_NAME)" "$$@"' >> dist/appimage-staging/$(APP_NAME)/AppRun
	@chmod +x dist/appimage-staging/$(APP_NAME)/AppRun
	@echo '[Desktop Entry]' > dist/appimage-staging/$(APP_NAME)/$(APP_NAME).desktop
	@echo 'Type=Application' >> dist/appimage-staging/$(APP_NAME)/$(APP_NAME).desktop
	@echo 'Name=$(APP_NAME)' >> dist/appimage-staging/$(APP_NAME)/$(APP_NAME).desktop
	@echo 'Comment=Secure P2P File Transfer — No Server Required' >> dist/appimage-staging/$(APP_NAME)/$(APP_NAME).desktop
	@echo 'Exec=$(APP_NAME)' >> dist/appimage-staging/$(APP_NAME)/$(APP_NAME).desktop
	@echo 'Icon=peerlink' >> dist/appimage-staging/$(APP_NAME)/$(APP_NAME).desktop
	@echo 'Categories=Network;FileTransfer;' >> dist/appimage-staging/$(APP_NAME)/$(APP_NAME).desktop
	@echo 'Terminal=false' >> dist/appimage-staging/$(APP_NAME)/$(APP_NAME).desktop
	@echo 'StartupNotify=true' >> dist/appimage-staging/$(APP_NAME)/$(APP_NAME).desktop
	@cp src/main/resources/icon.png dist/appimage-staging/$(APP_NAME)/peerlink.png
	@echo "Packaging AppImage..."
	ARCH=x86_64 appimagetool \
		dist/appimage-staging/$(APP_NAME) \
		$(DIST_LINUX)/$(APP_NAME)-$(VERSION)-x86_64.AppImage
	chmod +x $(DIST_LINUX)/$(APP_NAME)-$(VERSION)-x86_64.AppImage
	@echo "Creating portable tar.gz..."
	cd dist/appimage-staging && tar -czf ../linux/peerlink-$(VERSION)-linux.tar.gz $(APP_NAME)/

linux-flatpak: jar
	mkdir -p $(DIST_LINUX)
	flatpak remote-add --if-not-exists flathub https://dl.flathub.org/repo/flathub.flatpakrepo || true
	flatpak-builder --force-clean --repo=dist/flatpak-repo dist/flatpak-build com.peerlink.PeerLink.yaml
	flatpak build-bundle dist/flatpak-repo $(DIST_LINUX)/$(APP_NAME).flatpak com.peerlink.PeerLink

linux-all: linux-deb linux-rpm linux-appimage linux-flatpak

mac-dmg: jar
	mkdir -p $(DIST_MAC)
	jpackage \
		--type dmg \
		--input target \
		--name $(APP_NAME) \
		--main-jar peerlink-$(VERSION)-shaded.jar \
		--main-class $(MAIN_CLASS) \
		--app-version $(VERSION) \
		--vendor "ByteForge" \
		--description "Secure P2P File Transfer" \
		--copyright "Copyright 2025 ByteForge" \
		--icon src/main/resources/icon.icns \
		--mac-package-name "$(APP_NAME)" \
		--mac-package-identifier "com.peerlink.PeerLink" \
		--java-options "--add-opens java.base/java.lang=ALL-UNNAMED" \
		--java-options "-Xmx512m" \
		--java-options "-Xms64m" \
		--dest $(DIST_MAC) \
		--verbose

mac-pkg: jar
	mkdir -p $(DIST_MAC)
	jpackage \
		--type pkg \
		--input target \
		--name $(APP_NAME) \
		--main-jar peerlink-$(VERSION)-shaded.jar \
		--main-class $(MAIN_CLASS) \
		--app-version $(VERSION) \
		--vendor "ByteForge" \
		--icon src/main/resources/icon.icns \
		--mac-package-identifier "com.peerlink.PeerLink" \
		--java-options "--add-opens java.base/java.lang=ALL-UNNAMED" \
		--java-options "-Xmx512m" \
		--dest $(DIST_MAC) \
		--verbose

mac-all: mac-dmg mac-pkg

windows-exe:
	@echo "Windows packaging must be run on Windows"
	@echo "Run: nmake windows-exe on a Windows machine"

windows-msi:
	@echo "Windows packaging must be run on Windows"
	@echo "Run: nmake windows-msi on a Windows machine"

windows-all: windows-exe windows-msi

checksums:
	find dist/ -type f \( -name "*.deb" -o -name "*.rpm" \
		-o -name "*.AppImage" -o -name "*.flatpak" \
		-o -name "*.tar.gz" -o -name "*.dmg" -o -name "*.pkg" \
		-o -name "*.exe" -o -name "*.msi" \) \
		-exec sha256sum {} \; > dist/SHA256SUMS.txt
	cat dist/SHA256SUMS.txt
