# Deployment Guide - VM with Public IP

## Prerequisites

### VM Requirements
- **OS**: Ubuntu 20.04+ or Debian 11+ (recommended)
- **RAM**: Minimum 2GB, recommended 4GB
- **Storage**: Minimum 10GB free space
- **CPU**: 2 cores minimum
- **Public IP**: Required for Telegram webhook

### Software Requirements
- Java 21 (JDK)
- Docker & Docker Compose
- Git

---

## Step 1: Prepare Your VM

### 1.1 Connect to VM

```bash
# SSH into your VM
ssh user@your-vm-ip

# Or if using key
ssh -i your-key.pem user@your-vm-ip
```

### 1.2 Update System

```bash
sudo apt update
sudo apt upgrade -y
```

### 1.3 Install Java 21

```bash
# Add Java repository
sudo apt install -y wget apt-transport-https

# Install Java 21
sudo apt install -y openjdk-21-jdk

# Verify installation
java -version
# Should show: openjdk version "21.x.x"
```

### 1.4 Install Docker

```bash
# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Add your user to docker group
sudo usermod -aG docker $USER

# Apply group changes (or logout/login)
newgrp docker

# Verify
docker --version
```

### 1.5 Install Docker Compose

```bash
# Install Docker Compose
sudo apt install -y docker-compose

# Verify
docker-compose --version
```

### 1.6 Install Git

```bash
sudo apt install -y git
git --version
```

---

## Step 2: Deploy Application

### 2.1 Clone/Upload Project

**Option A: Clone from Git (if you have repository)**
```bash
cd /opt
sudo mkdir telegram-zoho-desk
sudo chown $USER:$USER telegram-zoho-desk
cd telegram-zoho-desk

git clone YOUR_REPO_URL .
```

**Option B: Upload via SCP**
```bash
# From your local machine (Windows)
# Open PowerShell in project directory
scp -r C:\Users\Asus\IdeaProjects\zoho-desk-telegram-chat user@your-vm-ip:/opt/telegram-zoho-desk
```

**Option C: Create tar and upload**
```bash
# On Windows (in project directory)
tar -czf telegram-zoho-desk.tar.gz *

# Upload
scp telegram-zoho-desk.tar.gz user@your-vm-ip:/tmp/

# On VM
cd /opt
sudo mkdir telegram-zoho-desk
sudo chown $USER:$USER telegram-zoho-desk
cd telegram-zoho-desk
tar -xzf /tmp/telegram-zoho-desk.tar.gz
```

### 2.2 Configure Environment

```bash
cd /opt/telegram-zoho-desk

# Create .env from example
cp .env.example .env

# Edit configuration
nano .env
```

**Update these values in .env:**
```bash
# Server Configuration
SERVER_PORT=8080
APP_URL=http://YOUR_VM_PUBLIC_IP:8080  # or https://your-domain.com

# Telegram Bot Configuration
TELEGRAM_BOT_TOKEN=8539976996:AAGWgTCpWaku9rcUvQ81Dnrsi0JywEaa4_g
TELEGRAM_WEBHOOK_AUTO_REGISTER=true  # Enable auto-registration

# Database Configuration (leave as is)
DB_HOST=localhost
DB_PORT=5432
DB_NAME=telegramdesk
DB_USER=postgres
DB_PASS=postgres

# Redis Configuration (leave as is)
REDIS_HOST=localhost
REDIS_PORT=6379

# Conversation Cleanup
CONVERSATION_CLEANUP_ENABLED=true
CONVERSATION_RETENTION_DAYS=7
```

Save and exit (Ctrl+X, Y, Enter)

### 2.3 Configure Firewall

```bash
# Allow SSH (if not already)
sudo ufw allow 22/tcp

# Allow application port
sudo ufw allow 8080/tcp

# If using HTTP/HTTPS with reverse proxy
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# Enable firewall
sudo ufw enable

# Check status
sudo ufw status
```

