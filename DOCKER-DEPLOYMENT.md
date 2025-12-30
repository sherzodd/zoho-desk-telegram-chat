# Docker Deployment Guide

## 🐳 Complete Docker Deployment

This guide shows how to deploy the entire application stack using Docker.

---

## 📋 What's Included

The Docker setup includes:
- **PostgreSQL 16** - Database
- **Redis 7** - Caching layer
- **Spring Boot App** - Main application

All services are connected via a Docker network and start automatically in the correct order.

---

## 🚀 Quick Start (1 Command!)

### Option 1: Using Script (Easiest)

```bash
# Make script executable
chmod +x docker-deploy.sh

# Deploy everything!
./docker-deploy.sh
```

### Option 2: Using Docker Compose Directly

```bash
# Build and start all services
docker-compose up --build -d

# View logs
docker-compose logs -f app
```

That's it! The entire stack is running! 🎉

---

## 📝 Prerequisites

### 1. Install Docker

**Ubuntu/Debian:**
```bash
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
newgrp docker
```

**Windows:**
Download Docker Desktop: https://www.docker.com/products/docker-desktop

**Mac:**
Download Docker Desktop: https://www.docker.com/products/docker-desktop

### 2. Install Docker Compose

**Linux:**
```bash
sudo apt install -y docker-compose
```

**Windows/Mac:**
Included with Docker Desktop

### 3. Verify Installation

```bash
docker --version
docker-compose --version
```

---

## ⚙️ Configuration

### 1. Create `.env` File

```bash
cp .env.example .env
nano .env
```

### 2. Update Critical Settings

```bash
# For VM deployment
APP_URL=http://YOUR_VM_PUBLIC_IP:8080
TELEGRAM_BOT_TOKEN=your_bot_token_here
TELEGRAM_WEBHOOK_AUTO_REGISTER=true

# Database (can use defaults)
DB_NAME=telegramdesk
DB_USER=postgres
DB_PASS=postgres

# Server
SERVER_PORT=8080
```

**Important Notes:**
- `DB_HOST` is automatically set to `postgres` (container name)
- `REDIS_HOST` is automatically set to `redis` (container name)
- Don't use `localhost` - use container names for internal communication

---

## 🎯 Deployment Steps

### Step 1: Clone/Upload Project

```bash
# On VM
git clone https://github.com/sherzodd/zoho-desk-telegram-chat.git
cd zoho-desk-telegram-chat
```

### Step 2: Configure Environment

```bash
cp .env.example .env
nano .env
# Update APP_URL and TELEGRAM_BOT_TOKEN
```

### Step 3: Deploy

```bash
# Make scripts executable
chmod +x docker-deploy.sh docker-stop.sh

# Deploy!
./docker-deploy.sh
```

### Step 4: Verify

```bash
# Check all services are running
docker-compose ps

# Should show:
# NAME                     STATUS
# telegramdesk-app         Up (healthy)
# telegramdesk-postgres    Up (healthy)
# telegramdesk-redis       Up (healthy)

# Test health endpoint
curl http://localhost:8080/admin/health
```

---

## 📊 Service Details

### Application Container

- **Name:** `telegramdesk-app`
- **Port:** 8080
- **Base Image:** Eclipse Temurin 21 JRE Alpine
- **Health Check:** Every 30s
- **Restart Policy:** unless-stopped

### PostgreSQL Container

- **Name:** `telegramdesk-postgres`
- **Port:** 5432
- **Image:** postgres:16-alpine
- **Volume:** `postgres_data` (persistent)

### Redis Container

- **Name:** `telegramdesk-redis`
- **Port:** 6379
- **Image:** redis:7-alpine
- **Volume:** `redis_data` (persistent)

---

## 🔧 Management Commands

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f app
docker-compose logs -f postgres
docker-compose logs -f redis

# Last 100 lines
docker-compose logs --tail=100 app
```

### Start/Stop Services

```bash
# Stop all
docker-compose down

# Stop all and remove volumes (WARNING: deletes data!)
docker-compose down -v

# Start all
docker-compose up -d

