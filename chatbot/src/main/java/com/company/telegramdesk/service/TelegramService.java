package com.company.telegramdesk.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class TelegramService {

    @Value("${telegram.bot-token}")
    private String botToken;

    private final RestTemplate restTemplate;

    public TelegramService() {
        this.restTemplate = new RestTemplate();
    }

    public void sendMessage(String chatId, String text) {
        String url = String.format(
                "https://api.telegram.org/bot%s/sendMessage",
                botToken
        );

        Map<String, Object> payload = new HashMap<>();
        payload.put("chat_id", chatId);
        payload.put("text", text);
        payload.put("parse_mode", "HTML");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            log.info("Message sent to chat {}: {}", chatId, response.getStatusCode());
        } catch (Exception e) {
            log.error("Failed to send message to chat {}", chatId, e);
            throw new RuntimeException("Failed to send Telegram message", e);
        }
    }

    public void sendMessage(String chatId, String text, Long replyToMessageId) {
        String url = String.format(
                "https://api.telegram.org/bot%s/sendMessage",
                botToken
        );

        Map<String, Object> payload = new HashMap<>();
        payload.put("chat_id", chatId);
        payload.put("text", text);
        payload.put("parse_mode", "HTML");
        if (replyToMessageId != null) {
            payload.put("reply_to_message_id", replyToMessageId);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            log.info("Reply sent to chat {}: {}", chatId, response.getStatusCode());
        } catch (Exception e) {
            log.error("Failed to send reply to chat {}", chatId, e);
            throw new RuntimeException("Failed to send Telegram reply", e);
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
            log.error("Failed to get webhook info", e);
            return null;
        }
    }
}
