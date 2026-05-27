# CHANGELOG - Packaging Changes

## Summary of Changes Made

### Fixed Issues

1. **XML Comment Syntax in pom.xml**
   - File: `pom.xml`
   - Issue: Double dashes (`--`) in XML comments near `--main-jar` argument references caused parsing errors
   - Fix: Changed comment text from "so jpackage --main-jar references" to "so jpackage uses it as input"
   - Lines affected: ~127

2. **Icon Generation**
   - File: `create_icons.sh` 
   - Issue: Script uses deprecated ImageMagick `convert` command
   - Fix: Script now works with ImageMagick 7 (shows warnings but works)
   - Created files: `src/main/resources/icon.png`, `src/main/resources/icon.ico`

3. **Shade Plugin Filter Configuration**
   - File: `pom.xml`
   - Issue: Version information was being excluded from JavaFX JARs
   - Fix: Changed filter to include `META-INF/versions/` 
   - Lines: ~136-152

### Verified Working

1. **Maven Build**: `mvn clean compile` ✅
2. **JAR Package**: `mvn clean package -Pjar-only` ✅
3. **JavaFX Run**: `mvn javafx:run` ✅ (runs correctly)
4. **Shaded JAR**: Contains 4858 classes including all JavaFX

### Known Limitations

1. **Native Packaging on Linux**
   - Requires: `dpkg-deb` (for .deb), `rpmbuild` (for .rpm)
   - jpackage needs these tools installed on the system
   - jlink issue with modified java.security in Java 25

2. **Standalone JAR Execution**
   - `java -jar peerlink-1.0.0-shaded.jar` fails on Java 25 due to JavaFX runtime detection issue
   - Workaround: Use `mvn javafx:run` for development
   - Alternative: Use jpackage to create native installer

### Build Commands

```bash
# Development (runs with Maven classpath)
mvn javafx:run

# Build JAR only
mvn clean package -Pjar-only

# Build with native packaging (on Linux with tools installed)
mvn clean package

# Build with explicit skip
mvn clean package -DskipLinux=true -DskipMac=true -DskipWindows=true
```

### Files Modified

1. `pom.xml` - Fixed XML comments and shade filters
2. `create_icons.sh` - Ran to generate icons (already existed)
3. `src/main/resources/icon.png` - Created
4. `src/main/resources/icon.ico` - Created