# Restart specific service
docker-compose restart app
```

### Check Status

```bash
# View running containers
docker-compose ps

# View resource usage
docker stats

# Check health
docker-compose ps app
```

### Rebuild Application

```bash
# After code changes
docker-compose up --build -d app

# Or rebuild without cache
docker-compose build --no-cache app
docker-compose up -d app
```

### Access Container Shell

```bash
# Application container
docker exec -it telegramdesk-app sh

# PostgreSQL
docker exec -it telegramdesk-postgres psql -U postgres -d telegramdesk

# Redis
docker exec -it telegramdesk-redis redis-cli
```

---

## 🗄️ Database Operations

### Connect to PostgreSQL

```bash
docker exec -it telegramdesk-postgres psql -U postgres -d telegramdesk
```

### Run SQL Commands

```bash
# View tables
docker exec -it telegramdesk-postgres psql -U postgres -d telegramdesk -c "\dt"

# Query conversations
docker exec -it telegramdesk-postgres psql -U postgres -d telegramdesk -c "SELECT * FROM conversations;"

# Count messages
docker exec -it telegramdesk-postgres psql -U postgres -d telegramdesk -c "SELECT COUNT(*) FROM messages;"
```

### Backup Database

```bash
# Create backup
docker exec telegramdesk-postgres pg_dump -U postgres telegramdesk > backup_$(date +%Y%m%d).sql

# Restore backup
docker exec -i telegramdesk-postgres psql -U postgres telegramdesk < backup_20251218.sql
```

---

## 🔴 Redis Operations

### Connect to Redis

```bash
docker exec -it telegramdesk-redis redis-cli
```

### Check Cache

```bash
# List all keys
docker exec telegramdesk-redis redis-cli KEYS "*"

# Get specific conversation
docker exec telegramdesk-redis redis-cli GET "conversation:123456"

# Check memory usage
docker exec telegramdesk-redis redis-cli INFO memory

# Flush all cache (WARNING: clears all data)
docker exec telegramdesk-redis redis-cli FLUSHALL
```

---

## 🌐 Firewall Configuration

```bash
# Allow Docker network
sudo ufw allow 8080/tcp

# If using different port
sudo ufw allow YOUR_PORT/tcp

# Enable firewall
sudo ufw enable

# Check status
sudo ufw status
```

---

## 🔄 Update Application

### Method 1: Rebuild from Latest Code

```bash
# Pull latest changes
git pull origin main

# Rebuild and restart
docker-compose up --build -d app

# View new logs
docker-compose logs -f app
```

### Method 2: Update Environment Variables

```bash
# Edit .env
nano .env

# Restart app to pick up changes
docker-compose restart app
```

---

## 📈 Monitoring

### Health Checks

All services have built-in health checks:

```bash
# Check health status
docker-compose ps

# Application health endpoint
curl http://localhost:8080/admin/health

# PostgreSQL health
docker exec telegramdesk-postgres pg_isready -U postgres

# Redis health
docker exec telegramdesk-redis redis-cli ping
```

### Resource Monitoring

```bash
# Real-time stats
docker stats

# Disk usage
docker system df

# Detailed container info
docker inspect telegramdesk-app
```

---

## 🐛 Troubleshooting

### Application Won't Start

```bash
# Check logs
docker-compose logs app

# Check if database is ready
docker-compose ps postgres

# Restart services in order
docker-compose restart postgres redis app
```

### Build Fails

```bash
# Clean build
docker-compose build --no-cache app

# Check Dockerfile syntax
docker-compose config

# Remove old images
docker system prune -a
```

### Port Already in Use

```bash
# Find what's using port 8080
sudo lsof -i :8080

# Kill process (if needed)
sudo kill -9 PID

# Or change port in .env
SERVER_PORT=8081
```

### Database Connection Issues

```bash
# Check PostgreSQL is running
docker-compose ps postgres

# Check logs
docker-compose logs postgres

# Test connection
docker exec telegramdesk-postgres psql -U postgres -d telegramdesk -c "SELECT 1;"
```

### Out of Disk Space

```bash
# Check Docker disk usage
docker system df