### 2.4 Make Scripts Executable

```bash
chmod +x deploy.sh stop.sh
chmod +x chatbot/gradlew
```

### 2.5 Create Logs Directory

```bash
mkdir -p logs
```

---

## Step 3: Deploy Application

### 3.1 Run Deployment Script

```bash
# Start in foreground (to see logs)
./deploy.sh

# OR start in background
./deploy.sh background
```

### 3.2 Verify Deployment

```bash
# Check if containers are running
docker ps

# Check application logs
tail -f logs/app.log

# Test health endpoint
curl http://localhost:8080/admin/health

# Expected: {"status":"UP","service":"telegram-zoho-desk"}
```

---

## Step 4: Register Telegram Webhook

### 4.1 Get Your VM's Public IP

```bash
# Get public IP
curl ifconfig.me
# Or
curl ipinfo.io/ip
```

### 4.2 Register Webhook

**Option A: Automatic (if auto-register is enabled in .env)**
The webhook will register automatically on startup.

**Option B: Manual Registration**
```bash
# Using your app's admin endpoint
curl -X POST http://localhost:8080/admin/webhook/register

# Or directly via Telegram API
BOT_TOKEN="8539976996:AAGWgTCpWaku9rcUvQ81Dnrsi0JywEaa4_g"
PUBLIC_IP="YOUR_VM_PUBLIC_IP"
curl -X POST "https://api.telegram.org/bot${BOT_TOKEN}/setWebhook?url=http://${PUBLIC_IP}:8080/webhook/telegram"
```

### 4.3 Verify Webhook

```bash
# Check webhook status
curl http://localhost:8080/admin/webhook/info

# Or directly
BOT_TOKEN="8539976996:AAGWgTCpWaku9rcUvQ81Dnrsi0JywEaa4_g"
curl "https://api.telegram.org/bot${BOT_TOKEN}/getWebhookInfo"
```

Expected response:
```json
{
  "ok": true,
  "result": {
    "url": "http://YOUR_VM_IP:8080/webhook/telegram",
    "has_custom_certificate": false,
    "pending_update_count": 0
  }
}
```

---

## Step 5: Test Your Bot

1. Open Telegram
2. Find your bot
3. Send a message: "Hello!"
4. Bot should respond with confirmation

**Monitor logs:**
```bash
# Watch application logs
tail -f logs/app.log

# Watch specific log level
tail -f logs/app.log | grep INFO

# Check PostgreSQL
docker exec -it telegramdesk-postgres psql -U postgres -d telegramdesk -c "SELECT * FROM conversations;"
```

---

## Step 6: Setup as System Service (Optional but Recommended)

### 6.1 Create Systemd Service

```bash
sudo nano /etc/systemd/system/telegram-zoho-desk.service
```

Add this content:
```ini
[Unit]
Description=Telegram Zoho Desk Integration
After=docker.service
Requires=docker.service

[Service]
Type=simple
User=YOUR_USERNAME
WorkingDirectory=/opt/telegram-zoho-desk/chatbot
EnvironmentFile=/opt/telegram-zoho-desk/.env
ExecStart=/opt/telegram-zoho-desk/chatbot/gradlew bootRun
Restart=always
RestartSec=10
StandardOutput=append:/opt/telegram-zoho-desk/logs/app.log
StandardError=append:/opt/telegram-zoho-desk/logs/error.log

[Install]
WantedBy=multi-user.target
```

### 6.2 Enable and Start Service

```bash
# Reload systemd
sudo systemctl daemon-reload

# Enable service (start on boot)
sudo systemctl enable telegram-zoho-desk

# Start service
sudo systemctl start telegram-zoho-desk

# Check status
sudo systemctl status telegram-zoho-desk

# View logs
sudo journalctl -u telegram-zoho-desk -f
```

### 6.3 Service Management Commands

