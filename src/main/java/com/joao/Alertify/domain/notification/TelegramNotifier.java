package com.joao.Alertify.domain.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

/**
 * Concrete implementation of NotificationService for Telegram.
 * Calls POST https://api.telegram.org/bot{TOKEN}/sendMessage
 *
 * The telegramWebClient already has the full base URL with token
 * configured in WebClientConfig, so only the path /sendMessage is needed.
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

            String response = telegramWebClient.post()
                    .uri("/sendMessage")
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                            clientResponse.bodyToMono(String.class).map(errorBody -> {
                                log.error("Telegram rejeitou a mensagem [4xx] para chat_id={}: {}",
                                        recipient, errorBody);
                                return new RuntimeException("Telegram 4xx: " + errorBody);
                            })
                    )
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                            clientResponse.bodyToMono(String.class).map(errorBody -> {
                                log.error("Erro interno do Telegram [5xx]: {}", errorBody);
                                return new RuntimeException("Telegram 5xx: " + errorBody);
                            })
                    )
                    .bodyToMono(String.class)
                    .block();

            log.info("Notificação Telegram enviada para chat_id={}. Resposta: {}", recipient, response);

        } catch (WebClientResponseException e) {
            log.error("Falha HTTP ao notificar Telegram [{}]: {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Erro inesperado ao notificar via Telegram para chat_id={}: {}", recipient, e.getMessage());
        }
    }
}



