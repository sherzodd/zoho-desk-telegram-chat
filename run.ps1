# PowerShell script to run the application with .env variables

Write-Host "Loading environment variables from .env..." -ForegroundColor Green

# Read .env file and set environment variables
if (Test-Path ".env") {
    Get-Content .env | ForEach-Object {
        if ($_ -match '^([^=#]+)=(.*)$') {
            $name = $matches[1].Trim()
            $value = $matches[2].Trim()
            [System.Environment]::SetEnvironmentVariable($name, $value, "Process")
            Write-Host "Set $name" -ForegroundColor Yellow
        }
    }
} else {
    Write-Host "ERROR: .env file not found!" -ForegroundColor Red
    Write-Host "Copy .env.example to .env first:" -ForegroundColor Red
    Write-Host "  cp .env.example .env" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "Checking Docker containers..." -ForegroundColor Green
docker-compose ps

Write-Host ""
Write-Host "Starting application..." -ForegroundColor Green
Write-Host ""

Set-Location chatbot
.\gradlew.bat bootRun

Set-Location ..
