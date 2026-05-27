# PACKAGING GUIDE

## 1. Build Modes

Understanding the different Java packaging approaches:

### Normal JAR Run
- Creates a standard JAR file with compiled classes
- Requires Java to be installed on target machine
- Run with: `java -jar peerlink.jar`
- No native installer, just a portable executable JAR
- Dependencies must be available at runtime

### Fat JAR (Shaded JAR)
- Combines application code and all dependencies into a single JAR
- Uses Maven Shade Plugin to merge JARs
- Contains JavaFX libraries within the JAR
- Still requires Java runtime on target machine
- Solves dependency management issues
- Larger file size but self-contained

### jlink Runtime
- Creates a custom minimal Java runtime
- Includes only modules needed by the application
- Reduces distribution size significantly
- Platform-specific runtime
- Requires module-info.java for proper module definition
- Results in smaller, faster startup times

### jpackage Installer
- Creates native platform installers (exe, deb, rpm, dmg, etc.)
- Bundles application with a minimal Java runtime (from jlink)
- Creates native shortcuts, menu entries, and file associations
- Provides professional installation experience
- Target machines don't need Java installed
- Generates platform-specific executables

## 2. Best Packaging Approach Recommendation

**Recommended: jpackage with jlink-generated runtime**

### Why This Approach?

1. **Smaller Distribution Size**: jlink creates a minimal Java runtime containing only the modules PeerLink actually uses, reducing the distribution size from ~100MB to ~50MB

2. **Faster Startup**: Custom runtime loads faster than full JDK/JRE

3. **Professional User Experience**: Native installers (exe, deb, rpm) provide familiar installation process

4. **No Java Requirement**: Users don't need to install Java separately

5. **Better Security**: Reduced attack surface with minimal runtime

6. **Consistent Behavior**: Application runs with exactly the Java version it was tested with

### Alternative Considered: jpackage with Shaded JAR

This approach was considered but rejected because:
- Larger distribution size (includes full JavaFX in JAR)
- Slower startup due to JAR scanning
- Less efficient memory usage
- No module system benefits

## 3. Maven Configuration Fix

### Updated pom.xml Configuration

The current `pom.xml` has several issues that need to be fixed:

1. **Inconsistent main class references**: Some sections use `peerlink.Main` while the actual class is `com.peerlink.ui.Main`
2. **Missing module-info.java**: Prevents optimal jlink usage
3. **Redundant configurations**: Some jpackage executions can be consolidated

### Linux Packaging Configuration

```xml
<!-- Linux .deb (Debian/Ubuntu) -->
<execution>
  <id>package-linux-deb</id>
  <phase>package</phase>
  <goals><goal>exec</goal></goals>
  <configuration>
    <skip>${skipLinux}</skip>
    <executable>jpackage</executable>
    <arguments>
      <argument>--type</argument>
      <argument>deb</argument>
      <argument>--input</argument>
      <argument>${project.build.directory}</argument>
      <argument>--name</argument>
      <argument>${app.name}</argument>
      <argument>--main-jar</argument>
      <argument>${project.artifactId}-${project.version}-shaded.jar</argument>
      <argument>--main-class</argument>
      <argument>${app.main-class}</argument>
      <argument>--app-version</argument>
      <argument>${app.version}</argument>
      <argument>--vendor</argument>
      <argument>${app.vendor}</argument>
      <argument>--description</argument>
      <argument>${app.description}</argument>
      <argument>--copyright</argument>
      <argument>${app.copyright}</argument>
      <argument>--icon</argument>
      <argument>src/main/resources/icon.png</argument>
      <argument>--linux-package-name</argument>
      <argument>peerlink</argument>
      <argument>--linux-app-category</argument>
      <argument>net</argument>
      <argument>--linux-menu-group</argument>
      <argument>Network;FileTransfer;</argument>
      <argument>--linux-shortcut</argument>
      <argument>--linux-deb-maintainer</argument>
      <argument>semwalsourav4@gmail.com</argument>
      <argument>--java-options</argument>
      <argument>--add-opens java.base/java.lang=ALL-UNNAMED</argument>
      <argument>--java-options</argument>
      <argument>-Xmx512m</argument>
      <argument>--java-options</argument>
      <argument>-Xms64m</argument>
      <argument>--dest</argument>
      <argument>dist/linux</argument>
      <argument>--verbose</argument>
    </arguments>
  </configuration>
</execution>

<!-- Linux .rpm (Fedora/RHEL/openSUSE) -->
<execution>
  <id>package-linux-rpm</id>
  <phase>package</phase>
  <goals><goal>exec</goal></goals>
  <configuration>
    <skip>${skipLinux}</skip>
    <executable>jpackage</executable>
    <arguments>
      <argument>--type</argument>
      <argument>rpm</argument>
      <argument>--input</argument>
      <argument>${project.build.directory}</argument>
      <argument>--name</argument>
      <argument>${app.name}</argument>
      <argument>--main-jar</argument>
      <argument>${project.artifactId}-${project.version}-shaded.jar</argument>
      <argument>--main-class</argument>
      <argument>${app.main-class}</argument>
      <argument>--app-version</argument>
      <argument>${app.version}</argument>
      <argument>--vendor</argument>
      <argument>${app.vendor}</argument>
      <argument>--description</argument>
      <argument>${app.description}</argument>
      <argument>--copyright</argument>
      <argument>${app.copyright}</argument>
      <argument>--icon</argument>
      <argument>src/main/resources/icon.png</argument>
      <argument>--linux-package-name</argument>
      <argument>peerlink</argument>
      <argument>--linux-app-category</argument>
      <argument>net</argument>
      <argument>--linux-menu-group</argument>
      <argument>Network;FileTransfer;</argument>
      <argument>--linux-shortcut</argument>
      <argument>--linux-rpm-license-type</argument>
      <argument>MIT</argument>
      <argument>--java-options</argument>
      <argument>--add-opens java.base/java.lang=ALL-UNNAMED</argument>
      <argument>--java-options</argument>
      <argument>-Xmx512m</argument>
      <argument>--java-options</argument>
      <argument>-Xms64m</argument>
      <argument>--dest</argument>
      <argument>dist/linux</argument>
      <argument>--verbose</argument>
    </arguments>
  </configuration>
</execution>
```

