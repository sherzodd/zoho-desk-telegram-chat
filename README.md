# Telegram-Zoho Desk Integration

Spring Boot microservice integrating Telegram messaging with Zoho Desk support platform.

## Tech Stack

- **Java 21** (LTS)
- **Spring Boot 3.4.1**
- **Gradle 8.x**
- **PostgreSQL 16**
- **Redis 7.x**
- **Docker & Docker Compose**

## Project Structure

```
telegram-zoho-desk/
├── chatbot/                    # Main Spring Boot application
│   ├── src/main/java/com/company/telegramdesk/
│   │   └── TelegramDeskApplication.java
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── application-dev.yml
│   │   └── application-prod.yml
│   └── build.gradle
├── docker-compose.yml
├── .env.example
└── README.md
```

## Setup Instructions

### Prerequisites

1. Install Java 21 (JDK)
2. Install Docker Desktop
3. Install Gradle 8.x (or use Gradle wrapper)

### Step 1: Clone and Configure

```bash
# Clone the repository
cd zoho-desk-telegram-chat

# Copy environment template
cp .env.example .env

# Edit .env with your credentials
```

### Step 2: Start Infrastructure

```bash
# Start PostgreSQL and Redis
docker-compose up -d

# Verify containers are running
docker-compose ps

# Check logs
docker-compose logs -f
```

### Step 3: Build Application

```bash
cd chatbot

# Build without tests
./gradlew build -x test

# Or with tests
./gradlew build
```

### Step 4: Run Application

```bash
# Run with dev profile
./gradlew bootRun --args='--spring.profiles.active=dev'

# Or run the JAR
java -jar build/libs/chatbot-0.0.1-SNAPSHOT.jar
```

## Environment Variables

Create a `.env` file based on `.env.example`:

```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=telegramdesk
DB_USER=postgres
DB_PASS=postgres

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379

# Server Configuration
SERVER_PORT=8080
APP_URL=http://localhost:8080

# Telegram Bot Configuration
TELEGRAM_BOT_TOKEN=your_bot_token_here

# Zoho Desk Configuration
ZOHO_ORG_ID=your_org_id
ZOHO_CLIENT_ID=your_client_id
ZOHO_CLIENT_SECRET=your_client_secret
ZOHO_REFRESH_TOKEN=your_refresh_token
ZOHO_API_DOMAIN=https://desk.zoho.com

# Active Profile
SPRING_PROFILES_ACTIVE=dev
```

## Docker Commands

```bash
# Start services
docker-compose up -d

# Stop services
docker-compose down

# View logs
docker-compose logs -f [postgres|redis]

# Restart services
docker-compose restart

# Remove volumes (WARNING: deletes data)
docker-compose down -v
```

## Database Access

### PostgreSQL

```bash
# Connect via Docker
docker exec -it telegramdesk-postgres psql -U postgres -d telegramdesk

# Or via local psql
psql -h localhost -U postgres -d telegramdesk
```

### Redis

```bash
# Connect via Docker
docker exec -it telegramdesk-redis redis-cli

# Test connection
redis-cli -h localhost -p 6379 ping
```

## Gradle Commands

```bash
# Build project
./gradlew build

# Clean build
./gradlew clean build

# Run application
./gradlew bootRun

# Run tests
./gradlew test

# Check dependencies
./gradlew dependencies
```

## Sprint 1 Deliverables ✅

- [x] Gradle project with all dependencies
- [x] Docker infrastructure (PostgreSQL + Redis)
- [x] Spring Boot configuration files
- [x] Project builds successfully
- [x] Environment template created

## Sprint 2 Deliverables ✅

- [x] Telegram DTO models (Update, Message, Chat, User)
- [x] Webhook controller receiving messages
- [x] TelegramService for sending messages
- [x] Webhook registration on startup
- [x] ConversationService stub
- [x] AdminController for webhook management
- [x] Complete testing and documentation

**📄 See [SPRINT2.md](SPRINT2.md) for detailed documentation**

## Sprint 3 Deliverables ✅

- [x] JPA entities (Conversation, Message) with indexes
- [x] JPA repositories with custom queries
- [x] Redis configuration with custom serialization
- [x] Enhanced ConversationService with cache-aside pattern
- [x] Conversation cleanup task with scheduling
- [x] Database persistence working
- [x] Redis caching with 1-hour TTL
- [x] Scheduled cleanup (daily at 2 AM)

**📄 See [SPRINT3.md](SPRINT3.md) for detailed documentation**

## Next Steps (Sprint 4+)

- Implement Zoho Desk API integration
- Create channel endpoints for Zoho
- Implement ticket creation from conversations
- Add agent reply functionality
- Create conversation management API

## Troubleshooting

### Docker not starting

```bash
# Check Docker Desktop is running
docker --version

# Check compose file syntax
docker-compose config
```

### Build fails

```bash
# Clean Gradle cache
./gradlew clean

# Check Java version
java -version  # Should be 21

# Refresh dependencies
./gradlew --refresh-dependencies build
```

### Database connection issues

```bash
# Check PostgreSQL is running
docker-compose ps postgres

# Check logs
docker-compose logs postgres

# Verify port is not in use
netstat -an | grep 5432
```

## Project Overview

This system:
1. Receives Telegram messages via webhook
2. Stores conversations in PostgreSQL
3. Caches active conversations in Redis
4. Syncs with Zoho Desk as a custom channel
5. Allows agents to create tickets and reply to users

## Architecture

```
Telegram → Spring Boot → PostgreSQL (Persist)
                ↓          Redis (Cache)
            Zoho Desk (Support Platform)
```

## License

Proprietary