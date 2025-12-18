# Testing Guide - Telegram Webhook Integration

## Quick Start Testing

### Prerequisites
- Telegram account
- Bot token from @BotFather
- ngrok installed (for local testing)

### Step-by-Step Testing

#### 1. Create Telegram Bot

```bash
# Open Telegram, search for @BotFather
# Send these commands:

/newbot
# Enter bot name: "My Test Support Bot"
# Enter bot username: "my_test_support_bot" (must end with 'bot')

# Save the token that looks like:
# 1234567890:ABCdefGHIjklMNOpqrsTUVwxyz
```

#### 2. Setup Environment

```bash
# Create .env file (or copy from .env.example)
cat > .env << EOF
TELEGRAM_BOT_TOKEN=YOUR_BOT_TOKEN_HERE
APP_URL=http://localhost:8080
TELEGRAM_WEBHOOK_AUTO_REGISTER=false
DB_HOST=localhost
DB_PORT=5432
DB_NAME=telegramdesk
DB_USER=postgres
DB_PASS=postgres
REDIS_HOST=localhost
REDIS_PORT=6379
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
EOF
```

#### 3. Start Infrastructure

```bash
# Start PostgreSQL and Redis
docker-compose up -d

# Verify containers are running
docker-compose ps
```

#### 4. Start Application

```bash
cd chatbot
./gradlew bootRun

# Wait for log message:
# "Started TelegramDeskApplication in X.XXX seconds"
```

#### 5. Expose Application (Local Testing)

```bash
# In a new terminal
ngrok http 8080

# You'll see output like:
# Forwarding   https://abc123.ngrok.io -> http://localhost:8080

# Copy the HTTPS URL (https://abc123.ngrok.io)
```

#### 6. Register Webhook

**Option A: Using Admin API**
```bash
# Update APP_URL in .env with ngrok URL
APP_URL=https://abc123.ngrok.io

# Restart application

# Register webhook
curl -X POST http://localhost:8080/admin/webhook/register
```

**Option B: Direct Telegram API**
```bash
curl -X POST "https://api.telegram.org/bot<YOUR_BOT_TOKEN>/setWebhook?url=https://abc123.ngrok.io/webhook/telegram"
```

#### 7. Verify Webhook

```bash
# Check webhook info
curl http://localhost:8080/admin/webhook/info

# Expected response:
{
  "ok": true,
  "result": {
    "url": "https://abc123.ngrok.io/webhook/telegram",
    "has_custom_certificate": false,
    "pending_update_count": 0
  }
}
```

#### 8. Test Bot

1. Open Telegram
2. Search for your bot username
3. Click START or send any message
4. Bot should respond with echo message

**Example conversation:**
```
You: Hello bot!
Bot: Received your message: Hello bot!

     Your message will be forwarded to our support team.
```

## Testing Checklist

### ✅ Basic Functionality
- [ ] Application starts without errors
- [ ] Webhook registers successfully
- [ ] Bot responds to messages
- [ ] Logs show incoming updates
- [ ] Logs show outgoing messages

### ✅ Error Handling
- [ ] Bot handles non-text messages gracefully
- [ ] Invalid webhook URL shows error
- [ ] Missing bot token shows clear error
- [ ] Network errors are logged properly

### ✅ Admin Endpoints
- [ ] `GET /admin/health` returns status
- [ ] `GET /admin/webhook/info` shows webhook details
- [ ] `POST /admin/webhook/register` registers webhook
- [ ] `POST /admin/webhook/delete` removes webhook
- [ ] `POST /admin/test/message` sends test message

## Manual API Testing

### Test Webhook Endpoint

```bash
# Test with sample payload
curl -X POST http://localhost:8080/webhook/telegram \
  -H "Content-Type: application/json" \
  -d '{
    "update_id": 123456,
    "message": {
      "message_id": 1,
      "from": {
        "id": 987654321,
        "username": "testuser",
        "first_name": "Test",
        "last_name": "User"
      },
      "chat": {
        "id": 987654321,
        "type": "private",
        "username": "testuser",
        "first_name": "Test"
      },
      "text": "Test message",
      "date": 1638360000
    }
  }'

# Expected: 200 OK
```

