package com.joao.Alertify.domain.news;

import com.joao.Alertify.domain.stock.Stock;

/**
 * Domain event published when a new news article is detected for a stock.
 * Used by the Observer pattern via Spring's ApplicationEventPublisher.
 */
public record NewsDetectedEvent(
        Stock stock,
        String headline,
        String description,
        String url,
        String recipient  // e.g. Telegram chat ID
) {}

