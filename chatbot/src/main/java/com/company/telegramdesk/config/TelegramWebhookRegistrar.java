package com.company.telegramdesk.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@Slf4j
public class TelegramWebhookRegistrar {

    @Value("${telegram.bot-token}")
    private String botToken;

    @Value("${telegram.webhook-url}")
    private String webhookUrl;

    @Value("${telegram.webhook.auto-register:false}")
    private boolean autoRegister;

    private final RestTemplate restTemplate = new RestTemplate();

    @EventListener(ApplicationReadyEvent.class)
    public void registerWebhook() {
        if (!autoRegister) {
            log.info("Webhook auto-registration is disabled. Set telegram.webhook.auto-register=true to enable.");
            log.info("To manually register webhook, use: POST https://api.telegram.org/bot{}/setWebhook?url={}",
                    botToken.substring(0, 5) + "...", webhookUrl);
            return;
        }

        String url = String.format(
                "https://api.telegram.org/bot%s/setWebhook?url=%s",
                botToken, webhookUrl
        );

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, null, Map.class);

            if (response.getBody() != null && Boolean.TRUE.equals(response.getBody().get("ok"))) {
                log.info("✅ Telegram webhook registered successfully: {}", webhookUrl);
                log.info("Webhook response: {}", response.getBody());
            } else {
                log.error("❌ Failed to register webhook. Response: {}", response.getBody());
            }
        } catch (Exception e) {
            log.error("❌ Error registering webhook: {}", e.getMessage());
            log.error("Make sure your bot token is correct and the webhook URL is accessible from internet");
        }
    }

    public void deleteWebhook() {
        String url = String.format(
                "https://api.telegram.org/bot%s/deleteWebhook",
                botToken
        );

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, null, Map.class);
            log.info("Webhook deleted: {}", response.getBody());
        } catch (Exception e) {
            log.error("Error deleting webhook", e);
        }
    }

    public Map<String, Object> getWebhookInfo() {
        String url = String.format(
                "https://api.telegram.org/bot%s/getWebhookInfo",
                botToken
        );

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error getting webhook info", e);
            return null;
        }
    }
}
