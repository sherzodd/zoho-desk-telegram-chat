package com.company.telegramdesk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.company.telegramdesk.repository")
public class TelegramDeskApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelegramDeskApplication.class, args);
    }
}
