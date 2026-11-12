@echo off
setlocal
echo ===================================================
echo   Packaging Personal Finance Tracker Standalone JAR
echo ===================================================

set JAR_CMD=jar
where jar >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    if exist "C:\Program Files\Java\jdk-25\bin\jar.exe" (
        set "JAR_CMD=C:\Program Files\Java\jdk-25\bin\jar.exe"
    )
)

if not exist bin (
    echo Bin folder not found. Compiling first...
    call compile.bat
)

if not exist dist mkdir dist

echo Extracting driver dependencies for standalone fat JAR...
cd bin
for %%f in (..\lib\*.jar) do (
    "%JAR_CMD%" -xf "%%f"
)
cd ..

echo Creating PersonalFinanceTracker.jar...
"%JAR_CMD%" -cfe dist\PersonalFinanceTracker.jar com.finance.tracker.Main -C bin .

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ===================================================
    echo   SUCCESS! Standalone JAR created at:
    echo   dist\PersonalFinanceTracker.jar
    echo.
    echo   You can run it directly:
    echo   java -jar dist\PersonalFinanceTracker.jar
    echo ===================================================
) else (
    echo.
    echo Packaging failed!
)
