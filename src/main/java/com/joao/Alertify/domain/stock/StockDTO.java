package com.joao.Alertify.domain.stock;

import java.time.LocalDateTime;

public record StockDTO(
        Long id,
        String ticker,
        String name,
        String exchange,
        boolean active,
        LocalDateTime createdAt
) {
    /** Maps a persisted {@link Stock} (with id, user, createdAt). */
    public static StockDTO from(Stock stock) {
        return new StockDTO(
                stock.getId(),
                stock.getTicker(),
                stock.getName(),
                stock.getExchange(),
                stock.isActive(),
                stock.getCreatedAt()
        );
    }

    /**
     * Maps a transient {@link Stock} returned by the external API
     * (no id, no user, no createdAt).
     */
    public static StockDTO fromExternal(Stock stock) {
        return new StockDTO(
                null,
                stock.getTicker(),
                stock.getName(),
                stock.getExchange(),
                false,
                null
        );
    }
}

