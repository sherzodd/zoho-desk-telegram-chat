@echo off
REM Load environment variables from .env file

echo Loading environment variables from .env...

FOR /F "tokens=*" %%i in ('type .env') do (
    SET "line=%%i"
    REM Skip empty lines and comments
    echo !line! | findstr /R "^[^#]" >nul
    if !errorlevel! == 0 (
        FOR /F "tokens=1,2 delims==" %%a in ("%%i") do (
            SET "%%a=%%b"
            echo Set %%a=%%b
        )
    )
)

echo.
echo Starting application...
echo.

cd chatbot
gradlew.bat bootRun

cd ..
