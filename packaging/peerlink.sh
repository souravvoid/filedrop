#!/usr/bin/env bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR="$SCRIPT_DIR/peerlink-1.0.0-shaded.jar"

# Auto-detect Java 21+
if command -v java &>/dev/null; then
  JAVA_VER=$(java -version 2>&1 | awk -F'"' '/version/ {print $2}' | cut -d. -f1)
  if [ "$JAVA_VER" -ge 21 ] 2>/dev/null; then
    exec java \
      --add-opens java.base/java.lang=ALL-UNNAMED \
      -Xmx512m -Xms64m \
      -jar "$JAR" "$@"
  fi
fi

# Try JAVA_HOME
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  exec "$JAVA_HOME/bin/java" \
    --add-opens java.base/java.lang=ALL-UNNAMED \
    -Xmx512m -Xms64m \
    -jar "$JAR" "$@"
fi

echo "ERROR: Java 21 or later is required."
echo "Install from: https://adoptium.net/"
exit 1
