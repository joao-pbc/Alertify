package com.joao.Alertify.infra.external;

import java.math.BigDecimal;

/**
 * DTO representing the raw response from the external Stock/News API.
 */
public record StockApiResponse(
        String ticker,
        String name,
        BigDecimal price,
        String exchange
) {}

