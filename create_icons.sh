#!/bin/bash
set -e

# 1. Accepts a master 1024x1024 PNG as input
mkdir -p src/main/resources
if [ ! -f src/main/resources/icon.png ]; then
  convert -size 1024x1024 xc:#026699 \
    -fill white \
    -font Helvetica-Bold \
    -pointsize 200 \
    -gravity Center \
    -annotate 0 "PL" \
    src/main/resources/icon.png
fi

# 2. Generate Linux icons
SIZES=(16 24 32 48 64 128 256 512)
for size in "${SIZES[@]}"; do
  mkdir -p "src/main/resources/icons/hicolor/${size}x${size}/apps"
  convert src/main/resources/icon.png -resize "${size}x${size}" "src/main/resources/icons/hicolor/${size}x${size}/apps/peerlink.png"
done

# 3. Generate Windows icon
convert src/main/resources/icon.png \
  \( -clone 0 -resize 16x16   \) \
  \( -clone 0 -resize 24x24   \) \
  \( -clone 0 -resize 32x32   \) \
  \( -clone 0 -resize 48x48   \) \
  \( -clone 0 -resize 64x64   \) \
  \( -clone 0 -resize 128x128 \) \
  \( -clone 0 -resize 256x256 \) \
  -delete 0 \
  src/main/resources/icon.ico

# 4. Generate macOS icon (ICNS)
if command -v sips &>/dev/null && command -v iconutil &>/dev/null; then
  mkdir -p icon.iconset
  sips -z 16 16     src/main/resources/icon.png --out icon.iconset/icon_16x16.png
  sips -z 32 32     src/main/resources/icon.png --out icon.iconset/icon_16x16@2x.png
  sips -z 32 32     src/main/resources/icon.png --out icon.iconset/icon_32x32.png
  sips -z 64 64     src/main/resources/icon.png --out icon.iconset/icon_32x32@2x.png
  sips -z 128 128   src/main/resources/icon.png --out icon.iconset/icon_128x128.png
  sips -z 256 256   src/main/resources/icon.png --out icon.iconset/icon_128x128@2x.png
  sips -z 256 256   src/main/resources/icon.png --out icon.iconset/icon_256x256.png
  sips -z 512 512   src/main/resources/icon.png --out icon.iconset/icon_256x256@2x.png
  sips -z 512 512   src/main/resources/icon.png --out icon.iconset/icon_512x512.png
  sips -z 1024 1024 src/main/resources/icon.png --out icon.iconset/icon_512x512@2x.png
  iconutil -c icns icon.iconset -o src/main/resources/icon.icns
  rm -rf icon.iconset
else
  echo "Skipping ICNS generation: sips or iconutil not available (only on macOS)."
fi
