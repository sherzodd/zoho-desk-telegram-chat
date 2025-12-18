package com.company.telegramdesk.model.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TelegramUpdate {
    @JsonProperty("update_id")
    private Long updateId;

    private TelegramMessage message;

    @JsonProperty("edited_message")
    private TelegramMessage editedMessage;

    @JsonProperty("channel_post")
    private TelegramMessage channelPost;

    @JsonProperty("edited_channel_post")
    private TelegramMessage editedChannelPost;
}
