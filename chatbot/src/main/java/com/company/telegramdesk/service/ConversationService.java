package com.company.telegramdesk.service;

import com.company.telegramdesk.model.dto.telegram.TelegramUpdate;
import com.company.telegramdesk.model.entity.Conversation;
import com.company.telegramdesk.model.entity.Message;
import com.company.telegramdesk.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConversationService {

    private final TelegramService telegramService;
    private final ConversationRepository conversationRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX = "conversation:";
    private static final long CACHE_TTL_HOURS = 1;

    @Transactional
    public void processIncomingMessage(TelegramUpdate update) {
        if (update.getMessage() == null || update.getMessage().getText() == null) {
            log.warn("Received update without text message: {}", update.getUpdateId());
            return;
        }

        var telegramMsg = update.getMessage();
        String chatId = telegramMsg.getChat().getId().toString();
        String text = telegramMsg.getText();
        String username = telegramMsg.getFrom().getUsername();

        log.info("Processing message from user {} (chat {}): {}", username, chatId, text);

        // Get or create conversation
        Conversation conversation = getOrCreateConversation(chatId, telegramMsg);

        // Add message to conversation
        Message message = new Message();
        message.setText(text);
        message.setSender("user");
        message.setTelegramMessageId(telegramMsg.getMessageId().toString());
        message.setTimestamp(LocalDateTime.now());

        conversation.addMessage(message);
        conversation.setSyncedToZoho(false);  // Mark for sync

        // Save to database
        Conversation saved = conversationRepository.save(conversation);
        log.info("Saved conversation {} with {} messages", saved.getId(), saved.getMessageCount());

        // Update cache
        cacheConversation(saved);

        // Send acknowledgment response
        String response = String.format(
                "✅ Message received!\n\n" +
                        "Your message: \"%s\"\n\n" +
                        "Our support team will review your message shortly. " +
                        "Conversation ID: %d",
                text, saved.getId()
        );
        telegramService.sendMessage(chatId, response);
    }

    private Conversation getOrCreateConversation(String chatId, com.company.telegramdesk.model.dto.telegram.TelegramMessage telegramMsg) {
        // Try cache first
        Conversation cached = getFromCache(chatId);
        if (cached != null) {
            log.debug("Found conversation in cache for chat {}", chatId);
            return cached;
        }

        // Try database
        Optional<Conversation> existing = conversationRepository.findByChatId(chatId);
        if (existing.isPresent()) {
            log.debug("Found conversation in database for chat {}", chatId);
            Conversation conversation = existing.get();
            cacheConversation(conversation);
            return conversation;
        }

        // Create new conversation
        log.info("Creating new conversation for chat {}", chatId);
        Conversation conversation = new Conversation();
        conversation.setChatId(chatId);
        conversation.setUsername(telegramMsg.getChat().getUsername());
        conversation.setFirstName(telegramMsg.getChat().getFirstName());
        conversation.setLastName(telegramMsg.getChat().getLastName());
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
        conversation.setLastMessageTime(LocalDateTime.now());

        return conversation;
    }

    private Conversation getFromCache(String chatId) {
        String cacheKey = CACHE_PREFIX + chatId;
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof Conversation) {
                return (Conversation) cached;
            }
        } catch (Exception e) {
            log.warn("Error retrieving from cache for chat {}: {}", chatId, e.getMessage());
        }
        return null;
    }

    private void cacheConversation(Conversation conversation) {
        String cacheKey = CACHE_PREFIX + conversation.getChatId();
        try {
            redisTemplate.opsForValue().set(cacheKey, conversation, CACHE_TTL_HOURS, TimeUnit.HOURS);
            log.debug("Cached conversation for chat {}", conversation.getChatId());
        } catch (Exception e) {
            log.warn("Error caching conversation for chat {}: {}", conversation.getChatId(), e.getMessage());
        }
    }

    public void invalidateCache(String chatId) {
        String cacheKey = CACHE_PREFIX + chatId;
        redisTemplate.delete(cacheKey);
        log.debug("Invalidated cache for chat {}", chatId);
    }

    public Optional<Conversation> getConversationByChatId(String chatId) {
        // Try cache first
        Conversation cached = getFromCache(chatId);
        if (cached != null) {
            return Optional.of(cached);
        }

        // Try database
        Optional<Conversation> conversation = conversationRepository.findByChatId(chatId);
        conversation.ifPresent(this::cacheConversation);
        return conversation;
    }

    public Optional<Conversation> getConversationById(Long id) {
        return conversationRepository.findById(id);
    }

    public List<Conversation> getUnsyncedConversations() {
        return conversationRepository.findBySyncedToZohoFalse();
    }

    public List<Conversation> getRecentConversations(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return conversationRepository.findRecentConversations(since);
    }

    public long getUnsyncedCount() {
        return conversationRepository.countBySyncedToZohoFalse();
    }

    @Transactional
    public void markAsSynced(Long conversationId) {
        conversationRepository.findById(conversationId).ifPresent(conversation -> {
            conversation.setSyncedToZoho(true);
            conversationRepository.save(conversation);
            invalidateCache(conversation.getChatId());
            log.info("Marked conversation {} as synced to Zoho", conversationId);
        });
    }
}
