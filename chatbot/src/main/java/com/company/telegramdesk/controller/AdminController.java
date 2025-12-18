package com.company.telegramdesk.controller;

import com.company.telegramdesk.config.TelegramWebhookRegistrar;
import com.company.telegramdesk.service.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@Slf4j
@RequiredArgsConstructor
public class AdminController {

    private final TelegramWebhookRegistrar webhookRegistrar;
    private final TelegramService telegramService;

    @PostMapping("/webhook/register")
    public ResponseEntity<String> registerWebhook() {
        try {
            webhookRegistrar.registerWebhook();
            return ResponseEntity.ok("Webhook registration attempted. Check logs for details.");
        } catch (Exception e) {
            log.error("Error registering webhook", e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/webhook/delete")
    public ResponseEntity<String> deleteWebhook() {
        try {
            webhookRegistrar.deleteWebhook();
            return ResponseEntity.ok("Webhook deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting webhook", e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/webhook/info")
    public ResponseEntity<Map<String, Object>> getWebhookInfo() {
        try {
            Map<String, Object> info = webhookRegistrar.getWebhookInfo();
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            log.error("Error getting webhook info", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/test/message")
    public ResponseEntity<String> sendTestMessage(
            @RequestParam String chatId,
            @RequestParam(defaultValue = "Test message from Telegram-Zoho Desk bot") String message
    ) {
        try {
            telegramService.sendMessage(chatId, message);
            return ResponseEntity.ok("Test message sent to chat " + chatId);
        } catch (Exception e) {
            log.error("Error sending test message", e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "telegram-zoho-desk"
        ));
    }
}
