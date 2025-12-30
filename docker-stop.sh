#!/bin/bash

# Docker stop script

echo "Stopping all services..."
docker-compose down

echo ""
echo "✅ All services stopped"
echo ""
echo "To remove volumes (WARNING: deletes all data):"
echo "  docker-compose down -v"
echo ""
