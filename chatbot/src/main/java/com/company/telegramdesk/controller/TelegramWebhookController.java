package com.company.telegramdesk.controller;

import com.company.telegramdesk.model.dto.telegram.TelegramUpdate;
import com.company.telegramdesk.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
@Slf4j
@RequiredArgsConstructor
public class TelegramWebhookController {

    private final ConversationService conversationService;

    @PostMapping("/telegram")
    public ResponseEntity<String> handleWebhook(@RequestBody TelegramUpdate update) {
        log.info("Received Telegram update ID: {}", update.getUpdateId());

        try {
            if (update.getMessage() != null && update.getMessage().getText() != null) {
                conversationService.processIncomingMessage(update);
            } else {
                log.debug("Skipping non-text message or empty update");
            }

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Error processing webhook", e);
            // Return 200 to Telegram to avoid retries for application errors
            return ResponseEntity.ok("ERROR");
        }
    }

    @GetMapping("/telegram")
    public ResponseEntity<String> handleGet() {
        return ResponseEntity.ok("Telegram webhook is active");
    }
}