```bash
# Start service
sudo systemctl start telegram-zoho-desk

# Stop service
sudo systemctl stop telegram-zoho-desk

# Restart service
sudo systemctl restart telegram-zoho-desk

# View logs
sudo journalctl -u telegram-zoho-desk -f

# Check status
sudo systemctl status telegram-zoho-desk
```

---

## Step 7: Setup HTTPS with Nginx (Recommended for Production)

### 7.1 Install Nginx

```bash
sudo apt install -y nginx certbot python3-certbot-nginx
```

### 7.2 Configure Nginx

```bash
sudo nano /etc/nginx/sites-available/telegram-zoho-desk
```

Add:
```nginx
server {
    listen 80;
    server_name your-domain.com;  # Replace with your domain

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Enable site:
```bash
sudo ln -s /etc/nginx/sites-available/telegram-zoho-desk /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

### 7.3 Get SSL Certificate (if using domain)

```bash
sudo certbot --nginx -d your-domain.com

# Auto-renewal is configured by default
sudo certbot renew --dry-run
```

### 7.4 Update Webhook URL

Update `.env`:
```bash
APP_URL=https://your-domain.com
```

Restart and re-register webhook:
```bash
sudo systemctl restart telegram-zoho-desk
curl -X POST http://localhost:8080/admin/webhook/register
```

---

## Monitoring & Maintenance

### View Logs
```bash
# Application logs
tail -f /opt/telegram-zoho-desk/logs/app.log

# Docker logs
docker-compose logs -f

# System service logs
sudo journalctl -u telegram-zoho-desk -f
```

### Database Backup
```bash
# Backup PostgreSQL
docker exec telegramdesk-postgres pg_dump -U postgres telegramdesk > backup_$(date +%Y%m%d).sql

# Restore
docker exec -i telegramdesk-postgres psql -U postgres telegramdesk < backup_20251218.sql
```

### Monitor Resources
```bash
# Check disk space
df -h

# Check memory
free -h

# Check Docker containers
docker stats

# Check application port
netstat -tulpn | grep 8080
```

---

## Troubleshooting

### Application won't start
```bash
# Check Java version
java -version

# Check if port is in use
sudo lsof -i :8080

# Check Docker containers
docker ps -a
docker-compose logs
```

### Webhook not working
```bash
# Check firewall
sudo ufw status

# Test from outside
curl http://YOUR_PUBLIC_IP:8080/webhook/telegram

# Check webhook registration
curl http://localhost:8080/admin/webhook/info
```

### Database connection issues
```bash
# Check PostgreSQL is running
docker ps | grep postgres

# Check logs
docker logs telegramdesk-postgres

# Test connection
docker exec -it telegramdesk-postgres psql -U postgres -d telegramdesk
```

---

## Quick Reference Commands

```bash
# Start application
./deploy.sh background

# Stop application
./stop.sh

# View logs
tail -f logs/app.log

# Restart Docker containers
docker-compose restart

# Check status
curl http://localhost:8080/admin/health

# Register webhook
curl -X POST http://localhost:8080/admin/webhook/register

# Database query
docker exec -it telegramdesk-postgres psql -U postgres -d telegramdesk -c "SELECT COUNT(*) FROM conversations;"

# Redis check
docker exec -it telegramdesk-redis redis-cli KEYS "*"
```

---

## Security Recommendations

1. **Change default PostgreSQL password** in `docker-compose.yml` and `.env`
2. **Use HTTPS** (setup Nginx with SSL)
3. **Setup firewall** properly (only open necessary ports)
4. **Regular backups** of database
5. **Monitor logs** for suspicious activity
6. **Keep system updated**: `sudo apt update && sudo apt upgrade`
7. **Use strong bot token** (never share it)

---

## Next Steps

1. ✅ Deploy to VM
2. ✅ Register webhook
3. ✅ Test bot functionality
4. ⏭️ Implement Zoho Desk integration (Sprint 4)
5. ⏭️ Setup monitoring and alerts
6. ⏭️ Configure automatic backups
