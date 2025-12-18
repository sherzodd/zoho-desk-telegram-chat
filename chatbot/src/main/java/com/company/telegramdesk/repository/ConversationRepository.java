package com.company.telegramdesk.repository;

import com.company.telegramdesk.model.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * Find conversation by Telegram chat ID
     */
    Optional<Conversation> findByChatId(String chatId);

    /**
     * Find conversations not yet synced to Zoho Desk
     */
    List<Conversation> findBySyncedToZohoFalse();

    /**
     * Find conversations that haven't received messages since the given time
     * Useful for cleanup tasks
     */
    List<Conversation> findByLastMessageTimeBefore(LocalDateTime time);

    /**
     * Find conversation by Zoho Desk ticket ID
     */
    Optional<Conversation> findByZohoDeskTicketId(String ticketId);

    /**
     * Check if a conversation exists for a given chat ID
     */
    boolean existsByChatId(String chatId);

    /**
     * Count conversations that need syncing to Zoho
     */
    long countBySyncedToZohoFalse();

    /**
     * Find recent conversations (last N days)
     */
    @Query("SELECT c FROM Conversation c WHERE c.lastMessageTime >= :since ORDER BY c.lastMessageTime DESC")
    List<Conversation> findRecentConversations(@Param("since") LocalDateTime since);

    /**
     * Find conversations with messages (excluding empty conversations)
     */
    @Query("SELECT c FROM Conversation c WHERE SIZE(c.messages) > 0")
    List<Conversation> findConversationsWithMessages();

    /**
     * Find conversations by username
     */
    List<Conversation> findByUsernameContainingIgnoreCase(String username);
}
