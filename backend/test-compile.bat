@echo off
echo Testing Maven compilation...
cd /d "%~dp0"
call mvn clean compile 2>&1 | findstr /i "error BUILD"
if %ERRORLEVEL% EQU 0 (
    echo Compilation FAILED - Errors found
    call mvn clean compile
) else (
    echo Compilation SUCCESSFUL
)
pause
