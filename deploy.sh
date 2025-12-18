#!/bin/bash

# Deployment script for Linux VM

set -e  # Exit on error

echo "=========================================="
echo "Telegram-Zoho Desk Deployment Script"
echo "=========================================="
echo ""

# Check if .env exists
if [ ! -f .env ]; then
    echo "ERROR: .env file not found!"
    echo "Please create .env from .env.example and configure it"
    exit 1
fi

# Load environment variables
set -a
source .env
set +a

echo "✅ Environment variables loaded"
echo ""

# Check Docker is installed
if ! command -v docker &> /dev/null; then
    echo "ERROR: Docker is not installed"
    echo "Install Docker: https://docs.docker.com/engine/install/"
    exit 1
fi

echo "✅ Docker is installed"

# Check Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo "ERROR: Docker Compose is not installed"
    echo "Install Docker Compose: https://docs.docker.com/compose/install/"
    exit 1
fi

echo "✅ Docker Compose is installed"
echo ""

# Start infrastructure
echo "Starting PostgreSQL and Redis..."
docker-compose up -d

# Wait for services to be healthy
echo "Waiting for services to be ready..."
sleep 10

# Check if services are running
if ! docker ps | grep -q telegramdesk-postgres; then
    echo "ERROR: PostgreSQL container is not running"
    exit 1
fi

if ! docker ps | grep -q telegramdesk-redis; then
    echo "ERROR: Redis container is not running"
    exit 1
fi

echo "✅ Infrastructure is running"
echo ""

# Build the application
echo "Building application..."
cd chatbot
./gradlew clean build -x test

if [ $? -ne 0 ]; then
    echo "ERROR: Build failed"
    exit 1
fi

echo "✅ Application built successfully"
echo ""

# Run the application
echo "Starting application..."
echo "Application will run on port ${SERVER_PORT:-8080}"
echo ""

# Run in background or foreground based on parameter
if [ "$1" = "background" ]; then
    nohup ./gradlew bootRun > ../logs/app.log 2>&1 &
    echo $! > ../app.pid
    echo "✅ Application started in background (PID: $(cat ../app.pid))"
    echo "View logs: tail -f logs/app.log"
else
    ./gradlew bootRun
fi
