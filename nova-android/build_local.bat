@echo off
echo ==============================================
echo       Building Nova Voice Assistant APK
echo ==============================================

set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

echo Using Java: %JAVA_HOME%
java -version

echo.
echo Running Gradle build...
call gradlew.bat assembleDebug --stacktrace

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ==============================================
    echo [SUCCESS] Nova APK built successfully!
    echo Location: app\build\outputs\apk\debug\app-debug.apk
    echo ==============================================
) else (
    echo.
    echo ==============================================
    echo [ERROR] Build failed. 
    echo If network SSL/proxy blocked Google Maven, you can also push to GitHub
    echo and GitHub Actions will automatically compile the APK in the cloud!
    echo ==============================================
)
pause
