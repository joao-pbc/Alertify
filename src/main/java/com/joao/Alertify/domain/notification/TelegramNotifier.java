package com.joao.Alertify.domain.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Concrete implementation of NotificationService for Telegram.
 * Calls the Telegram Bot API sendMessage endpoint.
 */
@Slf4j
@Component
public class TelegramNotifier implements NotificationService {

    private final WebClient telegramWebClient;

    public TelegramNotifier(@Qualifier("telegramWebClient") WebClient telegramWebClient) {
        this.telegramWebClient = telegramWebClient;
    }

    @Override
    public void notify(String message, String recipient) {
        try {
            Map<String, String> body = Map.of(
                    "chat_id", recipient,
                    "text", message,
                    "parse_mode", "Markdown"
            );

            telegramWebClient.post()
                    .uri("/sendMessage")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                            response -> log.info("Notificação Telegram enviada para {}", recipient),
                            error -> log.error("Falha ao enviar notificação Telegram: {}", error.getMessage())
                    );
        } catch (Exception e) {
            log.error("Erro inesperado ao notificar via Telegram: {}", e.getMessage());
        }
    }
}

