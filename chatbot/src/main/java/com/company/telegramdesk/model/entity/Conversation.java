package com.company.telegramdesk.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversations", indexes = {
        @Index(name = "idx_chat_id", columnList = "chatId", unique = true),
        @Index(name = "idx_synced_to_zoho", columnList = "syncedToZoho"),
        @Index(name = "idx_last_message_time", columnList = "lastMessageTime")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conversation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String chatId;  // Telegram chat ID (unique)

    @Column(length = 100)
    private String username;  // @username from Telegram

    @Column(length = 100)
    private String firstName;

    @Column(length = 100)
    private String lastName;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("timestamp ASC")
    private List<Message> messages = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime lastMessageTime;

    @Column(nullable = false)
    private boolean syncedToZoho = false;  // Has Zoho pulled this?

    @Column(length = 100)
    private String zohoDeskTicketId;  // Ticket ID if created

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (lastMessageTime == null) {
            lastMessageTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper method to add message
    public void addMessage(Message message) {
        messages.add(message);
        message.setConversation(this);
        this.lastMessageTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Helper method to get message count
    public int getMessageCount() {
        return messages != null ? messages.size() : 0;
    }
}
