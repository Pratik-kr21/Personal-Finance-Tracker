@echo off
setlocal
echo ===================================================
echo   Launching Personal Finance Tracker...
echo ===================================================

set JAVA_CMD=java
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    if exist "C:\Program Files\Java\jdk-25\bin\java.exe" (
        set "JAVA_CMD=C:\Program Files\Java\jdk-25\bin\java.exe"
    )
)

"%JAVA_CMD%" -cp "bin;lib/*" com.finance.tracker.Main
