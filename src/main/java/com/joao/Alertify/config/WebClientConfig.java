package com.joao.Alertify.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean("newsApiWebClient")
    public WebClient newsApiWebClient(
            @Value("${app.news-api.base-url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Bean("stockApiWebClient")
    public WebClient stockApiWebClient(
            @Value("${app.stock-api.base-url}") String baseUrl,
            @Value("${app.stock-api.token}") String token) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Authorization", "Bearer " + token)
                .build();
    }

    @Bean("telegramWebClient")
    public WebClient telegramWebClient(
            @Value("${app.telegram.api-url}") String telegramApiUrl,
            @Value("${app.telegram.bot-token}") String botToken) {
        // URL final: https://api.telegram.org/bot{TOKEN}
        // Chamadas usam o sufixo /sendMessage, /getMe, etc.
        String baseUrl = telegramApiUrl + botToken;
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}

