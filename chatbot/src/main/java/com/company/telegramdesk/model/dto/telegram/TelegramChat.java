package com.company.telegramdesk.model.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TelegramChat {
    private Long id;

    private String type;  // "private", "group", "supergroup", "channel"

    private String username;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String title;  // For groups/channels
}
