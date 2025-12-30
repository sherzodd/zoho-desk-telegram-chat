#!/bin/bash

# Docker deployment script

set -e  # Exit on error

echo "=========================================="
echo "Docker Deployment - Telegram-Zoho Desk"
echo "=========================================="
echo ""

# Check if .env exists
if [ ! -f .env ]; then
    echo "ERROR: .env file not found!"
    echo "Please create .env from .env.example and configure it"
    exit 1
fi

echo "✅ Environment file found"
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

# Build and start all services
echo "Building and starting all services..."
echo "This may take a few minutes on first run..."
echo ""

docker-compose up --build -d

echo ""
echo "✅ All services started!"
echo ""

# Wait for services to be healthy
echo "Waiting for services to be healthy..."
sleep 15

# Check service health
echo ""
echo "Checking service health..."
docker-compose ps

echo ""
echo "=========================================="
echo "Deployment Complete!"
echo "=========================================="
echo ""
echo "Application URL: http://localhost:8080"
echo ""
echo "Useful commands:"
echo "  - View logs:        docker-compose logs -f app"
echo "  - Stop services:    docker-compose down"
echo "  - Restart app:      docker-compose restart app"
echo "  - View status:      docker-compose ps"
echo "  - Check health:     curl http://localhost:8080/admin/health"
echo ""
