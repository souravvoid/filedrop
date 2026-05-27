@echo off
setlocal
set SCRIPT_DIR=%~dp0
set JAR=%SCRIPT_DIR%peerlink-1.0.0-shaded.jar
java --add-opens java.base/java.lang=ALL-UNNAMED ^
     -Xmx512m -Xms64m ^
     -jar "%JAR%" %*
endlocal
