# Sprint 2: Telegram Webhook Integration ✅

## Overview
Implemented complete Telegram webhook integration with message receiving, parsing, and sending capabilities.

## Deliverables

### ✅ 1. Telegram DTO Models
Created comprehensive data transfer objects for Telegram API:

**Location:** `chatbot/src/main/java/com/company/telegramdesk/model/dto/telegram/`

- **TelegramUpdate.java** - Main update container
- **TelegramMessage.java** - Message object with text, media support
- **TelegramChat.java** - Chat information (private, group, etc.)
- **TelegramUser.java** - User information
- **TelegramPhotoSize.java** - Photo metadata
- **TelegramDocument.java** - Document metadata

All DTOs use `@Data` Lombok annotation and proper Jackson annotations for JSON mapping (`@JsonProperty`).

### ✅ 2. Webhook Controller
**File:** `TelegramWebhookController.java`

**Features:**
- POST `/webhook/telegram` - Receives Telegram updates
- GET `/webhook/telegram` - Health check endpoint
- Comprehensive error handling
- Logs all incoming updates
- Returns 200 OK to prevent Telegram retries

**Example:**
```java
@PostMapping("/webhook/telegram")
public ResponseEntity<String> handleWebhook(@RequestBody TelegramUpdate update)
```

### ✅ 3. Telegram Service
**File:** `TelegramService.java`

**Features:**
- `sendMessage(chatId, text)` - Send simple text messages
- `sendMessage(chatId, text, replyToMessageId)` - Send replies
- `getWebhookInfo()` - Get current webhook status
- HTML parse mode support
- Proper error handling and logging

**Example:**
```java
telegramService.sendMessage("123456789", "Hello from bot!");
```

### ✅ 4. Webhook Registrar
**File:** `TelegramWebhookRegistrar.java`

**Features:**
- Auto-registers webhook on application startup (optional)
- `@EventListener(ApplicationReadyEvent.class)` for proper timing
- Manual registration via AdminController
- Delete webhook capability
- Get webhook info capability

**Configuration:**
```yaml
telegram:
  webhook:
    auto-register: false  # Set to true for auto-registration
```

### ✅ 5. Conversation Service
**File:** `ConversationService.java`

**Current Features:**
- Processes incoming messages
- Extracts chat ID, username, text
- Sends echo response to user
- Comprehensive logging

**Stubbed for Sprint 3:**
- Database persistence
- Redis caching

### ✅ 6. Admin Controller
**File:** `AdminController.java`

Provides management endpoints:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/admin/webhook/register` | POST | Manually register webhook |
| `/admin/webhook/delete` | POST | Delete webhook |
| `/admin/webhook/info` | GET | Get webhook info |
| `/admin/test/message` | POST | Send test message |
| `/admin/health` | GET | Health check |

**Examples:**
```bash
# Register webhook
curl -X POST http://localhost:8080/admin/webhook/register

# Get webhook info
curl http://localhost:8080/admin/webhook/info

# Send test message
curl -X POST "http://localhost:8080/admin/test/message?chatId=123456&message=Hello"
```

## Project Structure

```
chatbot/src/main/java/com/company/telegramdesk/
├── TelegramDeskApplication.java
├── config/
│   └── TelegramWebhookRegistrar.java
├── controller/
│   ├── TelegramWebhookController.java
│   └── AdminController.java
├── service/
│   ├── TelegramService.java
│   └── ConversationService.java
└── model/dto/telegram/
    ├── TelegramUpdate.java
    ├── TelegramMessage.java
    ├── TelegramChat.java
    ├── TelegramUser.java
    ├── TelegramPhotoSize.java
    └── TelegramDocument.java
```

## Configuration

### Environment Variables

Add to your `.env` file:

```bash
TELEGRAM_BOT_TOKEN=your_bot_token_from_botfather
APP_URL=https://your-domain.com
TELEGRAM_WEBHOOK_AUTO_REGISTER=false
```

### application.yml

```yaml
telegram:
  bot-token: ${TELEGRAM_BOT_TOKEN}
  webhook-url: ${APP_URL}/webhook/telegram
  webhook:
    auto-register: ${TELEGRAM_WEBHOOK_AUTO_REGISTER:false}
```

## Setup Instructions

### 1. Create Telegram Bot

```bash
# Open Telegram and search for @BotFather
# Send /newbot command
# Follow instructions to create bot
# Copy the bot token
```

### 2. Configure Environment

```bash
# Edit .env file
TELEGRAM_BOT_TOKEN=1234567890:ABCdefGHIjklMNOpqrsTUVwxyz
APP_URL=https://your-ngrok-url.ngrok.io  # or your domain
```

### 3. Expose Application (Development)

For local development, use ngrok:

```bash
# Install ngrok: https://ngrok.com/download
ngrok http 8080

