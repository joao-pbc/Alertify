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
}