### Get Webhook Info

```bash
curl http://localhost:8080/admin/webhook/info | jq
```

### Send Test Message

```bash
# Get your chat ID first
# Send a message to your bot, then check logs or webhook info
# Or use this bot to get your chat ID: @userinfobot

# Send test message
curl -X POST "http://localhost:8080/admin/test/message?chatId=YOUR_CHAT_ID&message=Hello%20World"
```

### Health Check

```bash
curl http://localhost:8080/admin/health
```

## Logs to Monitor

### Successful Webhook Registration
```
INFO  c.c.t.c.TelegramWebhookRegistrar : ✅ Telegram webhook registered successfully: https://abc123.ngrok.io/webhook/telegram
```

### Incoming Message
```
INFO  c.c.t.c.TelegramWebhookController : Received Telegram update ID: 123456
INFO  c.c.t.s.ConversationService       : Processing message from user testuser (chat 987654321): Hello bot!
```

### Outgoing Message
```
INFO  c.c.t.s.TelegramService           : Message sent to chat 987654321: 200 OK
```

## Common Issues

### 1. Webhook not receiving messages

**Check 1: Verify webhook is registered**
```bash
curl "https://api.telegram.org/bot<TOKEN>/getWebhookInfo"
```

**Check 2: Ensure ngrok is running**
```bash
# ngrok should show HTTP requests
# If not, restart ngrok and re-register webhook
```

**Check 3: Check application logs**
```bash
# Look for errors in application console
# Enable debug logging if needed
```

### 2. Bot not responding

**Check 1: Verify bot token**
```bash
curl "https://api.telegram.org/bot<TOKEN>/getMe"
# Should return bot info
```

**Check 2: Check application is running**
```bash
curl http://localhost:8080/admin/health
```

**Check 3: Check logs for errors**

### 3. ngrok URL changed

```bash
# ngrok URLs expire after 2 hours (free tier)
# Get new URL: restart ngrok
ngrok http 8080

# Re-register webhook with new URL
curl -X POST http://localhost:8080/admin/webhook/register
```

### 4. SSL/HTTPS errors

- Telegram requires HTTPS for webhooks
- ngrok provides HTTPS automatically
- For production, use proper SSL certificate

### 5. Connection refused

```bash
# Check PostgreSQL is running
docker-compose ps postgres

# Check Redis is running
docker-compose ps redis

# Restart if needed
docker-compose restart
```

## Production Testing

### Before Deployment
- [ ] Set proper domain in APP_URL
- [ ] Enable HTTPS with valid certificate
- [ ] Set TELEGRAM_WEBHOOK_AUTO_REGISTER=false
- [ ] Test with real traffic
- [ ] Monitor logs for errors
- [ ] Set up log aggregation
- [ ] Configure alerts for failures

### Load Testing
```bash
# Send multiple requests
for i in {1..10}; do
  curl -X POST http://localhost:8080/webhook/telegram \
    -H "Content-Type: application/json" \
    -d "$(cat test-payload.json)" &
done
wait

# Check logs for any errors
```

## Debugging Tips

### Enable Debug Logging

Add to `application-dev.yml`:
```yaml
logging:
  level:
    com.company.telegramdesk: DEBUG
    org.springframework.web: DEBUG
```

### View ngrok Requests

1. Open http://localhost:4040 (ngrok web interface)
2. See all HTTP requests/responses
3. Replay requests for testing

### Test with Postman

1. Import webhook endpoint
2. Use sample JSON payload
3. Test different message types
4. Validate responses

## Next Steps

After Sprint 2 testing is complete:
1. Implement database persistence (Sprint 3)
2. Add Redis caching (Sprint 3)
3. Test with conversation history
4. Implement Zoho Desk integration (Sprint 4+)

## Support

For issues:
1. Check logs first
2. Verify configuration
3. Test with cURL
4. Review Telegram Bot API docs
5. Check ngrok status

## Resources

- [Telegram Bot API](https://core.telegram.org/bots/api)
- [ngrok Documentation](https://ngrok.com/docs)
- [Spring Boot Testing](https://spring.io/guides/gs/testing-web/)