# Clean up
docker system prune -a --volumes

# WARNING: This removes ALL unused containers, networks, images, and volumes
```

---

## 🔐 Security Best Practices

### 1. Change Default Passwords

In `.env`:
```bash
DB_PASS=your_strong_password_here
```

In `docker-compose.yml`, the password is automatically picked up from `.env`.

### 2. Use Docker Secrets (Production)

```yaml
# In docker-compose.yml
secrets:
  db_password:
    file: ./secrets/db_password.txt

services:
  app:
    secrets:
      - db_password
```

### 3. Limit Resource Usage

```yaml
services:
  app:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
        reservations:
          cpus: '1'
          memory: 1G
```

### 4. Network Isolation

```yaml
# Don't expose database ports to host in production
services:
  postgres:
    ports: []  # Remove port mapping
```

---

## 📦 Production Deployment

### With HTTPS (Nginx Reverse Proxy)

1. **Install Nginx:**
```bash
sudo apt install -y nginx certbot python3-certbot-nginx
```

2. **Configure Nginx:**
```nginx
# /etc/nginx/sites-available/telegram-zoho-desk
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

3. **Enable and Get SSL:**
```bash
sudo ln -s /etc/nginx/sites-available/telegram-zoho-desk /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
sudo certbot --nginx -d your-domain.com
```

4. **Update .env:**
```bash
APP_URL=https://your-domain.com
```

5. **Restart app:**
```bash
docker-compose restart app
```

---

## 🔄 Automated Deployment (CI/CD)

### GitHub Actions Example

```yaml
# .github/workflows/deploy.yml
name: Deploy to VM

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - name: Deploy to VM
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.VM_HOST }}
          username: ${{ secrets.VM_USER }}
          key: ${{ secrets.SSH_KEY }}
          script: |
            cd /opt/telegram-zoho-desk
            git pull origin main
            docker-compose up --build -d app
```

---

## 📊 Comparison: Docker vs Manual

| Feature | Docker | Manual |
|---------|--------|--------|
| Setup Time | 5 minutes | 20-30 minutes |
| Dependencies | Automatic | Manual install |
| Isolation | Complete | Shared system |
| Updates | `docker-compose up --build` | Manual restart |
| Rollback | Easy (`docker-compose down && up`) | Complex |
| Portability | Any machine | Specific to setup |
| Resource Control | Built-in limits | Manual config |

**Recommendation:** Use Docker for all deployments! ✅

---

## 🎯 Quick Reference

```bash
# Deploy
./docker-deploy.sh

# Stop
./docker-stop.sh

# View logs
docker-compose logs -f app

# Restart
docker-compose restart app

# Rebuild
docker-compose up --build -d app

# Check status
docker-compose ps

# Access database
docker exec -it telegramdesk-postgres psql -U postgres -d telegramdesk

# Check cache
docker exec -it telegramdesk-redis redis-cli KEYS "*"

# Health check
curl http://localhost:8080/admin/health
```

---

## ✅ Advantages of Docker Deployment

1. ✅ **One Command Setup** - Everything with `docker-compose up`
2. ✅ **Consistent Environment** - Works same everywhere
3. ✅ **Automatic Dependencies** - No manual Java/Gradle install
4. ✅ **Health Checks** - Built-in monitoring
5. ✅ **Easy Rollback** - Just rebuild previous version
6. ✅ **Resource Limits** - Control CPU/memory usage
7. ✅ **Clean Removal** - `docker-compose down -v` removes everything
8. ✅ **Multi-Stage Build** - Smaller final image
9. ✅ **Network Isolation** - Services communicate securely
10. ✅ **Production Ready** - Same setup for dev and prod

---

## 📞 Support

For issues:
1. Check `docker-compose logs app`
2. Verify `.env` configuration
3. Check service health: `docker-compose ps`
4. See main [DEPLOYMENT.md](DEPLOYMENT.md) for detailed troubleshooting

---

**Last Updated:** 2025-12-18
**Docker Version:** 24.0+
**Docker Compose Version:** 2.0+
