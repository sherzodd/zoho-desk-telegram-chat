package com.company.telegramdesk.task;

import com.company.telegramdesk.model.entity.Conversation;
import com.company.telegramdesk.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ConversationCleanupTask {

    private final ConversationRepository conversationRepository;

    @Value("${conversation.cleanup.retention-days:7}")
    private int retentionDays;

    @Value("${conversation.cleanup.enabled:true}")
    private boolean cleanupEnabled;

    /**
     * Run daily at 2 AM to clean up old conversations
     */
    @Scheduled(cron = "${conversation.cleanup.cron:0 0 2 * * ?}")
    @Transactional
    public void cleanupOldConversations() {
        if (!cleanupEnabled) {
            log.debug("Conversation cleanup is disabled");
            return;
        }

        log.info("Starting conversation cleanup task (retention: {} days)", retentionDays);

        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        List<Conversation> oldConversations = conversationRepository.findByLastMessageTimeBefore(cutoff);

        if (oldConversations.isEmpty()) {
            log.info("No conversations to clean up");
            return;
        }

        // Only delete conversations that have been synced to Zoho (or don't have ticket)
        List<Conversation> toDelete = oldConversations.stream()
                .filter(c -> c.isSyncedToZoho() || c.getZohoDeskTicketId() == null)
                .toList();

        if (!toDelete.isEmpty()) {
            conversationRepository.deleteAll(toDelete);
            log.info("✅ Cleaned up {} old conversations (out of {} found)",
                    toDelete.size(), oldConversations.size());
        } else {
            log.info("No conversations eligible for cleanup (all have active tickets)");
        }
    }

    /**
     * Optional: Run every hour to log statistics
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void logStatistics() {
        try {
            long totalConversations = conversationRepository.count();
            long unsyncedConversations = conversationRepository.countBySyncedToZohoFalse();

            List<Conversation> recentConversations = conversationRepository
                    .findRecentConversations(LocalDateTime.now().minusDays(1));

            log.info("📊 Conversation Statistics: Total={}, Unsynced={}, Last24h={}",
                    totalConversations, unsyncedConversations, recentConversations.size());
        } catch (Exception e) {
            log.error("Error logging statistics", e);
        }
    }
}
