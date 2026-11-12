@echo off
setlocal
echo ===================================================
echo   Compiling Personal Finance Tracker (Core Java)
echo ===================================================

set JAVAC_CMD=javac
where javac >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    if exist "C:\Program Files\Java\jdk-25\bin\javac.exe" (
        set "JAVAC_CMD=C:\Program Files\Java\jdk-25\bin\javac.exe"
    )
)

if not exist bin mkdir bin

echo Using: %JAVAC_CMD%
"%JAVAC_CMD%" -encoding UTF-8 -cp "lib/*" -d bin src\com\finance\tracker\model\*.java src\com\finance\tracker\db\*.java src\com\finance\tracker\dao\*.java src\com\finance\tracker\ui\*.java src\com\finance\tracker\Main.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ===================================================
    echo   BUILD SUCCESSFUL!
    echo   To launch the application, run: run.bat
    echo ===================================================
) else (
    echo.
    echo ===================================================
    echo   BUILD FAILED! Please check the errors above.
    echo ===================================================
)
pause