# Copy the HTTPS URL (e.g., https://abc123.ngrok.io)
# Update APP_URL in .env with this URL
```

### 4. Start Application

```bash
cd chatbot
./gradlew bootRun
```

### 5. Register Webhook

**Option A: Automatic (on startup)**
```bash
# Set in .env
TELEGRAM_WEBHOOK_AUTO_REGISTER=true
# Restart application
```

**Option B: Manual**
```bash
curl -X POST http://localhost:8080/admin/webhook/register
```

**Option C: Direct Telegram API**
```bash
curl -X POST "https://api.telegram.org/bot<YOUR_BOT_TOKEN>/setWebhook?url=https://your-domain.com/webhook/telegram"
```

### 6. Verify Webhook

```bash
# Check webhook status
curl http://localhost:8080/admin/webhook/info

# Check application health
curl http://localhost:8080/admin/health

# Test webhook endpoint
curl http://localhost:8080/webhook/telegram
```

### 7. Test Bot

1. Open Telegram
2. Search for your bot username
3. Send `/start` or any message
4. Bot should echo your message back

## Testing

### Test Message Flow

1. **User sends message to bot**
   ```
   User: Hello bot!
   ```

2. **Telegram sends webhook to your server**
   ```json
   POST /webhook/telegram
   {
     "update_id": 123456,
     "message": {
       "message_id": 1,
       "from": {
         "id": 987654321,
         "username": "testuser",
         "first_name": "Test"
       },
       "chat": {
         "id": 987654321,
         "type": "private"
       },
       "text": "Hello bot!",
       "date": 1638360000
     }
   }
   ```

3. **Bot processes and responds**
   ```
   Bot: Received your message: Hello bot!

   Your message will be forwarded to our support team.
   ```

### Manual Testing with cURL

```bash
# Test webhook endpoint with sample payload
curl -X POST http://localhost:8080/webhook/telegram \
  -H "Content-Type: application/json" \
  -d '{
    "update_id": 123456,
    "message": {
      "message_id": 1,
      "from": {
        "id": 987654321,
        "username": "testuser",
        "first_name": "Test"
      },
      "chat": {
        "id": 987654321,
        "type": "private"
      },
      "text": "Test message",
      "date": 1638360000
    }
  }'
```

### Send Test Message

```bash
# Send test message to a chat
curl -X POST "http://localhost:8080/admin/test/message?chatId=YOUR_CHAT_ID&message=Hello%20World"
```

## Logs

Application logs show:

```
✅ Telegram webhook registered successfully: https://your-domain.com/webhook/telegram
📨 Received Telegram update ID: 123456
👤 Processing message from user testuser (chat 987654321): Hello bot!
✅ Message sent to chat 987654321: 200 OK
```

## Troubleshooting

### Webhook not receiving messages

1. **Check webhook is registered:**
   ```bash
   curl http://localhost:8080/admin/webhook/info
   ```

2. **Verify URL is accessible:**
   ```bash
   curl https://your-domain.com/webhook/telegram
   # Should return: "Telegram webhook is active"
   ```

3. **Check Telegram API webhook status:**
   ```bash
   curl "https://api.telegram.org/bot<TOKEN>/getWebhookInfo"
   ```

4. **Look for pending errors:**
   - Check `last_error_message` in webhook info
   - Ensure your server returns 200 OK
   - Verify HTTPS is working (Telegram requires HTTPS)

### Bot not responding

1. **Check bot token is correct:**
   ```bash
   curl "https://api.telegram.org/bot<TOKEN>/getMe"
   ```

2. **Check application logs** for errors

3. **Verify message format** in logs

### ngrok issues

```bash
# ngrok session expired - restart it
ngrok http 8080

# Update webhook with new URL
curl -X POST http://localhost:8080/admin/webhook/register
```

## API Endpoints Summary

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/webhook/telegram` | POST | Receive Telegram updates |
| `/webhook/telegram` | GET | Health check |
| `/admin/webhook/register` | POST | Register webhook |
| `/admin/webhook/delete` | POST | Delete webhook |
| `/admin/webhook/info` | GET | Get webhook info |
| `/admin/test/message` | POST | Send test message |
| `/admin/health` | GET | Health check |

## Next Steps (Sprint 3)

- [ ] Create database entities (Conversation, Message)
- [ ] Implement JPA repositories
- [ ] Add Redis caching
- [ ] Persist conversations to PostgreSQL
- [ ] Implement conversation history retrieval
- [ ] Add conversation status management

## Notes

- Webhook registration is disabled by default (manual registration required)
- Bot requires HTTPS for production webhooks
- Use ngrok for local development testing
- All messages are logged for debugging
- Error responses return 200 OK to prevent Telegram retries
- HTML parse mode is enabled for rich text formatting

## Resources

- [Telegram Bot API Documentation](https://core.telegram.org/bots/api)
- [BotFather Commands](https://core.telegram.org/bots#6-botfather)
- [Webhook Guide](https://core.telegram.org/bots/api#setwebhook)
- [ngrok Documentation](https://ngrok.com/docs)
