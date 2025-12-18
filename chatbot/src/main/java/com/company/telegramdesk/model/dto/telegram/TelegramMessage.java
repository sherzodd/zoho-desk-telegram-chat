package com.company.telegramdesk.model.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TelegramMessage {
    @JsonProperty("message_id")
    private Long messageId;

    private TelegramUser from;

    private TelegramChat chat;

    private Long date;

    private String text;

    // Additional fields for media messages
    private TelegramPhotoSize[] photo;

    private TelegramDocument document;

    private String caption;

    @JsonProperty("reply_to_message")
    private TelegramMessage replyToMessage;
}
