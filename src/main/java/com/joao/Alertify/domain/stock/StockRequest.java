package com.joao.Alertify.domain.stock;

import jakarta.validation.constraints.NotBlank;

public record StockRequest(
        @NotBlank(message = "Ticker é obrigatório")
        String ticker,

        String name,

        String exchange
) {}