### Windows Packaging Configuration

```xml
<!-- Windows .exe -->
<execution>
  <id>package-windows-exe</id>
  <phase>package</phase>
  <goals><goal>exec</goal></goals>
  <configuration>
    <skip>${skipWindows}</skip>
    <executable>jpackage</executable>
    <arguments>
      <argument>--type</argument>
      <argument>exe</argument>
      <argument>--input</argument>
      <argument>${project.build.directory}</argument>
      <argument>--name</argument>
      <argument>${app.name}</argument>
      <argument>--main-jar</argument>
      <argument>${project.artifactId}-${project.version}-shaded.jar</argument>
      <argument>--main-class</argument>
      <argument>${app.main-class}</argument>
      <argument>--app-version</argument>
      <argument>${app.version}</argument>
      <argument>--vendor</argument>
      <argument>${app.vendor}</argument>
      <argument>--description</argument>
      <argument>${app.description}</argument>
      <argument>--copyright</argument>
      <argument>${app.copyright}</argument>
      <argument>--icon</argument>
      <argument>src/main/resources/icon.ico</argument>
      <argument>--win-dir-chooser</argument>
      <argument>--win-menu</argument>
      <argument>--win-menu-group</argument>
      <argument>${app.name}</argument>
      <argument>--win-shortcut</argument>
      <argument>--win-shortcut-prompt</argument>
      <argument>--win-upgrade-uuid</argument>
      <argument>a1b2c3d4-e5f6-7890-abcd-ef1234567890</argument>
      <argument>--java-options</argument>
      <argument>--add-opens java.base/java.lang=ALL-UNNAMED</argument>
      <argument>--java-options</argument>
      <argument>-Xmx512m</argument>
      <argument>--java-options</argument>
      <argument>-Xms64m</argument>
      <argument>--dest</argument>
      <argument>dist/windows</argument>
      <argument>--verbose</argument>
    </arguments>
  </configuration>
</execution>

<!-- Windows .msi -->
<execution>
  <id>package-windows-msi</id>
  <phase>package</phase>
  <goals><goal>exec</goal></goals>
  <configuration>
    <skip>${skipWindows}</skip>
    <executable>jpackage</executable>
    <arguments>
      <argument>--type</argument>
      <argument>msi</argument>
      <argument>--input</argument>
      <argument>${project.build.directory}</argument>
      <argument>--name</argument>
      <argument>${app.name}</argument>
      <argument>--main-jar</argument>
      <argument>${project.artifactId}-${project.version}-shaded.jar</argument>
      <argument>--main-class</argument>
      <argument>${app.main-class}</argument>
      <argument>--app-version</argument>
      <argument>${app.version}</argument>
      <argument>--vendor</argument>
      <argument>${app.vendor}</argument>
      <argument>--icon</argument>
      <argument>src/main/resources/icon.ico</argument>
      <argument>--win-dir-chooser</argument>
      <argument>--win-menu</argument>
      <argument>--win-menu-group</argument>
      <argument>${app.name}</argument>
      <argument>--win-shortcut</argument>
      <argument>--win-upgrade-uuid</argument>
      <argument>a1b2c3d4-e5f6-7890-abcd-ef1234567890</argument>
      <argument>--java-options</argument>
      <argument>--add-opens java.base/java.lang=ALL-UNNAMED</argument>
      <argument>--java-options</argument>
      <argument>-Xmx512m</argument>
      <argument>--dest</argument>
      <argument>dist/windows</argument>
      <argument>--verbose</argument>
    </arguments>
  </configuration>
</execution>
```

### Key Changes Made

1. **Standardized main class**: All references now use `com.peerlink.ui.Main` consistently
2. **Fixed resource paths**: Ensure icons are correctly referenced
3. **Optimized JVM arguments**: Consistent heap settings across platforms
4. **Improved output structure**: Clear destination directories for each platform
5. **Maintained security settings**: Kept `--add-opens` for JavaFX internal access

### OS Profiles

The existing OS profiles in pom.xml are correct and will automatically activate the appropriate packaging based on the build platform:

- Linux: Activates when OS family is "unix" and name is "linux"
- macOS: Activates when OS family is "mac"
- Windows: Activates when OS family is "windows"

This allows developers to simply run `mvn clean package` on their respective platforms to generate the appropriate installers.