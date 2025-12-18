# Quick Start - Deploy to VM

## 🚀 Fast Track Deployment (15 minutes)

### Prerequisites
- VM with Ubuntu 20.04+
- Public IP address
- SSH access

---

## Step 1: Upload Project to VM

**From your Windows machine:**

```powershell
# Navigate to project directory
cd C:\Users\Asus\IdeaProjects\zoho-desk-telegram-chat

# Create archive (if you have tar)
tar -czf telegram-zoho-desk.tar.gz *

# Upload to VM
scp telegram-zoho-desk.tar.gz user@YOUR_VM_IP:/tmp/
```

---

## Step 2: Setup VM (One-Time Setup)

**SSH into your VM:**

```bash
ssh user@YOUR_VM_IP
```

**Run setup commands:**

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Java 21
sudo apt install -y openjdk-21-jdk

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
newgrp docker

# Install Docker Compose
sudo apt install -y docker-compose

# Verify installations
java -version
docker --version
docker-compose --version
```

---

## Step 3: Extract and Configure Project

```bash
# Create project directory
sudo mkdir -p /opt/telegram-zoho-desk
sudo chown $USER:$USER /opt/telegram-zoho-desk
cd /opt/telegram-zoho-desk

# Extract project
tar -xzf /tmp/telegram-zoho-desk.tar.gz

# Make scripts executable
chmod +x deploy.sh stop.sh chatbot/gradlew

# Create logs directory
mkdir -p logs

# Configure environment
cp .env.example .env
nano .env
```

**Update in .env:**
```bash
# Get your VM public IP first
curl ifconfig.me

# Then edit .env:
APP_URL=http://YOUR_VM_PUBLIC_IP:8080  # Use the IP from above
TELEGRAM_BOT_TOKEN=8539976996:AAGWgTCpWaku9rcUvQ81Dnrsi0JywEaa4_g
TELEGRAM_WEBHOOK_AUTO_REGISTER=true   # Enable auto-registration!
```

Save: `Ctrl+X`, then `Y`, then `Enter`

---

## Step 4: Configure Firewall

```bash
# Allow SSH
sudo ufw allow 22/tcp

# Allow application
sudo ufw allow 8080/tcp

# Enable firewall
sudo ufw --force enable

# Check status
sudo ufw status
```

---

## Step 5: Deploy!

```bash
# Deploy in background
./deploy.sh background

# Wait 30 seconds for startup
sleep 30

# Check if running
curl http://localhost:8080/admin/health
```

Expected output: `{"status":"UP","service":"telegram-zoho-desk"}`

---

## Step 6: Verify Webhook

```bash
# Check webhook status
curl http://localhost:8080/admin/webhook/info

# Should show your VM IP in the webhook URL
```

---

## Step 7: Test Bot

1. Open Telegram
2. Search for your bot (or go to https://t.me/YOUR_BOT_USERNAME)
3. Send message: "Hello!"
4. Bot should reply instantly

**Watch logs in real-time:**
```bash
tail -f logs/app.log
```

---

## 🎉 You're Done!

Your bot is now running on the VM and receiving webhooks!

## Useful Commands

```bash
# View logs
tail -f /opt/telegram-zoho-desk/logs/app.log

# Stop application
cd /opt/telegram-zoho-desk
./stop.sh

# Start application
./deploy.sh background

# Check status
curl http://localhost:8080/admin/health

# View conversations
docker exec -it telegramdesk-postgres psql -U postgres -d telegramdesk -c "SELECT * FROM conversations;"

# Check Redis cache
docker exec -it telegramdesk-redis redis-cli KEYS "*"

# Restart Docker containers
cd /opt/telegram-zoho-desk
docker-compose restart
```

---

## If Something Goes Wrong

### Application not starting
```bash
# Check logs
tail -100 logs/app.log

# Check Docker containers
docker ps -a
docker-compose logs
```

### Webhook not working
```bash
# Test from outside (from your Windows machine)
curl http://YOUR_VM_PUBLIC_IP:8080/webhook/telegram

# If timeout, check firewall
sudo ufw status
sudo ufw allow 8080/tcp
```

### Port already in use
```bash
# Find what's using port 8080
sudo lsof -i :8080

# Kill it (replace PID)
sudo kill -9 PID
```

---

## Setup as System Service (Optional)

To make the app start automatically on reboot:

```bash
# Create service file
sudo nano /etc/systemd/system/telegram-zoho-desk.service
```

Paste this (replace YOUR_USERNAME):
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

Enable and start:
```bash
sudo systemctl daemon-reload
sudo systemctl enable telegram-zoho-desk
sudo systemctl start telegram-zoho-desk
sudo systemctl status telegram-zoho-desk
```

---

## Support

For detailed documentation, see: [DEPLOYMENT.md](DEPLOYMENT.md)
