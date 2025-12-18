package com.company.telegramdesk.repository;

import com.company.telegramdesk.model.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Find all messages for a conversation, ordered by timestamp
     */
    List<Message> findByConversationIdOrderByTimestampAsc(Long conversationId);

    /**
     * Find messages by sender type (user or agent)
     */
    List<Message> findByConversationIdAndSenderOrderByTimestampAsc(Long conversationId, String sender);

    /**
     * Find message by Telegram message ID
     */
    Optional<Message> findByTelegramMessageId(String telegramMessageId);

    /**
     * Count messages in a conversation
     */
    long countByConversationId(Long conversationId);

    /**
     * Count messages by sender type in a conversation
     */
    long countByConversationIdAndSender(Long conversationId, String sender);

    /**
     * Find messages within a time range
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId " +
            "AND m.timestamp BETWEEN :start AND :end ORDER BY m.timestamp ASC")
    List<Message> findByConversationIdAndTimestampBetween(
            @Param("conversationId") Long conversationId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /**
     * Find recent messages across all conversations
     */
    @Query("SELECT m FROM Message m WHERE m.timestamp >= :since ORDER BY m.timestamp DESC")
    List<Message> findRecentMessages(@Param("since") LocalDateTime since);

    /**
     * Get the latest message for a conversation
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId " +
            "ORDER BY m.timestamp DESC LIMIT 1")
    Optional<Message> findLatestMessageByConversationId(@Param("conversationId") Long conversationId);

    /**
     * Search messages by text content
     */
    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId " +
            "AND LOWER(m.text) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
            "ORDER BY m.timestamp DESC")
    List<Message> searchMessagesByText(
            @Param("conversationId") Long conversationId,
            @Param("searchText") String searchText
    );
}
