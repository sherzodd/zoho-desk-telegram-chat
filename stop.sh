#!/bin/bash

# Stop script

echo "Stopping Telegram-Zoho Desk application..."

# Stop Spring Boot app
if [ -f app.pid ]; then
    PID=$(cat app.pid)
    if ps -p $PID > /dev/null; then
        echo "Stopping application (PID: $PID)..."
        kill $PID
        rm app.pid
        echo "✅ Application stopped"
    else
        echo "Application not running"
        rm app.pid
    fi
else
    echo "No PID file found, searching for running process..."
    pkill -f "telegram-zoho-desk" || echo "No running process found"
fi

# Optionally stop Docker containers
read -p "Stop Docker containers? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    docker-compose down
    echo "✅ Docker containers stopped"
fi
