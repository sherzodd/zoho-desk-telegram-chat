# 🐳 Docker Deployment - Quick Start

## One Command Deployment!

```bash
# Clone project
git clone https://github.com/sherzodd/zoho-desk-telegram-chat.git
cd zoho-desk-telegram-chat

# Configure
cp .env.example .env
nano .env  # Update APP_URL and TELEGRAM_BOT_TOKEN

# Deploy everything!
chmod +x docker-deploy.sh
./docker-deploy.sh
```

**That's it!** Your entire stack is running in Docker! 🎉

---

## What You Get

- ✅ **PostgreSQL 16** - Database (persistent storage)
- ✅ **Redis 7** - Caching layer
- ✅ **Spring Boot App** - Your application
- ✅ **Automatic Health Checks** - All services monitored
- ✅ **Automatic Restarts** - Services recover from failures
- ✅ **Network Isolation** - Secure service communication

---

## Quick Commands

```bash
# Deploy
./docker-deploy.sh

# Stop
./docker-stop.sh

# View logs
docker-compose logs -f app

# Restart app
docker-compose restart app

# Check status
docker-compose ps

# Rebuild after code changes
docker-compose up --build -d app
```

---

## Configuration

Update `.env` file:

```bash
# For VM with public IP
APP_URL=http://YOUR_VM_PUBLIC_IP:8080
TELEGRAM_BOT_TOKEN=your_bot_token
TELEGRAM_WEBHOOK_AUTO_REGISTER=true

# Database & Redis use default settings (automatic)
# DB_HOST=postgres  (container name, not localhost!)
# REDIS_HOST=redis  (container name, not localhost!)
```

**Important:**
- When using Docker, `DB_HOST` is automatically set to `postgres`
- Don't use `localhost` - use container names!

---

## Advantages Over Manual Deployment

| Feature | Docker | Manual |
|---------|--------|--------|
| Setup Time | 5 min | 30 min |
| Java Install | Not needed | Required |
| Gradle Install | Not needed | Required |
| Dependencies | Automatic | Manual |
| Isolation | Complete | Shared |
| Portability | Any OS | Specific |
| Updates | 1 command | Multiple steps |
| Cleanup | 1 command | Manual |

---

## Full Documentation

See [DOCKER-DEPLOYMENT.md](DOCKER-DEPLOYMENT.md) for:
- Complete setup guide
- Advanced configuration
- Troubleshooting
- Production deployment with Nginx/SSL
- CI/CD examples
- Monitoring and maintenance

---

## Requirements

- Docker 24.0+
- Docker Compose 2.0+
- 2GB RAM minimum
- 10GB disk space

---

## Support

Issues? Check:
1. `docker-compose logs app`
2. `docker-compose ps`
3. [DOCKER-DEPLOYMENT.md](DOCKER-DEPLOYMENT.md)
