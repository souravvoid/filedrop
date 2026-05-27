@echo off
echo PeerLink Windows Build Script
echo ==============================

echo Checking prerequisites...

:: Check Java
where java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Error: Java is not installed
    echo Install OpenJDK 21 from: https://adoptium.net/
    exit /b 1
)

for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%g
)

:: Remove quotes from version string
set JAVA_VERSION=%JAVA_VERSION:"=%

:: Extract major version
for /f "delims=._" %%v in ("%JAVA_VERSION%") do set MAJOR_VERSION=%%v

if %MAJOR_VERSION% lss 21 (
    echo Error: Java 21+ required, found Java %JAVA_VERSION%
    echo Install OpenJDK 21 from: https://adoptium.net/
    exit /b 1
)

echo ✓ Java %JAVA_VERSION% detected

:: Check Maven
where mvn >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Error: Maven is not installed
    echo Install Maven from: https://maven.apache.org/
    exit /b 1
)
echo ✓ Maven detected

:: Check jpackage
where jpackage >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Error: jpackage not found
    echo Ensure JDK 21+ is installed and in PATH
    exit /b 1
)
echo ✓ jpackage detected

:: Check WiX Toolset for MSI
where candle >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Warning: WiX Toolset not found - MSI packaging will fail
    echo Install from: https://wixtoolset.org/
)

:: Create dist directory
if not exist dist\windows mkdir dist\windows

echo Building PeerLink...

:: Build the project
mvn clean package -DskipTests --batch-mode

echo Build completed successfully!
echo Installers created in dist\windows\

echo.
echo Available packages:
for %%f in (dist\windows\*.exe dist\windows\*.msi) do (
    if exist "%%f" (
        for /f "usebackq" %%s in (`dir "%%f" ^| findstr "%%~nxf"`) do (
            echo   %%~zf bytes  %%~nxf
        )
    )
)

echo.
echo Installation instructions:
echo   EXE: Double-click dist\windows\PeerLink-*.exe
echo   MSI: msiexec /i dist\windows\PeerLink-*.msi