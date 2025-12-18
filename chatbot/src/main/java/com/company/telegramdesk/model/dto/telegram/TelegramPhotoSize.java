package com.company.telegramdesk.model.dto.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TelegramPhotoSize {
    @JsonProperty("file_id")
    private String fileId;

    @JsonProperty("file_unique_id")
    private String fileUniqueId;

    private Integer width;

    private Integer height;

    @JsonProperty("file_size")
    private Integer fileSize;
}
